package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import dev.luizhenriquevicari.remir.core.legal.PublishLegalDocumentRequest
import dev.luizhenriquevicari.remir.server.common.Conflict
import dev.luizhenriquevicari.remir.server.common.NotFound
import dev.luizhenriquevicari.remir.server.db.dbQuery
import dev.luizhenriquevicari.remir.server.features.profile.ProfileRepository
import kotlin.time.Clock

/** Business rules + transaction boundary for legal documents and user acceptance. */
class LegalService(
    private val repository: LegalRepository,
    private val profileRepository: ProfileRepository,
    private val clock: Clock = Clock.System,
) {
    suspend fun current(type: LegalDocumentType): LegalDocumentDto =
        dbQuery {
            repository.latest(type) ?: throw NotFound("Document not published yet")
        }

    /** Editing means publishing a new version; older versions stay untouched. */
    suspend fun publish(
        adminId: String,
        type: LegalDocumentType,
        request: PublishLegalDocumentRequest,
    ): LegalDocumentDto =
        dbQuery {
            repository.insert(type, request.content, request.requiresAcceptance, adminId, clock.now())
            repository.latest(type) ?: throw NotFound("Document not published yet")
        }

    /** Latest version of each document the user has not accepted yet (only when it requires acceptance). */
    suspend fun pending(userId: String): List<LegalDocumentDto> =
        dbQuery {
            LegalDocumentType.entries.mapNotNull { pendingFor(userId, it) }
        }

    /** The user can only accept the current version, so a stale screen can't record consent to old text. */
    suspend fun accept(
        userId: String,
        type: LegalDocumentType,
        version: Int,
    ) = dbQuery {
        val now = clock.now()
        val latest = repository.latest(type) ?: throw NotFound("Document not published yet")
        if (version != latest.version) throw Conflict("A newer version is available")
        profileRepository.provision(userId, now)
        repository.accept(userId, type, version, now)
    }

    private fun pendingFor(
        userId: String,
        type: LegalDocumentType,
    ): LegalDocumentDto? {
        val latest = repository.latest(type) ?: return null
        val required = repository.latestRequiringAcceptance(type) ?: return null
        val accepted = repository.acceptedVersion(userId, type)
        return latest.takeIf { accepted == null || accepted < required }
    }
}
