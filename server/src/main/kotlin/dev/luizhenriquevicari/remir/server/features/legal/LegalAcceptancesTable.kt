package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.server.features.profile.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** Which version of which document each user accepted, and when. Kept as proof of consent. */
object LegalAcceptancesTable : Table("legal_acceptances") {
    val userId = varchar("user_id", 64).references(UsersTable.id).index()

    /** `LegalDocumentType.slug`. */
    val type = varchar("type", 20)
    val version = integer("version")
    val acceptedAt = timestamp("accepted_at")

    override val primaryKey = PrimaryKey(userId, type, version)
}
