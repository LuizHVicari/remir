# Remir

Personal life-management app: expenses, church, habits, work, study and fitness.

| Module | What it is |
|---|---|
| `core/` | Plain Kotlin/JVM shared by server and app: DTOs, typed routes (Ktor Resources), validators (Konform) |
| `server/` | Ktor API with Exposed, PostgreSQL and Flyway, authenticated with Clerk JWTs |
| `app/` | Android app: Jetpack Compose, Koin, Ktor Client, Clerk |
| `build-logic/` | Gradle convention plugins |

Code conventions live in [`.claude/skills/remir-conventions/SKILL.md`](.claude/skills/remir-conventions/SKILL.md).

## Requirements

- Android Studio Rabbit 2 (2026.2.2) or newer, for AGP 9.5.
- Android SDK Platform 37. Gradle downloads it if the SDK licenses are accepted.
- Docker. It runs the local Postgres, the server tests (Testcontainers) and `generateMigrations`.
- JDK: Gradle uses a Java 21 toolchain and downloads one via foojay if none is installed.

## 1. Create the Clerk instance

1. At <https://dashboard.clerk.com>, create an application and pick the sign-in methods (for example email + Google).
2. Open **Configure → Native applications** and **enable the Native API**. Without it, the Android SDK fails with `native_api_disabled`.
3. On the same page, add an Android application:
   - **Namespace**: `android_app` (the Digital Asset Links namespace, not the app name);
   - **Package name**: `dev.luizhenriquevicari.remir`;
   - **SHA-256 certificate fingerprints**: needed by passkeys and Google One Tap. Get it with `./gradlew :app:signingReport` (the `SHA-256` line of the `debug` variant). Each machine has its own debug keystore, so register every fingerprint you use; add the release (or Play App Signing) fingerprint when there is one.
4. Under **API keys**, copy two values:
   - the **Publishable key** (`pk_test_...`), which goes to the app;
   - the **Frontend API URL** (for example `https://your-app.clerk.accounts.dev`), which goes to the server as the issuer.

The Secret key is not needed: the server only verifies JWTs against the public JWKS.

## 2. Server and Docker config: `.env`

```bash
cp .env.example .env
```

`docker compose` and `./gradlew :server:run` both read this file. It is git-ignored, so each machine keeps its own copy.

| Variable | Meaning |
|---|---|
| `DATABASE_HOST` | Postgres host (`localhost` with compose) |
| `DATABASE_PORT` | **Host** port. Compose publishes Postgres on it and the server connects to it. If 5432 is taken (for example by a native Postgres), pick another port such as `5433`. |
| `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD` | Database and credentials. Compose creates them on first start. |
| `PORT` | Server HTTP port (default `8080`) |
| `CLERK_ISSUER` | Clerk Frontend API URL. While it is empty, every authenticated route answers 401. |

Already running Postgres natively and want to use it? Skip compose and point the `DATABASE_*` values at that instance.

In production, set the same variables as real environment variables.

## 3. App config

### Debug: `local.properties`

Android Studio creates this file in the project root with `sdk.dir`. It is never committed; `local.properties.example` is the template. Add:

```properties
clerk.publishableKey=pk_test_xxxxxxxxxxxxxxxx
# Optional. The default, http://10.0.2.2:8080/, is the host's localhost seen from the emulator.
# Change the port if you changed PORT in .env.
# On a physical device, run `adb reverse tcp:8080 tcp:8080` and use localhost (it also works on the emulator):
# api.url=http://localhost:8080/
# Without adb, use your machine's LAN IP and allow that host in
# app/src/debug/res/xml/network_security_config.xml.
```

Rebuild the app after changing it: the values are compiled into `BuildConfig`.

### Release: Gradle properties or environment variables

A release build does **not** read `local.properties`, so it never depends on one machine. Give it both values as Gradle properties (`-P` on the command line, or `~/.gradle/gradle.properties`) or as environment variables (for CI):

| Gradle property | Environment variable | Value |
|---|---|---|
| `remir.api.url` | `REMIR_API_URL` | Backend URL. Must start with `https://`. |
| `remir.clerk.publishableKey` | `REMIR_CLERK_PUBLISHABLE_KEY` | Production publishable key (`pk_live_...`) |

```bash
./gradlew :app:assembleRelease \
  -Premir.api.url=https://api.example.com/ \
  -Premir.clerk.publishableKey=pk_live_xxxxxxxxxxxxxxxx
```

