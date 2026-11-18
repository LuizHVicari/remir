# Remir

Personal life-management app. It is a Gradle monorepo with three modules:

- `core`: the shared Kotlin/JVM contract;
- `server`: Ktor, Exposed and Postgres;
- `app`: Android with Compose.

Authentication is handled by Clerk. Everything in the repo is written in English.

## Run

```bash
cp .env.example .env                    # once; server + docker config
docker compose up -d                    # Postgres
./gradlew :server:run                   # API (loads .env)
./gradlew :server:generateMigrations    # after changing an Exposed Table
./gradlew build                         # everything, including tests (needs Docker)
./gradlew :app:installDebug             # app on the emulator
```

Clerk setup, `.env` and `local.properties` are described in `README.md`.

## Conventions

**Before creating or changing features, routes, tables, screens, ViewModels, validation or auth, load the `remir-conventions` skill** (`.claude/skills/remir-conventions/SKILL.md`). It is the source of truth for the project's conventions.
