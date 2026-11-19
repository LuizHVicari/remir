package dev.luizhenriquevicari.remir.server.config

import io.ktor.server.config.ApplicationConfig

/** Typed configuration. Values come from `application.conf`, which reads environment variables. */
data class AppConfig(
    val database: DatabaseConfig,
    val clerk: ClerkConfig,
) {
    companion object {
        fun from(config: ApplicationConfig) = AppConfig(
            database = DatabaseConfig(
                host = config.required("database.host", "DATABASE_HOST"),
                port = config.required("database.port", "DATABASE_PORT").toInt(),
                name = config.required("database.name", "DATABASE_NAME"),
                user = config.required("database.user", "DATABASE_USER"),
                password = config.required("database.password", "DATABASE_PASSWORD"),
                sslMode = config.propertyOrNull("database.sslmode")?.getString()?.takeIf { it.isNotBlank() },
                poolSize = config.propertyOrNull("database.poolSize")?.getString()?.toIntOrNull() ?: DEFAULT_POOL_SIZE,
            ),
            clerk = ClerkConfig(issuer = config.propertyOrNull("clerk.issuer")?.getString().orEmpty().trimEnd('/')),
        )

        private fun ApplicationConfig.required(path: String, envVar: String): String =
            propertyOrNull(path)?.getString()
                ?: error("Missing environment variable $envVar (for local dev, copy .env.example to .env)")
    }
}

private const val DEFAULT_POOL_SIZE = 10

/**
 * @property sslMode Postgres `sslmode` (e.g. `require` for Neon). Null leaves it out, which is what local dev uses.
 * @property poolSize Max connections per server instance. Keep it small on managed Postgres with a connection
 * limit, because each Cloud Run instance opens its own pool.
 */
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
    val sslMode: String? = null,
    val poolSize: Int = DEFAULT_POOL_SIZE,
) {
    val jdbcUrl: String
        get() = "jdbc:postgresql://$host:$port/$name" + (sslMode?.let { "?sslmode=$it" } ?: "")
}

/** @property issuer Clerk Frontend API URL (the tokens' `iss`), without a trailing slash. */
data class ClerkConfig(
    val issuer: String,
)
