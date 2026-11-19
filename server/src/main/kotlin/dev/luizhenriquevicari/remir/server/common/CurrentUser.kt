package dev.luizhenriquevicari.remir.server.common

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

/** Token claim that carries the Clerk public metadata (configured in the Clerk session token template). */
private const val METADATA_CLAIM = "metadata"
private const val ADMIN_ROLE = "admin"

/**
 * `sub` of the Clerk token (`user_...` format, NOT a UUID).
 * Only works inside `authenticate(CLERK_AUTH) { }`.
 */
fun ApplicationCall.userId(): String =
    principal<JWTPrincipal>()?.subject ?: error("userId() called outside authenticate(CLERK_AUTH)")

/** True when the token's `metadata.role` is `admin`. Only works inside `authenticate(CLERK_AUTH) { }`. */
fun ApplicationCall.isAdmin(): Boolean =
    principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(METADATA_CLAIM)
        ?.asMap()
        ?.get("role") == ADMIN_ROLE

/** Throws [Forbidden] unless the caller is an admin. */
fun ApplicationCall.requireAdmin() {
    if (!isAdmin()) throw Forbidden("Admin role required")
}
