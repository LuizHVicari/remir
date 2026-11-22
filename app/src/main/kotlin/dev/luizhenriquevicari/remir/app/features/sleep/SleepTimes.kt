package dev.luizhenriquevicari.remir.app.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toKotlinInstant

/**
 * The user picks a date and time for going to bed and only a time of day for the rest. Each later time lands on
 * the first moment at or after the previous one, so "11 pm" then "7 am" crosses midnight on its own.
 */
fun buildSleepRequest(
    bedDate: LocalDate,
    bedTime: LocalTime,
    fellAsleepTime: LocalTime?,
    wokeUpTime: LocalTime,
    gotUpTime: LocalTime,
    zone: ZoneId = ZoneId.systemDefault(),
): CreateSleepRecordRequest {
    val bedtime = bedDate.atTime(bedTime).atZone(zone)
    val fellAsleep = fellAsleepTime?.let { bedtime.nextAt(it) }
    val wokeUp = (fellAsleep ?: bedtime).nextAt(wokeUpTime)
    val gotUp = wokeUp.nextAt(gotUpTime)
    return CreateSleepRecordRequest(
        bedtime = bedtime.toInstant().toKotlinInstant(),
        fellAsleepAt = fellAsleep?.toInstant()?.toKotlinInstant(),
        wokeUpAt = wokeUp.toInstant().toKotlinInstant(),
        gotUpAt = gotUp.toInstant().toKotlinInstant(),
    )
}

/** First moment at [time] that is not before this one: the same day when possible, the next day otherwise. */
private fun ZonedDateTime.nextAt(time: LocalTime): ZonedDateTime {
    val sameDay = with(time)
    return if (sameDay.isBefore(this)) plusDays(1).with(time) else sameDay
}

/** Time actually asleep: from falling asleep (or going to bed, when unknown) until waking up. */
val SleepRecordDto.timeAsleep: Duration
    get() = (wokeUpAt - (fellAsleepAt ?: bedtime)).coerceAtLeast(0.milliseconds)
