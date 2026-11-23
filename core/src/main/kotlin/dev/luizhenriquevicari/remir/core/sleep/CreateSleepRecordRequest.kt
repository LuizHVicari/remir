package dev.luizhenriquevicari.remir.core.sleep

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CreateSleepRecordRequest(
    val bedtime: Instant,
    val fellAsleepAt: Instant? = null,
    val wokeUpAt: Instant,
    val gotUpAt: Instant,
    /** How the user felt on waking up. Optional: a night may be logged without it. */
    val feeling: SleepFeeling? = null,
)
