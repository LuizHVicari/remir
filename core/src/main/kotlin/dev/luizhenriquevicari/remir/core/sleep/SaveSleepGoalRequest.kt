package dev.luizhenriquevicari.remir.core.sleep

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/** Replaces the whole goal: a part sent as null (or left out) is cleared. At least one part must be set. */
@Serializable
data class SaveSleepGoalRequest(
    val bedtime: LocalTime? = null,
    val wakeUpTime: LocalTime? = null,
    val sleepMinutes: Int? = null,
)