If one is missing, or the URL is not `https://`, only the release build fails, with a message naming the property. Debug builds are not affected. Don't put the real values in the repo's `gradle.properties`.

## 4. Run from Android Studio

1. **File → Open** the root `remir/` folder. Gradle syncs on its own.
2. Start Postgres with `docker compose up -d` (Studio terminal).
3. Run the **server** configuration. It is shared in `.run/`, so it shows up in the run-config picker, and it is the same as `./gradlew :server:run`. Check it with `curl http://localhost:8080/health`, which should answer `ok`.
4. Pick the **app** configuration and an emulator, then click Run.
5. In the app, sign in through the Clerk screen, open **My profile**, edit the name, then **Sign out**.

## Admin role and legal documents

The privacy policy and terms are stored in the database as Markdown and served as a public page at `/legal/privacy` and `/legal/terms`. Only an admin can publish a new version (`POST /admin/legal-documents/{type}`).

The server decides who is admin from the `metadata.role` claim of the Clerk session token:

1. In the Clerk dashboard, open **Sessions → Customize session token**, and in the **Claims** editor add `{ "metadata": "{{user.public_metadata}}" }`. Do this on each Clerk instance (development and production).
2. Open **Users**, select your user, and under **User metadata** click **Edit** next to **Public**. Set `{ "role": "admin" }`.

