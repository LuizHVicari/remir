package dev.luizhenriquevicari.remir.core.sleep

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * One night of sleep. [fellAsleepAt] is optional because the exact moment is hard to know; clients may leave it
 * out or send an estimate.
 */
@Serializable
data class SleepRecordDto(
    val id: Uuid,
    /** When the user went to bed. */
    val bedtime: Instant,
    /** When the user actually fell asleep, if known or estimated. */
    val fellAsleepAt: Instant?,
    /** When the user woke up. */
    val wokeUpAt: Instant,
    /** When the user got out of bed. */
    val gotUpAt: Instant,
    /** How the user felt on waking up, if they said. Defaults to null so a server that predates it still decodes. */
    val feeling: SleepFeeling? = null,
    val createdAt: Instant,
)
