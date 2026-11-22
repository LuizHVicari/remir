package dev.luizhenriquevicari.remir.app.features.sleep

import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val MINUTES_PER_HOUR = 60
private const val MAX_HOURLY_RANGE_HOURS = 6

/** The vertical range, widened to whole hours and at least one hour tall, so the line never sits on an edge. */
internal fun hourRange(points: List<ChartPoint>): IntRange {
    val low = points.minOf { it.minutes } / MINUTES_PER_HOUR * MINUTES_PER_HOUR
    val high = (points.maxOf { it.minutes } + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR * MINUTES_PER_HOUR
    return low..maxOf(high, low + MINUTES_PER_HOUR)
}

/** Hourly labels, or every two hours when the range is tall enough for them to crowd each other. */
internal fun hourlyItemPlacer(range: IntRange): VerticalAxis.ItemPlacer {
    val step =
        if (range.last - range.first >
            MAX_HOURLY_RANGE_HOURS * MINUTES_PER_HOUR
        ) {
            2 * MINUTES_PER_HOUR
        } else {
            MINUTES_PER_HOUR
        }
    return VerticalAxis.ItemPlacer.step({ step.toDouble() })
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
