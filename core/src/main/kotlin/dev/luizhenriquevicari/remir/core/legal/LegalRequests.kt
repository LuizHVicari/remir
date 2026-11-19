package dev.luizhenriquevicari.remir.core.legal

import kotlinx.serialization.Serializable

/** Admin publishes a new version. Editing a document always creates a new version. */
@Serializable
data class PublishLegalDocumentRequest(
    val content: String,
    val requiresAcceptance: Boolean,
)

/** The user accepts a specific version (the one the app showed). */
@Serializable
data class AcceptLegalDocumentRequest(
    val version: Int,
)
