package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.SleepFeeling
import dev.luizhenriquevicari.remir.server.db.timestamptz
import dev.luizhenriquevicari.remir.server.features.profile.UsersTable
import org.jetbrains.exposed.v1.core.Table

/** One row per night of sleep. */
object SleepRecordsTable : Table("sleep_records") {
    val id = uuid("id")
    val userId = varchar("user_id", 64).references(UsersTable.id).index()
    val bedtime = timestamptz("bedtime")

    /** Null when the user did not know or estimate it. */
    val fellAsleepAt = timestamptz("fell_asleep_at").nullable()
    val wokeUpAt = timestamptz("woke_up_at")
    val gotUpAt = timestamptz("got_up_at")

    /** How the user felt on waking up, stored by name. Null when they did not say. */
    val feeling = enumerationByName<SleepFeeling>("feeling", 16).nullable()
    val createdAt = timestamptz("created_at")

    override val primaryKey = PrimaryKey(id)
}
