package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Instant

/** Exposed DSL only. Runs inside `dbQuery` (opened by the Service). Returns `core` DTOs. */
class LegalRepository {
    fun latest(type: LegalDocumentType): LegalDocumentDto? =
        LegalDocumentsTable
            .selectAll()
            .where { LegalDocumentsTable.type eq type.slug }
            .orderBy(LegalDocumentsTable.version, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.toDto(type)

    /** Highest version that asks users to accept again, or null when there is none. */
    fun latestRequiringAcceptance(type: LegalDocumentType): Int? {
        val maxVersion = LegalDocumentsTable.version.max()
        return LegalDocumentsTable
            .select(maxVersion)
            .where { (LegalDocumentsTable.type eq type.slug) and (LegalDocumentsTable.requiresAcceptance eq true) }
            .single()[maxVersion]
    }

    fun insert(
        type: LegalDocumentType,
        content: String,
        requiresAcceptance: Boolean,
        publishedBy: String,
        now: Instant,
    ) {
        val maxVersion = LegalDocumentsTable.version.max()
        val current =
            LegalDocumentsTable
                .select(maxVersion)
                .where { LegalDocumentsTable.type eq type.slug }
                .single()[maxVersion] ?: 0
        LegalDocumentsTable.insert {
            it[LegalDocumentsTable.type] = type.slug
            it[version] = current + 1
            it[LegalDocumentsTable.content] = content
            it[LegalDocumentsTable.requiresAcceptance] = requiresAcceptance
            it[effectiveAt] = now
            it[LegalDocumentsTable.publishedBy] = publishedBy
        }
    }

    /** Highest version of [type] the user accepted, or null if they never accepted any. */
    fun acceptedVersion(
        userId: String,
        type: LegalDocumentType,
    ): Int? {
        val maxVersion = LegalAcceptancesTable.version.max()
        return LegalAcceptancesTable
            .select(maxVersion)
            .where { (LegalAcceptancesTable.userId eq userId) and (LegalAcceptancesTable.type eq type.slug) }
            .single()[maxVersion]
    }

    /** Idempotent: accepting the same version twice keeps the first timestamp. */
    fun accept(
        userId: String,
        type: LegalDocumentType,
        version: Int,
        now: Instant,
    ) {
        LegalAcceptancesTable.insertIgnore {
            it[LegalAcceptancesTable.userId] = userId
            it[LegalAcceptancesTable.type] = type.slug
            it[LegalAcceptancesTable.version] = version
            it[acceptedAt] = now
        }
    }

    private fun ResultRow.toDto(type: LegalDocumentType) =
        LegalDocumentDto(
            type = type,
            version = this[LegalDocumentsTable.version],
            content = this[LegalDocumentsTable.content],
            requiresAcceptance = this[LegalDocumentsTable.requiresAcceptance],
            effectiveAt = this[LegalDocumentsTable.effectiveAt],
        )
}
