#!/usr/bin/env bash
# Blue-green deploy of the Remir server on a Docker host that already runs Traefik. Runs ON the server (CI streams
# it over SSH). It never touches containers or images that do not belong to Remir.
#
# Required environment:
#   ENVIRONMENT   dev | prod
#   IMAGE         full image reference, e.g. ghcr.io/luizhvicari/remir-server:1.5.0-dev.a1b2c3d
#   ENV_FILE      path of the env file holding the application settings (DATABASE_*, CLERK_ISSUER, ...)
#   DOMAIN        host name Traefik routes to this environment, e.g. remir.luizhenriquevicari.dev
# Optional:
#   GHCR_USER, GHCR_TOKEN   log in to ghcr.io before pulling
#   TRAEFIK_NETWORK         Docker network Traefik watches (default proxy); it must already exist
#   CERT_RESOLVER           Traefik certificate resolver for the router. Leave empty behind Cloudflare's proxy,
#                           where the ACME TLS challenge cannot pass; set it when the record is DNS only.
#   MEMORY                  container memory limit (default 448m)
#   KEEP_IMAGES             how many Remir images to keep on the host (default 3)
#   HEALTH_TIMEOUT_SECONDS  how long to wait for the new container to be healthy (default 120)
#   DRAIN_SECONDS           how long Traefik gets to stop routing to the old container before it is stopped (default 3)
set -euo pipefail

: "${ENVIRONMENT:?ENVIRONMENT must be dev or prod}"
: "${IMAGE:?IMAGE is required}"
: "${ENV_FILE:?ENV_FILE is required}"
: "${DOMAIN:?DOMAIN is required}"
case "$ENVIRONMENT" in dev | prod) ;; *) echo "ENVIRONMENT must be dev or prod, got '$ENVIRONMENT'" >&2; exit 2 ;; esac

MEMORY="${MEMORY:-448m}"
KEEP_IMAGES="${KEEP_IMAGES:-3}"
NETWORK="${TRAEFIK_NETWORK:-proxy}"
CERT_RESOLVER="${CERT_RESOLVER:-}"
PREFIX="remir-${ENVIRONMENT}"
IMAGE_REPO="${IMAGE%:*}"
HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-120}"

log() { echo "[deploy:${ENVIRONMENT}] $*"; }

running() { docker ps --format '{{.Names}}' | grep -qx "$1"; }

if [ -n "${GHCR_TOKEN:-}" ]; then
  # A private Docker config keeps this login away from the other projects' registry credentials on the host.
  export DOCKER_CONFIG="${REMIR_DOCKER_CONFIG:-/opt/remir/docker-config}"
  mkdir -p "$DOCKER_CONFIG"
  log "logging in to ghcr.io"
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "${GHCR_USER:-token}" --password-stdin >/dev/null
fi

# The network belongs to Traefik and to the other projects: only check it, never create or change it.
docker network inspect "$NETWORK" >/dev/null 2>&1 || {
  echo "Docker network '$NETWORK' does not exist. Is Traefik set up on this host?" >&2
  exit 1
}

# Tags are versions (or version plus commit), so an image that is already on the host is the right one.
if ! docker image inspect "$IMAGE" >/dev/null 2>&1; then
  log "pulling $IMAGE"
  docker pull "$IMAGE" >/dev/null
fi

if running "${PREFIX}-blue"; then
  CURRENT=blue NEXT=green
elif running "${PREFIX}-green"; then
  CURRENT=green NEXT=blue
else
  CURRENT=none NEXT=blue
fi
NEW="${PREFIX}-${NEXT}"
OLD="${PREFIX}-${CURRENT}"
log "current: ${CURRENT}, starting: ${NEW}"

docker rm -f "$NEW" >/dev/null 2>&1 || true

# Both colors declare the same router and service names, so Traefik merges them into one service. Traefik only
# routes to containers whose Docker health check passes, so the new one receives traffic once it is healthy.
labels=(
  -l "traefik.enable=true"
  -l "traefik.http.routers.${PREFIX}.rule=Host(\`${DOMAIN}\`)"
  -l "traefik.http.routers.${PREFIX}.entrypoints=websecure"
  -l "traefik.http.services.${PREFIX}.loadbalancer.server.port=8080"
)
if [ -n "$CERT_RESOLVER" ]; then
  labels+=(-l "traefik.http.routers.${PREFIX}.tls.certresolver=${CERT_RESOLVER}")
fi

docker run -d \
  --name "$NEW" \
  --network "$NETWORK" \
  --env-file "$ENV_FILE" \
  --memory "$MEMORY" \
  --memory-swap "$MEMORY" \
  --restart unless-stopped \
  --health-cmd 'curl -fsS http://localhost:8080/health || exit 1' \
  --health-interval 5s \
  --health-timeout 3s \
  --health-retries 3 \
  --health-start-period 20s \
  "${labels[@]}" \
  "$IMAGE" >/dev/null

log "waiting for ${NEW} to become healthy (up to ${HEALTH_TIMEOUT_SECONDS}s)"
deadline=$((SECONDS + HEALTH_TIMEOUT_SECONDS))
while true; do
  state=$(docker inspect --format '{{.State.Health.Status}}' "$NEW" 2>/dev/null || echo missing)
  alive=$(docker inspect --format '{{.State.Running}}' "$NEW" 2>/dev/null || echo false)
  if [ "$state" = healthy ]; then
    break
  fi
  # Give up at the deadline, or right away when the container is gone or crashed.
  if [ "$SECONDS" -ge "$deadline" ] || [ "$state" = missing ] || [ "$alive" != true ]; then
    log "FAILED: ${NEW} is '${state}' (running=${alive}). Last logs:"
    docker logs --tail 40 "$NEW" 2>&1 || true
    docker rm -f "$NEW" >/dev/null 2>&1 || true
    log "the previous container (${CURRENT}) was left running"
    exit 1
  fi
  sleep 3
done
log "${NEW} is healthy"

if [ "$CURRENT" != none ]; then
  # Take the old container out of Traefik's network first, so no request is routed to it while it shuts down.
  log "draining ${OLD}"
  docker network disconnect "$NETWORK" "$OLD" >/dev/null 2>&1 || true
  sleep "${DRAIN_SECONDS:-3}"
  log "removing ${OLD}"
  docker stop -t 20 "$OLD" >/dev/null 2>&1 || true
  docker rm -f "$OLD" >/dev/null 2>&1 || true
fi

# Keep the newest images of Remir only: other projects' images are never listed here.
in_use=$(docker ps -a --format '{{.Image}}' | sort -u)
docker images "$IMAGE_REPO" --format '{{.Repository}}:{{.Tag}}' \
  | tail -n +"$((KEEP_IMAGES + 1))" \
  | while read -r image; do
    if ! grep -qx "$image" <<<"$in_use"; then
      docker rmi "$image" >/dev/null 2>&1 || true
    fi
  done

log "done: ${IMAGE} is live as ${NEW} at https://${DOMAIN}"
