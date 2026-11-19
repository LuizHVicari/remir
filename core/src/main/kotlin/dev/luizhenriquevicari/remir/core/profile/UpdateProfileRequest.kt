package dev.luizhenriquevicari.remir.core.profile

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val displayName: String,
)
