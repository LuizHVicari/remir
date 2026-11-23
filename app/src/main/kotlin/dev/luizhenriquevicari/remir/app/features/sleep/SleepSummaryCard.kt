package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import java.time.ZoneOffset
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val LOADING_HEIGHT_DP = 120

/**
 * The summary of a period: averages of when the user goes to bed and wakes up, and a line chart of each. Sits
 * below the goal card. [loading] replaces the content with a spinner while a newly picked period is fetched.
 */
@Composable
fun SleepSummaryCard(
    summary: SleepSummary,
    period: SummaryPeriod,
    loading: Boolean,
    onPeriodChange: (SummaryPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.sleep_summary_title), style = MaterialTheme.typography.titleLarge)
            PeriodChips(period, onPeriodChange)
            when {
                loading ->
                    Box(Modifier.fillMaxWidth().height(LOADING_HEIGHT_DP.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                summary.nights == 0 ->
                    Text(stringResource(R.string.sleep_summary_empty), style = MaterialTheme.typography.bodyMedium)
                else -> SummaryContent(summary, period)
            }
        }
    }
}

/** Scrolls sideways when the chips do not fit, which happens with large text or on narrow screens. */
@Composable
private fun PeriodChips(
    selected: SummaryPeriod,
    onSelect: (SummaryPeriod) -> Unit,
) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryPeriod.entries.forEach { period ->
            FilterChip(
                selected = period == selected,
                onClick = { onSelect(period) },
                label = { Text(stringResource(period.label)) },
            )
        }
    }
}

@Composable
private fun SummaryContent(
    summary: SleepSummary,
    period: SummaryPeriod,
) {
    val onCard = MaterialTheme.colorScheme.onSurface
    // The icons of the averages are tinted like the chart lines, which is what tells the lines apart.
    val bedtimeColor = MaterialTheme.colorScheme.primary
    val wakeUpColor = MaterialTheme.colorScheme.tertiary
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LabeledValue(
            icon = Icons.Filled.Bedtime,
            label = stringResource(R.string.sleep_summary_avg_bedtime),
            value = summary.averageBedtime?.toLocalizedTime(),
            placeholder = stringResource(R.string.sleep_goal_unset_time),
            contentColor = onCard,
            iconTint = bedtimeColor,
        )
        LabeledValue(
            icon = Icons.Filled.WbSunny,
            label = stringResource(R.string.sleep_summary_avg_wake_up),
            value = summary.averageWakeUpTime?.toLocalizedTime(),
            placeholder = stringResource(R.string.sleep_goal_unset_time),
            contentColor = onCard,
            iconTint = wakeUpColor,
        )
    }
    SleepTimesChart(summary = summary, period = period, bedtimeColor = bedtimeColor, wakeUpColor = wakeUpColor)
}

private fun sampleNight(
    bedtime: String,
    wokeUpAt: String,
): SleepRecordDto {
    val woke = Instant.parse(wokeUpAt)
    return SleepRecordDto(Uuid.random(), Instant.parse(bedtime), null, woke, woke, null, woke)
}

/** About 11 pm to 7 am UTC, shifted a little each night. */
private val sampleRecords =
    listOf(
        sampleNight("2026-03-07T23:00:00Z", "2026-03-08T07:00:00Z"),
        sampleNight("2026-03-08T23:30:00Z", "2026-03-09T07:15:00Z"),
        sampleNight("2026-03-09T22:45:00Z", "2026-03-10T06:50:00Z"),
        sampleNight("2026-03-10T23:45:00Z", "2026-03-11T07:20:00Z"),
        sampleNight("2026-03-11T23:00:00Z", "2026-03-12T07:05:00Z"),
        sampleNight("2026-03-13T00:00:00Z", "2026-03-13T07:30:00Z"),
        sampleNight("2026-03-13T23:15:00Z", "2026-03-14T07:00:00Z"),
    )

@PreviewLightDark
@Composable
private fun SleepSummaryCardPreview() {
    RemirPreview {
        SleepSummaryCard(
            summary = summarize(sampleRecords, ZoneOffset.UTC),
            period = SummaryPeriod.Week,
            loading = false,
            onPeriodChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepSummaryCardEmptyPreview() {
    RemirPreview {
        SleepSummaryCard(
            summary = SleepSummary(),
            period = SummaryPeriod.Month,
            loading = false,
            onPeriodChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepSummaryCardLoadingPreview() {
    RemirPreview {
        SleepSummaryCard(
            summary = SleepSummary(),
            period = SummaryPeriod.Year,
            loading = true,
            onPeriodChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
