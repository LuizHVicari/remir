package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.asString
import dev.luizhenriquevicari.remir.app.common.toLocalizedDate
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.navigation.TabScaffold
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun SleepRoute(
    onOpenProfile: () -> Unit,
    onLogSleep: () -> Unit,
    onEditGoal: () -> Unit,
    viewModel: SleepViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Also runs when coming back from the other screens, so a new night or goal shows up.
    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    SleepScreen(
        state = state,
        onOpenProfile = onOpenProfile,
        onLogSleep = onLogSleep,
        onEditGoal = onEditGoal,
        onPeriodChange = viewModel::onPeriodChange,
        onRetry = viewModel::retry,
    )
}

/** Stateless: state + lambdas only. */
@Composable
fun SleepScreen(
    state: SleepState,
    onOpenProfile: () -> Unit,
    onLogSleep: () -> Unit,
    onEditGoal: () -> Unit,
    onPeriodChange: (SummaryPeriod) -> Unit,
    onRetry: () -> Unit,
) {
    TabScaffold(
        title = stringResource(R.string.tab_sleep),
        onOpenProfile = onOpenProfile,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onLogSleep,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.sleep_log)) },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.loading -> Loading(modifier)
            state.error != null && state.records.isEmpty() -> ErrorState(state.error.asString(), modifier, onRetry)
            else -> SleepList(state, onEditGoal, onPeriodChange, modifier)
        }
    }
}

@Composable
private fun SleepList(
    state: SleepState,
    onEditGoal: () -> Unit,
    onPeriodChange: (SummaryPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Extra room at the bottom so the floating button never covers the last card.
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SleepGoalCard(goal = state.goal, onEdit = onEditGoal) }
        item {
            SleepSummaryCard(
                summary = state.summary,
                period = state.period,
                loading = state.summaryLoading,
                onPeriodChange = onPeriodChange,
            )
        }
        if (state.records.isEmpty()) {
            item { Text(stringResource(R.string.sleep_empty), style = MaterialTheme.typography.bodyLarge) }
        }
        items(state.records, key = { it.id }) { SleepRecordCard(it) }
    }
}

@Composable
private fun SleepRecordCard(record: SleepRecordDto) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(record.wokeUpAt.toLocalizedDate(), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.sleep_in_bed,
                    record.bedtime.toLocalizedTime(),
                    record.gotUpAt.toLocalizedTime(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.sleep_slept, record.timeAsleep.asHoursAndMinutes()),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun Duration.asHoursAndMinutes(): String =
    stringResource(R.string.sleep_duration, inWholeHours.toInt(), (inWholeMinutes % MINUTES_PER_HOUR).toInt())

private const val MINUTES_PER_HOUR = 60

private fun sampleRecord(
    bedtime: String,
    fellAsleepAt: String?,
    wokeUpAt: String,
    gotUpAt: String,
) = SleepRecordDto(
    id = Uuid.random(),
    bedtime = Instant.parse(bedtime),
    fellAsleepAt = fellAsleepAt?.let(Instant::parse),
    wokeUpAt = Instant.parse(wokeUpAt),
    gotUpAt = Instant.parse(gotUpAt),
    createdAt = Instant.parse(gotUpAt) + 1.minutes,
)

private val sampleRecords =
    listOf(
        sampleRecord("2026-03-10T23:00:00Z", "2026-03-10T23:30:00Z", "2026-03-11T07:00:00Z", "2026-03-11T07:15:00Z"),
        sampleRecord("2026-03-09T22:45:00Z", null, "2026-03-10T06:30:00Z", "2026-03-10T06:45:00Z"),
    )

private val sampleGoal =
    SleepGoalDto(LocalTime.parse("23:00"), LocalTime.parse("07:00"), 480, Instant.parse("2026-03-10T12:00:00Z"))

@PreviewLightDark
@Composable
private fun SleepScreenPreview() {
    RemirPreview {
        SleepScreen(
            SleepState(loading = false, records = sampleRecords, goal = sampleGoal),
            onOpenProfile = {},
            onLogSleep = {},
            onEditGoal = {},
            onPeriodChange = {},
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepScreenNoGoalPreview() {
    RemirPreview {
        SleepScreen(
            SleepState(loading = false, records = sampleRecords),
            onOpenProfile = {},
            onLogSleep = {},
            onEditGoal = {},
            onPeriodChange = {},
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepScreenEmptyPreview() {
    RemirPreview {
        SleepScreen(
            SleepState(loading = false),
            onOpenProfile = {},
            onLogSleep = {},
            onEditGoal = {},
            onPeriodChange = {},
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepScreenLoadingPreview() {
    RemirPreview {
        SleepScreen(
            SleepState(),
            onOpenProfile = {},
            onLogSleep = {},
            onEditGoal = {},
            onPeriodChange = {},
            onRetry = {},
        )
    }
}
