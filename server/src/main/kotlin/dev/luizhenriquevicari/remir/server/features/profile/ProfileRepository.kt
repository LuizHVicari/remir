package dev.luizhenriquevicari.remir.server.features.profile

import dev.luizhenriquevicari.remir.core.profile.ProfileDto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Instant

/** Exposed DSL only. Runs inside `dbQuery` (opened by the Service). Returns `core` DTOs. */
class ProfileRepository {
    /** Creates the user when missing (`ON CONFLICT DO NOTHING`). */
    fun provision(
        id: String,
        now: Instant,
    ) {
        UsersTable.insertIgnore {
            it[UsersTable.id] = id
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    fun find(id: String): ProfileDto? =
        UsersTable
            .selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toProfileDto()

    /** @return number of updated rows. */
    fun updateDisplayName(
        id: String,
        displayName: String,
        now: Instant,
    ): Int =
        UsersTable.update({ UsersTable.id eq id }) {
            it[UsersTable.displayName] = displayName
            it[updatedAt] = now
        }

    private fun ResultRow.toProfileDto() =
        ProfileDto(
            id = this[UsersTable.id],
            displayName = this[UsersTable.displayName],
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt],
        )
}
