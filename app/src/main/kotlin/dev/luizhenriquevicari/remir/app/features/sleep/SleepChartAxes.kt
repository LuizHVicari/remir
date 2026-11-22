package dev.luizhenriquevicari.remir.app.features.sleep

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val MINUTES_PER_HOUR = 60

/**
 * The vertical range of a line: widened to whole hours, plus one more hour above the highest point, which is where
 * the tooltip goes. So it is always more than an hour tall, which also stops a line that barely varies from looking
 * like it swings wildly.
 */
internal fun hourRange(points: List<ChartPoint>): IntRange {
    val low = points.minOf { it.minutes } / MINUTES_PER_HOUR * MINUTES_PER_HOUR
    val high = (points.maxOf { it.minutes } + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR * MINUTES_PER_HOUR
    return low..(high + MINUTES_PER_HOUR)
}

/** The label of a day along the bottom axis, as short as the period allows. */
internal fun dayLabel(
    period: SummaryPeriod,
    epochDay: Long,
    locale: Locale = Locale.getDefault(),
): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return when (period) {
        SummaryPeriod.Week -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        SummaryPeriod.Month -> date.dayOfMonth.toString()
        SummaryPeriod.ThreeMonths -> DateTimeFormatter.ofPattern("d MMM", locale).format(date)
        SummaryPeriod.Year -> date.month.getDisplayName(TextStyle.SHORT, locale)
    }
}
