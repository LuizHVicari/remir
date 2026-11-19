package dev.luizhenriquevicari.remir.app.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders

class ClerkTokenConfig {
    var tokenProvider: suspend () -> String? = { null }
}

/**
 * Fetches the Clerk token on EVERY request and sets `Authorization: Bearer`.
 * Do not use `Auth { bearer { loadTokens } }`: it caches, and Clerk tokens are short-lived.
 */
val ClerkToken =
    createClientPlugin("ClerkToken", ::ClerkTokenConfig) {
        val tokenProvider = pluginConfig.tokenProvider
        onRequest { request, _ ->
            val token = tokenProvider() ?: return@onRequest
            request.headers[HttpHeaders.Authorization] = "Bearer $token"
        }
    }
