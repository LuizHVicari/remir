package dev.luizhenriquevicari.remir.core.legal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Legal documents the user must accept. `slug` is the value used in URLs and in the database. */
@Serializable
enum class LegalDocumentType(val slug: String) {
    @SerialName("privacy")
    Privacy("privacy"),

    @SerialName("terms")
    Terms("terms"),

    ;

    companion object {
        fun fromSlug(slug: String): LegalDocumentType? = entries.firstOrNull { it.slug == slug }
    }
}

/** One immutable version of a legal document. `content` is Markdown. */
@Serializable
data class LegalDocumentDto(
    val type: LegalDocumentType,
    val version: Int,
    val content: String,
    /** When false, users who accepted an earlier version are not asked again (e.g. a typo fix). */
    val requiresAcceptance: Boolean,
    val effectiveAt: Instant,
)

/** Documents the current user still has to accept. Empty when nothing is pending. */
@Serializable
data class LegalStatusDto(
    val pending: List<LegalDocumentDto>,
)
