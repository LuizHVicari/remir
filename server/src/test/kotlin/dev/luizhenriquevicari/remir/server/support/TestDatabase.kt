package dev.luizhenriquevicari.remir.server.support

import dev.luizhenriquevicari.remir.core.api.RemirJson
import dev.luizhenriquevicari.remir.server.Dependencies
import dev.luizhenriquevicari.remir.server.config.AppConfig
import dev.luizhenriquevicari.remir.server.config.ClerkConfig
import dev.luizhenriquevicari.remir.server.config.DatabaseConfig
import dev.luizhenriquevicari.remir.server.setup
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.testcontainers.postgresql.PostgreSQLContainer

/** Real Postgres (Testcontainers), one per test JVM. Ryuk removes the container at the end. */
object TestDatabase {
    private const val POSTGRES_PORT = 5432

    /** Same image as `generateMigrations`, passed by Gradle (see server/build.gradle.kts). */
    private val image: String =
        requireNotNull(System.getProperty("remir.postgresImage")) {
            "System property remir.postgresImage is missing; run the tests through Gradle"
        }

    private val container: PostgreSQLContainer by lazy {
        PostgreSQLContainer(image).apply { start() }
    }

    val dependencies: Dependencies by lazy {
        val config =
            AppConfig(
                database =
                    DatabaseConfig(
                        host = container.host,
                        port = container.getMappedPort(POSTGRES_PORT),
                        name = container.databaseName,
                        user = container.username,
                        password = container.password,
                    ),
                clerk = ClerkConfig(TestJwt.ISSUER),
            )
        Dependencies.create(config, TestJwt.jwkProvider)
    }
}

/**
 * Starts the application with test dependencies.
 * `config = MapApplicationConfig()` stops `testApplication` from loading `application.conf`
 * (and the production `module()`, which would read the real env vars).
 */
fun remirTestApplication(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
    testApplication {
        environment { config = MapApplicationConfig() }
        application { setup(TestDatabase.dependencies) }
        val client = createClient { install(ContentNegotiation) { json(RemirJson) } }
        block(client)
    }
