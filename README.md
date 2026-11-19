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
