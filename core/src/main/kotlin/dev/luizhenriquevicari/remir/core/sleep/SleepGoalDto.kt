package dev.luizhenriquevicari.remir.core.sleep

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * The user's sleep goal. Each part is optional, but at least one is set. The two times of day are local
 * (no time zone): "sleep at 23:00" means 23:00 wherever the user is.
 */
@Serializable
data class SleepGoalDto(
    val bedtime: LocalTime?,
    val wakeUpTime: LocalTime?,
    /** Wanted time asleep, in minutes. */
    val sleepMinutes: Int?,
    val updatedAt: Instant,
)
