package dev.luizhenriquevicari.remir.app.features.sleep

import java.time.Duration
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinDuration
import kotlin.time.Duration as KotlinDuration

/** A sleep goal that asks for more sleep than fits between going to bed and waking up. */
data class SleepGoalMismatch(
    val bedtime: LocalTime,
    val wakeUpTime: LocalTime,
    val timeInBed: KotlinDuration,
    val sleep: KotlinDuration,
)

/**
 * Only when all three parts are set, and only when the sleep duration does not fit between bedtime and wake up
 * time (crossing midnight when needed). Sleeping less than the time in bed is normal, since it takes a while to
 * fall asleep, so that is not flagged. The goal can still be saved: this is a warning, not a validation rule.
 */
fun checkSleepGoalFits(
    bedtime: LocalTime?,
    wakeUpTime: LocalTime?,
    sleepMinutes: Int?,
): SleepGoalMismatch? {
    if (bedtime == null || wakeUpTime == null || sleepMinutes == null) return null
    val between = Duration.between(bedtime, wakeUpTime)
    // Waking up "earlier" on the clock than going to bed means the next day.
    val timeInBed = (if (between.isNegative || between.isZero) between.plusDays(1) else between).toKotlinDuration()
    val sleep = sleepMinutes.minutes
    return SleepGoalMismatch(bedtime, wakeUpTime, timeInBed, sleep).takeIf { sleep > timeInBed }
}
