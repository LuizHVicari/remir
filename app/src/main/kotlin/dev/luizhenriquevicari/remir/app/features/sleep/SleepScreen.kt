package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.navigation.TabScaffold
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration
import kotlin.time.Instant

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun SleepRoute(
    onOpenProfile: () -> Unit,
    onLogSleep: () -> Unit,
    onEditGoal: () -> Unit,
    onSeeAll: () -> Unit,
    onEditNight: (SleepRecordDto) -> Unit,
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
        onSeeAll = onSeeAll,
        onEditNight = onEditNight,
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
    onSeeAll: () -> Unit,
    onEditNight: (SleepRecordDto) -> Unit,
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
            else -> SleepList(state, onEditGoal, onSeeAll, onEditNight, onPeriodChange, modifier)
        }
    }
}

@Composable
private fun SleepList(
    state: SleepState,
    onEditGoal: () -> Unit,
    onSeeAll: () -> Unit,
    onEditNight: (SleepRecordDto) -> Unit,
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
        item { RecentNightsCard(records = state.records, onSeeAll = onSeeAll, onEdit = onEditNight) }
    }
}

@Composable
fun Duration.asHoursAndMinutes(): String =
    stringResource(R.string.sleep_duration, inWholeHours.toInt(), (inWholeMinutes % MINUTES_PER_HOUR).toInt())

private const val MINUTES_PER_HOUR = 60

private val sampleGoal =
    SleepGoalDto(LocalTime.parse("23:00"), LocalTime.parse("07:00"), 480, Instant.parse("2026-03-10T12:00:00Z"))

@PreviewLightDark
@Composable
private fun SleepScreenPreview() {
    RemirPreview {
        SleepScreen(
            SleepState(loading = false, records = sampleNights, goal = sampleGoal),
            onOpenProfile = {},
            onLogSleep = {},
            onEditGoal = {},
            onSeeAll = {},
            onEditNight = {},
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
            onSeeAll = {},
            onEditNight = {},
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
            onSeeAll = {},
            onEditNight = {},
            onPeriodChange = {},
            onRetry = {},
        )
    }
}
