package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.server.db.timestamptz
import org.jetbrains.exposed.v1.core.Table

/**
 * Immutable versions of the legal documents (privacy policy, terms). Editing a document inserts a new
 * version; rows are never updated, so an acceptance always points at the exact text the user saw.
 * Schema source of truth: after changing it, run `./gradlew :server:generateMigrations`.
 */
object LegalDocumentsTable : Table("legal_documents") {
    /** `LegalDocumentType.slug`. */
    val type = varchar("type", 20)
    val version = integer("version")

    /** Markdown. */
    val content = text("content")
    val requiresAcceptance = bool("requires_acceptance")
    val effectiveAt = timestamptz("effective_at")

    /** Clerk `sub` of the admin who published it. */
    val publishedBy = varchar("published_by", 64)

    override val primaryKey = PrimaryKey(type, version)
}