This follows [Clerk's basic RBAC guide](https://clerk.com/docs/guides/secure/basic-rbac). The session token refreshes about every 60 seconds, so a role change can take up to a minute to show up, and custom claims are limited to about 1.2 KB. Without the claim every admin call answers 403. Not yet exercised with a real Clerk token.

## Deploy: Docker host + Traefik + Cloudflare + Neon

The API runs as a Docker container on a host reached over SSH, routed by the Traefik that already runs there, behind Cloudflare's proxy, with the database on Neon. Two workflows:

- `pr.yml`: on a pull request into `dev` or `main`, runs ktlint, detekt, the tests, the Android build and lint, hadolint, a Docker build (not pushed) and actionlint. Nothing is published.
- `publish.yml`: builds the image from the `Dockerfile`, publishes it to GHCR and deploys it with `deploy/deploy.sh`. It runs only on a push to `dev` or on a release tag.

| Trigger | Image tags | GitHub environment |
|---|---|---|
| push to `dev` | `<next minor>-dev.<sha>`, e.g. `1.5.0-dev.a1b2c3d` (`0.1.0-dev.<sha>` before the first tag) | `dev` |
| tag `v1.4.0` on `main` | `1.4.0` and `latest` | `prod` |

How a deploy works (`deploy/deploy.sh`, tested locally against Docker, not yet on the real host):

1. CI writes the application settings to `/opt/remir/<env>.env` on the host, readable by root only.
2. The script pulls the image and starts a new container (`remir-<env>-blue` or `-green`) on Traefik's Docker network (`proxy`), with Traefik labels for the environment's `DOMAIN`. Both colors use the same router and service names, so Traefik merges them, and it only routes to a container once its health check passes.
3. It waits for `/health`. If the new container does not become healthy, it is removed and the previous one keeps serving.
4. Once healthy, the previous container is taken off Traefik's network, given a few seconds to drain, and then stopped and removed. Only the newest 3 Remir images are kept.

Measured locally against a real Traefik (v3.6, 90 requests per swap, three swaps): no error responses during a swap, but **one request per swap stalled until the client timeout (5 s)**, always at the moment the new container becomes routable. The cause was not identified. Before charging users, watch a real deploy on the host and decide if that needs a fix, for example a retry middleware.

The host can be shared with other projects: the script only creates and removes containers and images named `remir-*`, it only checks that the `proxy` network exists, and it logs in to GHCR with its own Docker config. Every container has a memory limit (default `448m`, variable `CONTAINER_MEMORY`), and the JVM sizes its heap from it.

To release: merge into `main`, then create the tag on the merged commit (`git tag v1.4.0 && git push origin v1.4.0`, or the GitHub Releases button). A push to `main` without a tag publishes nothing. The workflow refuses a tag that is not on `main`.

Suggested branch protection on `main` and `dev` (Settings → Branches): require a pull request and require the `pr` checks to pass.

> Drafted from the docs and tested only locally (`deploy/deploy.sh` against Docker, workflows with actionlint). It has **not been run end to end** on the real host yet: expect to adjust a detail the first time.

### One-time setup

1. **Neon:** create a branch for each environment (the default branch is production, plus a `dev` branch created while production is still empty, so staging never holds real user data). In the Neon console use **Connect**, pick the branch, and **untick "Pooled connection"**: Flyway migrations are safer over the direct host. Neon requires SSL (the deploy sets `DATABASE_SSLMODE=require`). The free compute suspends after 5 idle minutes, so the first request after a pause is slower.

2. **Deploy key:** make a key used only by this project, and add its public half to `authorized_keys` of the user that runs Docker on the host (a dedicated non-root user in the `docker` group is safer than `root`, but the `docker` group is still root-equivalent):

   ```bash
   ssh-keygen -t ed25519 -N "" -C remir-deploy -f ~/.ssh/remir_deploy
   ```

3. **DNS and Traefik:** the host must run Traefik with the Docker provider on a `proxy` network and an `websecure` entrypoint (`docker ps` should show it; if it is stopped, `docker start traefik`, and give it `--restart unless-stopped` so a reboot does not leave it down). In Cloudflare create one `A` record per environment pointing at the host, proxied (orange cloud): `primo.luizhenriquevicari.dev` for `prod` and `primo-dev.luizhenriquevicari.dev` for `dev`. Set the zone's SSL/TLS mode to **Full**. Traefik's `le` resolver uses the TLS challenge, which cannot pass through Cloudflare's proxy, so the deploy leaves `CERT_RESOLVER` empty and Traefik serves its default certificate, which Cloudflare accepts in *Full* mode but does not validate. For a validated origin, use *Full (strict)* with a Cloudflare Origin CA certificate loaded in Traefik, or keep the record DNS only and set `CERT_RESOLVER=le`.

4. **GitHub secrets** (repository level, Settings → Secrets and variables → Actions). They are secrets, not variables, because this repository is public and secrets are masked in its public logs:

   | Secret | Value |
   |---|---|
   | `SSH_HOST` | host name or address of the server |
   | `SSH_USER` | user that runs Docker |
   | `SSH_KNOWN_HOSTS` | output of `ssh-keyscan -t ed25519 <host>`, which pins the host key |
   | `SSH_KEY` | the private key (`~/.ssh/remir_deploy`) |

   ```bash
   gh secret set SSH_KEY < ~/.ssh/remir_deploy
   ```

5. **GitHub environments** `dev` and `prod` (Settings → Environments), each with its own Neon branch:

   | Kind | Name | Value |
   |---|---|---|
   | variable | `DATABASE_HOST`, `DATABASE_NAME`, `DATABASE_USER` | from the Neon branch (direct host) |
   | secret | `DATABASE_PASSWORD` | from the Neon branch |
   | variable | `CLERK_ISSUER` | Clerk Frontend API URL of that environment |
   | variable | `DOMAIN` | host name routed to this environment (`primo.luizhenriquevicari.dev` or `primo-dev.luizhenriquevicari.dev`) |
   | variable (optional) | `DATABASE_PORT`, `DATABASE_POOL_SIZE`, `CONTAINER_MEMORY`, `CERT_RESOLVER` | defaults `5432`, `3`, `448m`, empty |

   Consider a required reviewer on the `prod` environment, so a release waits for an approval.

6. Create the `dev` branch and push to it: that runs the first deploy.

## Database migrations

Exposed `Table` objects are the schema source of truth:

1. Change or add a `*Table.kt`.
2. Run `./gradlew :server:generateMigrations` (or the **generate migrations** run config). It starts a throwaway Postgres, applies the existing scripts and writes the missing statements to `server/src/main/resources/db/migration/V<timestamp>__<description>.sql`.
3. Review the generated SQL and commit it. Flyway applies it when the server starts.

`SchemaDriftTest` fails the build if a table changed without a matching migration.

## Commands

```bash
docker compose up -d                       # local Postgres (reads .env)
./gradlew :server:run                      # API on PORT (reads .env)
./gradlew :server:generateMigrations       # new Flyway script from the Exposed tables
./gradlew build                            # everything, with lint and tests (needs Docker)
./gradlew :server:test                     # server tests (Testcontainers)
./gradlew :app:installDebug                # install on the connected emulator/device
```
