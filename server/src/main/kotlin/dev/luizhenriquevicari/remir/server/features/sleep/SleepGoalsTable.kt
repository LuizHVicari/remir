package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.server.db.timestamptz
import dev.luizhenriquevicari.remir.server.features.profile.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.time

/** One row per user. Each part of the goal is optional (the core validator requires at least one). */
object SleepGoalsTable : Table("sleep_goals") {
    val userId = varchar("user_id", 64).references(UsersTable.id)

    /** Local time of day, no time zone: "sleep at 23:00" means 23:00 wherever the user is. */
    val bedtime = time("bedtime").nullable()
    val wakeUpTime = time("wake_up_time").nullable()
    val sleepMinutes = integer("sleep_minutes").nullable()
    val updatedAt = timestamptz("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
