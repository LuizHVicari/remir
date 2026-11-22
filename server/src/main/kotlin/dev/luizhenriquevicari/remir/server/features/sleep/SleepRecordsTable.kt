package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.server.features.profile.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** One row per night of sleep. */
object SleepRecordsTable : Table("sleep_records") {
    val id = uuid("id")
    val userId = varchar("user_id", 64).references(UsersTable.id).index()
    val bedtime = timestamp("bedtime")

    /** Null when the user did not know or estimate it. */
    val fellAsleepAt = timestamp("fell_asleep_at").nullable()
    val wokeUpAt = timestamp("woke_up_at")
    val gotUpAt = timestamp("got_up_at")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
