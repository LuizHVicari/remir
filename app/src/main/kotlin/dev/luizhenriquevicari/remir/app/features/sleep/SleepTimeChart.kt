package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private const val MINUTES_PER_HOUR = 60
private const val MAX_HOURLY_RANGE_HOURS = 6
private const val CHART_HEIGHT_DP = 150

/**
 * One line chart: a point per day, the time of day on the vertical axis (in whole hours, so the labels stay
 * readable) and the days along the bottom. It fits the card's width instead of scrolling, even for a year.
 */
@Composable
internal fun SleepTimeChart(
    title: String,
    points: List<ChartPoint>,
    period: SummaryPeriod,
    formatMinutes: (Int) -> String,
    lineColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        // Vico cannot draw a series with no points.
        if (points.isNotEmpty()) TimeChartHost(points, period, formatMinutes, lineColor)
    }
}

@Composable
private fun TimeChartHost(
    points: List<ChartPoint>,
    period: SummaryPeriod,
    formatMinutes: (Int) -> String,
    lineColor: Color,
) {
    ProvideVicoTheme(rememberM3VicoTheme()) {
        // The points are plain data, so the model is built directly instead of through a model producer. That
        // also lets the chart draw in previews, where a producer's asynchronous updates never run.
        val model =
            remember(points) {
                CartesianChartModel(
                    LineCartesianLayerModel.build { series(x = points.map { it.day }, y = points.map { it.minutes }) },
                )
            }
        CartesianChartHost(
            chart = rememberTimeChart(points, period, formatMinutes, lineColor),
            model = model,
            modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
        )
    }
}

@Composable
private fun rememberTimeChart(
    points: List<ChartPoint>,
    period: SummaryPeriod,
    formatMinutes: (Int) -> String,
    lineColor: Color,
): CartesianChart {
    val range = hourRange(points)
    val line = LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(lineColor)))
    return rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(line),
            rangeProvider =
                remember(range) {
                    CartesianLayerRangeProvider.fixed(minY = range.first.toDouble(), maxY = range.last.toDouble())
                },
        ),
        startAxis =
            VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, value, _ -> formatMinutes(value.roundToInt()) },
                itemPlacer = remember(range) { VerticalAxis.ItemPlacer.step({ stepMinutes(range).toDouble() }) },
            ),
        bottomAxis =
            HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ -> dayLabel(period, value.toLong()) },
                itemPlacer =
                    remember(
                        period,
                    ) { HorizontalAxis.ItemPlacer.aligned(spacing = { period.labelSpacingDays }) },
            ),
        // One x step is one day, so the gaps between nights show up as gaps in the line.
        getXStep = { _, _, _ -> 1.0 },
    )
}

/** The vertical range, widened to whole hours and at least one hour tall, so the line never sits on an edge. */
private fun hourRange(points: List<ChartPoint>): IntRange {
    val low = points.minOf { it.minutes } / MINUTES_PER_HOUR * MINUTES_PER_HOUR
    val high = (points.maxOf { it.minutes } + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR * MINUTES_PER_HOUR
    return low..maxOf(high, low + MINUTES_PER_HOUR)
}

/** Hourly labels, or every two hours when the range is tall enough for them to crowd each other. */
private fun stepMinutes(range: IntRange): Int =
    if (range.last - range.first > MAX_HOURLY_RANGE_HOURS * MINUTES_PER_HOUR) 2 * MINUTES_PER_HOUR else MINUTES_PER_HOUR

private fun dayLabel(
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
