package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.toLocalizedDate
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import java.time.LocalDate
import kotlin.math.roundToInt

private const val CHART_HEIGHT_DP = 210

/** What the tooltip says for each line, in the order of the chart's layers (bedtime first). */
private class Labels(
    val bedtime: String,
    val wakeUp: String,
    val formatBedtime: (Int) -> String,
    val formatWakeUp: (Int) -> String,
)

/**
 * Bedtime and wake up time on one line chart. Both are times of day but at opposite ends of it (about 11 pm and
 * 7 am), so each line has its own vertical axis (bedtime on the left, wake up on the right) and neither ends up
 * flat. Pressing the chart shows a tooltip with the day and both times, and a dot on each line.
 * It fits the card's width instead of scrolling, even for a year.
 */
@Composable
internal fun SleepTimesChart(
    summary: SleepSummary,
    period: SummaryPeriod,
    bedtimeColor: Color,
    wakeUpColor: Color,
) {
    val labels =
        Labels(
            bedtime = stringResource(R.string.sleep_chart_bedtime),
            wakeUp = stringResource(R.string.sleep_chart_wake_up),
            formatBedtime = { bedtimeFromAxis(it).toLocalizedTime() },
            formatWakeUp = { timeOfDay(it).toLocalizedTime() },
        )
    // The legend sits in the band at the top of the chart that Vico reserves for the tooltip. It is drawn first, so
    // the tooltip (and the chart, which is transparent) goes over it while the user presses the chart.
    Box {
        Legend(labels, bedtimeColor, wakeUpColor, Modifier.align(Alignment.TopStart))
        // Vico cannot draw a series with no points.
        if (summary.bedtimePoints.isNotEmpty()) ChartHost(summary, period, labels, bedtimeColor, wakeUpColor)
    }
}

@Composable
private fun Legend(
    labels: Labels,
    bedtimeColor: Color,
    wakeUpColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem(labels.bedtime, bedtimeColor)
        LegendItem(labels.wakeUp, wakeUpColor)
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ChartHost(
    summary: SleepSummary,
    period: SummaryPeriod,
    labels: Labels,
    bedtimeColor: Color,
    wakeUpColor: Color,
) {
    ProvideVicoTheme(rememberM3VicoTheme()) {
        // The points are plain data, so the model is built directly instead of through a model producer.
        val model =
            remember(summary) {
                CartesianChartModel(
                    LineCartesianLayerModel.build { seriesOf(summary.bedtimePoints) },
                    LineCartesianLayerModel.build { seriesOf(summary.wakeUpPoints) },
                )
            }
        CartesianChartHost(
            chart = rememberSleepTimesChart(summary, period, labels, bedtimeColor, wakeUpColor),
            model = model,
            modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
        )
    }
}

private fun LineCartesianLayerModel.BuilderScope.seriesOf(points: List<ChartPoint>) =
    series(x = points.map { it.day }, y = points.map { it.minutes })

@Composable
private fun rememberSleepTimesChart(
    summary: SleepSummary,
    period: SummaryPeriod,
    labels: Labels,
    bedtimeColor: Color,
    wakeUpColor: Color,
): CartesianChart {
    val bedtimeRange = hourRange(summary.bedtimePoints)
    val wakeUpRange = hourRange(summary.wakeUpPoints)
    return rememberCartesianChart(
        lineLayer(bedtimeColor, bedtimeRange, Axis.Position.Vertical.Start),
        lineLayer(wakeUpColor, wakeUpRange, Axis.Position.Vertical.End),
        startAxis =
            VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(TextStyle(color = bedtimeColor, fontSize = 11.sp)),
                valueFormatter = CartesianValueFormatter { _, value, _ -> labels.formatBedtime(value.roundToInt()) },
                itemPlacer = remember(bedtimeRange) { hourlyItemPlacer(bedtimeRange) },
            ),
        endAxis =
            VerticalAxis.rememberEnd(
                label = rememberAxisLabelComponent(TextStyle(color = wakeUpColor, fontSize = 11.sp)),
                valueFormatter = CartesianValueFormatter { _, value, _ -> labels.formatWakeUp(value.roundToInt()) },
                // The left axis already draws the horizontal guidelines; a second set would clutter the chart.
                guideline = null,
                itemPlacer = remember(wakeUpRange) { hourlyItemPlacer(wakeUpRange) },
            ),
        bottomAxis =
            HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ -> dayLabel(period, value.toLong()) },
                itemPlacer =
                    remember(
                        period,
                    ) { HorizontalAxis.ItemPlacer.aligned(spacing = { period.labelSpacingDays }) },
            ),
        marker = rememberTooltip(labels),
        // One x step is one day, so the gaps between nights show up as gaps in the line.
        getXStep = { _, _, _ -> 1.0 },
    )
}

@Composable
private fun lineLayer(
    color: Color,
    range: IntRange,
    axis: Axis.Position.Vertical,
): LineCartesianLayer =
    rememberLineCartesianLayer(
        lineProvider =
            LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(color))),
            ),
        rangeProvider =
            remember(range) {
                CartesianLayerRangeProvider.fixed(minY = range.first.toDouble(), maxY = range.last.toDouble())
            },
        verticalAxisPosition = axis,
    )

/**
 * The tooltip shown while pressing the chart: the day and both times, in the inverse colors so it stands out from
 * the card, with a dot on each line in that line's color and a vertical guide at the day.
 */
@Composable
private fun rememberTooltip(labels: Labels): DefaultCartesianMarker {
    val formatter =
        remember(labels) {
            DefaultCartesianMarker.ValueFormatter { _, targets -> tooltipText(labels, targets) }
        }
    return rememberDefaultCartesianMarker(
        label =
            rememberTextComponent(
                style = TextStyle(color = MaterialTheme.colorScheme.inverseOnSurface, fontSize = 12.sp),
                lineCount = TOOLTIP_LINES,
                padding = Insets(8.dp, 6.dp, 8.dp, 6.dp),
                background =
                    rememberShapeComponent(
                        fill = Fill(MaterialTheme.colorScheme.inverseSurface),
                        shape = RoundedCornerShape(8.dp),
                    ),
            ),
        valueFormatter = formatter,
        // At the top, in a band the chart reserves for it, so the tooltip never covers a dot (next to the points it
        // would, whenever the highest one is too close to the top).
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
        indicator = { color -> ShapeComponent(fill = Fill(color), shape = CircleShape) },
        indicatorSize = 10.dp,
        guideline = rememberAxisGuidelineComponent(),
    )
}

private const val TOOLTIP_LINES = 3

private fun tooltipText(
    labels: Labels,
    targets: List<CartesianMarker.Target>,
): CharSequence {
    val lines = targets.filterIsInstance<LineCartesianLayerMarkerTarget>()
    val day = targets.firstOrNull()?.let { LocalDate.ofEpochDay(it.x.toLong()).toLocalizedDate() }.orEmpty()

    // One target per layer, in layer order: bedtime first, then wake up.
    fun minutesOf(layer: Int) =
        lines
            .getOrNull(layer)
            ?.points
            ?.firstOrNull()
            ?.entry
            ?.y
            ?.roundToInt()
    return listOfNotNull(
        day,
        minutesOf(0)?.let { "${labels.bedtime}: ${labels.formatBedtime(it)}" },
        minutesOf(1)?.let { "${labels.wakeUp}: ${labels.formatWakeUp(it)}" },
    ).joinToString("\n")
}
