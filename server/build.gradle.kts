plugins {
    id("remir.ktor-server")
}

application {
    mainClass.set("dev.luizhenriquevicari.remir.server.ApplicationKt")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.jetbrains.markdown)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.hikari)
    implementation(libs.postgresql)
    implementation(libs.logback.classic)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.exposed.migration.jdbc)
}

val postgresImage = "postgres:${libs.versions.postgres.get()}"

// Exposed `Table` objects are the schema source of truth. `./gradlew :server:generateMigrations`
// starts a throwaway Postgres (Testcontainers), applies the existing Flyway scripts, diffs them
// against the tables and writes the missing statements as a new versioned script.
exposed {
    migrations {
        tablesPackage.set("dev.luizhenriquevicari.remir.server.features")
        testContainersImageName.set(postgresImage)
        useUpperCaseDescription.set(false)
    }
}

tasks.test {
    systemProperty("remir.postgresImage", postgresImage)
}

// Local dev: `./gradlew :server:run` loads the root `.env` (same file docker compose reads).
// Real environment variables take precedence. The server itself only reads env vars.
val dotEnv: Map<String, String> =
    rootProject
        .file(".env")
        .takeIf { it.exists() }
        ?.readLines()
        .orEmpty()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
        .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim().removeSurrounding("\"") }

tasks.named<JavaExec>("run") {
    dotEnv.filterKeys { System.getenv(it) == null }.forEach { (key, value) -> environment(key, value) }
}
