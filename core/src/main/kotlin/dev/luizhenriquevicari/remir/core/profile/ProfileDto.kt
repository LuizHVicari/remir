package dev.luizhenriquevicari.remir.core.profile

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Profile of the authenticated user. `id` is the Clerk `sub` (`user_...`), not a UUID. */
@Serializable
data class ProfileDto(
    val id: String,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    /** Computed by the server from the token's `metadata.role` claim; never stored, never read from the client. */
    val isAdmin: Boolean = false,
)
