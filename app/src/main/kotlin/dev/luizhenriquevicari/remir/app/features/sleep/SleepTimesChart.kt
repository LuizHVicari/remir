package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
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
import dev.luizhenriquevicari.remir.app.common.toLocalizedDate
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import java.time.LocalDate
import kotlin.math.roundToInt

private const val CHART_HEIGHT_DP = 140
private const val TOOLTIP_LINES = 2

/**
 * Bedtime and wake up time as two plain lines, with no vertical axes or grid: what matters is the trend, and
 * pressing the chart shows the exact day and times in a tooltip, with a dot on each line. Bedtimes (about 11 pm)
 * and wake up times (about 7 am) are far apart on the clock, so each line is drawn on its own scale. The icons of
 * the averages above carry the lines' colors, so there is no legend.
 */
@Composable
internal fun SleepTimesChart(
    summary: SleepSummary,
    period: SummaryPeriod,
    bedtimeColor: Color,
    wakeUpColor: Color,
) {
    // Vico cannot draw a series with no points.
    if (summary.bedtimePoints.isEmpty()) return
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
            chart = rememberChart(summary, period, bedtimeColor, wakeUpColor),
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
private fun rememberChart(
    summary: SleepSummary,
    period: SummaryPeriod,
    bedtimeColor: Color,
    wakeUpColor: Color,
): CartesianChart =
    rememberCartesianChart(
        lineLayer(bedtimeColor, hourRange(summary.bedtimePoints), Axis.Position.Vertical.Start),
        lineLayer(wakeUpColor, hourRange(summary.wakeUpPoints), Axis.Position.Vertical.End),
        // Only the days, no axis line, ticks or grid.
        bottomAxis =
            HorizontalAxis.rememberBottom(
                line = null,
                tick = null,
                guideline = null,
                valueFormatter = CartesianValueFormatter { _, value, _ -> dayLabel(period, value.toLong()) },
                itemPlacer =
                    remember(
                        period,
                    ) { HorizontalAxis.ItemPlacer.aligned(spacing = { period.labelSpacingDays }) },
            ),
        marker = rememberTooltip(),
        // One x step is one day, so the gaps between nights show up as gaps in the line.
        getXStep = { _, _, _ -> 1.0 },
    )

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
 * Shown while pressing the chart: the day and "bedtime → wake up", in the inverse colors so it stands out from the
 * card. Each line gets a dot in its color, which also tells the arrow's two times apart.
 */
@Composable
private fun rememberTooltip(): DefaultCartesianMarker {
    val formatter = remember { DefaultCartesianMarker.ValueFormatter { _, targets -> tooltipText(targets) } }
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
        // Next to the points, so the chart does not reserve an empty band at the top of the card for it.
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        indicator = { color -> ShapeComponent(fill = Fill(color), shape = CircleShape) },
        indicatorSize = 10.dp,
    )
}

private fun tooltipText(targets: List<CartesianMarker.Target>): CharSequence {
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
    val bedtime = minutesOf(0)?.let { bedtimeFromAxis(it).toLocalizedTime() }
    val wakeUp = minutesOf(1)?.let { timeOfDay(it).toLocalizedTime() }
    return listOfNotNull(day, listOfNotNull(bedtime, wakeUp).joinToString(" → ").ifEmpty { null }).joinToString("\n")
}
