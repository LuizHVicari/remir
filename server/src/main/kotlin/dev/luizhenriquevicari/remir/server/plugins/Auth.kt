package dev.luizhenriquevicari.remir.server.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import dev.luizhenriquevicari.remir.core.api.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.net.URI
import java.util.concurrent.TimeUnit

/** Auth provider name. Use `authenticate(CLERK_AUTH) { }` around protected routes. */
const val CLERK_AUTH = "clerk"

private const val LEEWAY_SECONDS = 5L

// JWKS cache: signing keys rarely rotate. Rate limit: bounds the calls Clerk sees for unknown `kid`s.
private const val JWKS_CACHE_SIZE = 10L
private const val JWKS_CACHE_HOURS = 24L
private const val JWKS_RATE_LIMIT_BUCKET = 10L
private const val JWKS_RATE_LIMIT_MINUTES = 1L

/** Clerk JWKS with a cache (keys rarely rotate) and a rate limit (avoids hammering Clerk with unknown `kid`s). */
fun clerkJwkProvider(issuer: String): JwkProvider {
    val base = issuer.ifBlank { "https://clerk-issuer-not-configured.invalid" }
    return JwkProviderBuilder(URI("$base/.well-known/jwks.json").toURL())
        .cached(JWKS_CACHE_SIZE, JWKS_CACHE_HOURS, TimeUnit.HOURS)
        .rateLimited(JWKS_RATE_LIMIT_BUCKET, JWKS_RATE_LIMIT_MINUTES, TimeUnit.MINUTES)
        .build()
}

fun Application.configureAuth(
    jwkProvider: JwkProvider,
    issuer: String,
) {
    if (issuer.isBlank()) {
        environment.log.warn("CLERK_ISSUER is not set: every authenticated route will answer 401.")
    }
    install(Authentication) {
        jwt(CLERK_AUTH) {
            // TODO(verify): also check `azp` (authorized party). Clerk docs say `azp` may be missing when the
            // request has no Origin header (the native app case), so it is not required for now.
            // Checks the signature (RS256 via JWKS), `iss` and `exp`/`nbf` with a small leeway.
            verifier(jwkProvider, issuer) { acceptLeeway(LEEWAY_SECONDS) }
            validate { credential ->
                credential.payload.subject
                    ?.takeIf { it.isNotBlank() }
                    ?.let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiError("unauthorized", "Missing or invalid token"))
            }
        }
    }
}
