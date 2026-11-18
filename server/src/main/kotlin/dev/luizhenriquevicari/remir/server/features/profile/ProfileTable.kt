package dev.luizhenriquevicari.remir.server.features.profile

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Schema source of truth: after changing it, run `./gradlew :server:generateMigrations`
 * and commit the generated script.
 */
object UsersTable : Table("users") {
    /** Clerk `sub` (`user_...`). No password or credential data is ever stored. */
    val id = varchar("id", 64)
    val displayName = varchar("display_name", 60).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
