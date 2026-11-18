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
            ),
            clerk = ClerkConfig(issuer = config.propertyOrNull("clerk.issuer")?.getString().orEmpty().trimEnd('/')),
        )

        private fun ApplicationConfig.required(path: String, envVar: String): String =
            propertyOrNull(path)?.getString()
                ?: error("Missing environment variable $envVar (for local dev, copy .env.example to .env)")
    }
}

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
) {
    val jdbcUrl: String
        get() = "jdbc:postgresql://$host:$port/$name"
}

/** @property issuer Clerk Frontend API URL (the tokens' `iss`), without a trailing slash. */
data class ClerkConfig(
    val issuer: String,
)
