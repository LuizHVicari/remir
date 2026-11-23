package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.annotation.StringRes
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.time.toJavaInstant

/**
 * The periods the summary can cover, counted back from now. [labelSpacingDays] is how many days apart the labels
 * along a chart's bottom axis are.
 */
enum class SummaryPeriod(
    val days: Int,
    val labelSpacingDays: Int,
    @StringRes val label: Int,
) {
    Week(days = 7, labelSpacingDays = 1, label = R.string.sleep_period_week),
    Month(days = 30, labelSpacingDays = 7, label = R.string.sleep_period_month),
    ThreeMonths(days = 90, labelSpacingDays = 14, label = R.string.sleep_period_three_months),
    Year(days = 365, labelSpacingDays = 30, label = R.string.sleep_period_year),
}

/** One point of a chart: the day the night ended (epoch day) and a time of day in minutes. */
data class ChartPoint(
    val day: Long,
    val minutes: Int,
)

/**
 * What the summary card shows for a period. [bedtimePoints] are in minutes on the bedtime axis (see
 * [toBedtimeAxis]); [wakeUpPoints] are minutes since midnight.
 */
data class SleepSummary(
    val nights: Int = 0,
    val averageBedtime: LocalTime? = null,
    val averageWakeUpTime: LocalTime? = null,
    val bedtimePoints: List<ChartPoint> = emptyList(),
    val wakeUpPoints: List<ChartPoint> = emptyList(),
)

private const val MINUTES_PER_DAY = 1440
private const val NOON_MINUTES = 720
private const val SECONDS_PER_MINUTE = 60L

/**
 * Bedtimes cluster around midnight, so 23:30 and 00:30 would look 23 hours apart (and average to noon) on a plain
 * midnight-based scale. Counting minutes from noon puts the whole evening-to-early-morning range side by side.
 */
fun LocalTime.toBedtimeAxis(): Int = (minutesOfDay() - NOON_MINUTES + MINUTES_PER_DAY) % MINUTES_PER_DAY

fun bedtimeFromAxis(minutes: Int): LocalTime = timeOfDay((minutes + NOON_MINUTES) % MINUTES_PER_DAY)

fun timeOfDay(minutes: Int): LocalTime =
    LocalTime.ofSecondOfDay(
        minutes.coerceIn(0, MINUTES_PER_DAY - 1) * SECONDS_PER_MINUTE,
    )

private fun LocalTime.minutesOfDay() = toSecondOfDay() / SECONDS_PER_MINUTE.toInt()

/**
 * Times of day are in the device's zone, since "I went to bed at 11 pm" means 11 pm where the user was. A night
 * belongs to the day it ended, like on the cards. Several nights on the same day are averaged into one point.
 */
fun summarize(
    records: List<SleepRecordDto>,
    zone: ZoneId = ZoneId.systemDefault(),
): SleepSummary {
    if (records.isEmpty()) return SleepSummary()
    val nights =
        records.map {
            val wokeUp = it.wokeUpAt.toJavaInstant().atZone(zone)
            Night(
                day = wokeUp.toLocalDate().toEpochDay(),
                bedtime =
                    it.bedtime
                        .toJavaInstant()
                        .atZone(zone)
                        .toLocalTime()
                        .toBedtimeAxis(),
                wakeUp = wokeUp.toLocalTime().minutesOfDay(),
            )
        }
    return SleepSummary(
        nights = nights.size,
        averageBedtime = bedtimeFromAxis(nights.map { it.bedtime }.average().roundToInt()),
        averageWakeUpTime = timeOfDay(nights.map { it.wakeUp }.average().roundToInt()),
        bedtimePoints = nights.pointsBy { it.bedtime },
        wakeUpPoints = nights.pointsBy { it.wakeUp },
    )
}

private class Night(
    val day: Long,
    val bedtime: Int,
    val wakeUp: Int,
)

private fun List<Night>.pointsBy(minutes: (Night) -> Int): List<ChartPoint> =
    groupBy { it.day }
        .map { (day, onThatDay) -> ChartPoint(day, onThatDay.map(minutes).average().roundToInt()) }
        .sortedBy { it.day }
