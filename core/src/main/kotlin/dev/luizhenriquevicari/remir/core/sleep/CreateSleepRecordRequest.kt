package dev.luizhenriquevicari.remir.core.sleep

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CreateSleepRecordRequest(
    val bedtime: Instant,
    val fellAsleepAt: Instant? = null,
    val wokeUpAt: Instant,
    val gotUpAt: Instant,
)
