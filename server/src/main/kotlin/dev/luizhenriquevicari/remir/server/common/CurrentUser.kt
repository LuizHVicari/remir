package dev.luizhenriquevicari.remir.server.common

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

/**
 * `sub` of the Clerk token (`user_...` format, NOT a UUID).
 * Only works inside `authenticate(CLERK_AUTH) { }`.
 */
fun ApplicationCall.userId(): String =
    principal<JWTPrincipal>()?.subject ?: error("userId() called outside authenticate(CLERK_AUTH)")
