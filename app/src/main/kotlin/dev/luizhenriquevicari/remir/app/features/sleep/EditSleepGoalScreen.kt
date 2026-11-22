package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.asString
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.designsystem.components.PickerField
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTimePickerDialog
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalValidation
import org.koin.compose.viewmodel.koinViewModel
import java.time.Duration
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes

/** Wires the ViewModel to the screen. Leaves the screen once the goal is saved. */
@Composable
fun EditSleepGoalRoute(
    onBack: () -> Unit,
    viewModel: EditSleepGoalViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    EditSleepGoalScreen(
        state = state,
        onBack = onBack,
        onBedtimeChange = viewModel::onBedtimeChange,
        onWakeUpTimeChange = viewModel::onWakeUpTimeChange,
        onSleepMinutesChange = viewModel::onSleepMinutesChange,
        onSave = viewModel::save,
        onSaveAnyway = viewModel::saveAnyway,
        onDismissMismatch = viewModel::dismissMismatch,
    )
}

/** Which picker dialog is open. Saved so it survives rotation. */
private enum class GoalPicker { Bedtime, WakeUp, Duration }

/** Stateless: state + lambdas only. The open dialog is the only local UI state. */
@Composable
fun EditSleepGoalScreen(
    state: EditSleepGoalState,
    onBack: () -> Unit,
    onBedtimeChange: (LocalTime?) -> Unit,
    onWakeUpTimeChange: (LocalTime?) -> Unit,
    onSleepMinutesChange: (Int?) -> Unit,
    onSave: () -> Unit,
    onSaveAnyway: () -> Unit,
    onDismissMismatch: () -> Unit,
) {
    var picker by rememberSaveable { mutableStateOf<GoalPicker?>(null) }
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.sleep_goal_edit_title), onBack = onBack) }) { padding ->
        if (state.loading) {
            Loading(Modifier.padding(padding))
            return@Scaffold
        }
        val notSet = stringResource(R.string.sleep_not_set)
        val wholeGoalError = state.fieldErrors[SleepGoalValidation.WHOLE_GOAL]
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GoalHint(wholeGoalError)
            PickerField(
                value = state.bedtime?.toLocalizedTime() ?: notSet,
                label = stringResource(R.string.sleep_goal_bedtime_field),
                onClick = { picker = GoalPicker.Bedtime },
                onClear = state.bedtime?.let { { onBedtimeChange(null) } },
            )
            PickerField(
                value = state.wakeUpTime?.toLocalizedTime() ?: notSet,
                label = stringResource(R.string.sleep_goal_wake_up_field),
                onClick = { picker = GoalPicker.WakeUp },
                onClear = state.wakeUpTime?.let { { onWakeUpTimeChange(null) } },
            )
            PickerField(
                value = state.sleepMinutes?.minutes?.asHoursAndMinutes() ?: notSet,
                label = stringResource(R.string.sleep_goal_duration_field),
                onClick = { picker = GoalPicker.Duration },
                error = state.fieldErrors["sleepMinutes"],
                onClear = state.sleepMinutes?.let { { onSleepMinutesChange(null) } },
            )
            state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            PrimaryButton(stringResource(R.string.sleep_save), onClick = onSave, loading = state.saving)
        }
    }
    GoalPickerDialog(
        picker = picker,
        state = state,
        onDismiss = { picker = null },
        onBedtimeChange = onBedtimeChange,
        onWakeUpTimeChange = onWakeUpTimeChange,
        onSleepMinutesChange = onSleepMinutesChange,
    )
    state.mismatch?.let { SleepGoalMismatchDialog(it, onSaveAnyway, onDismissMismatch) }
}

/** The rule of the form, or the error when the whole goal is empty. */
@Composable
private fun GoalHint(error: String?) {
    Text(
        text = error ?: stringResource(R.string.sleep_goal_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * Warns that the times do not add up, but lets the user keep them. "Review goal" is the confirm button because it
 * is the suggested way out; dismissing the dialog any other way also means reviewing.
 */
@Composable
private fun SleepGoalMismatchDialog(
    mismatch: SleepGoalMismatch,
    onSaveAnyway: () -> Unit,
    onReview: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onReview,
        title = { Text(stringResource(R.string.sleep_goal_mismatch_title)) },
        text = {
            Text(
                stringResource(
                    R.string.sleep_goal_mismatch_body,
                    mismatch.bedtime.toLocalizedTime(),
                    mismatch.wakeUpTime.toLocalizedTime(),
                    mismatch.timeInBed.asHoursAndMinutes(),
                    mismatch.sleep.asHoursAndMinutes(),
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onReview,
            ) { Text(stringResource(R.string.sleep_goal_mismatch_review)) }
        },
        dismissButton = {
            TextButton(onClick = onSaveAnyway) { Text(stringResource(R.string.sleep_goal_mismatch_save_anyway)) }
        },
    )
}

@Composable
private fun GoalPickerDialog(
    picker: GoalPicker?,
    state: EditSleepGoalState,
    onDismiss: () -> Unit,
    onBedtimeChange: (LocalTime?) -> Unit,
    onWakeUpTimeChange: (LocalTime?) -> Unit,
    onSleepMinutesChange: (Int?) -> Unit,
) {
    // Confirming a dialog applies the value and closes it.
    when (picker) {
        null -> Unit
        GoalPicker.Bedtime ->
            RemirTimePickerDialog(
                initial = state.bedtime ?: DEFAULT_BEDTIME,
                onConfirm = {
                    onBedtimeChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        GoalPicker.WakeUp ->
            RemirTimePickerDialog(
                initial = state.wakeUpTime ?: DEFAULT_WAKE_UP_TIME,
                onConfirm = {
                    onWakeUpTimeChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        GoalPicker.Duration ->
            RemirTimePickerDialog(
                initial = state.sleepMinutes?.let { LocalTime.MIDNIGHT.plusMinutes(it.toLong()) } ?: DEFAULT_SLEEP,
                onConfirm = {
                    onSleepMinutesChange(Duration.between(LocalTime.MIDNIGHT, it).toMinutes().toInt())
                    onDismiss()
                },
                onDismiss = onDismiss,
                // A duration is hours and minutes, never AM/PM.
                title = stringResource(R.string.sleep_goal_duration_dialog),
                is24Hour = true,
            )
    }
}

private val DEFAULT_BEDTIME = LocalTime.parse("23:00")
private val DEFAULT_WAKE_UP_TIME = LocalTime.parse("07:00")
private val DEFAULT_SLEEP = LocalTime.parse("08:00")

@PreviewLightDark
@Composable
private fun EditSleepGoalScreenPreview() {
    RemirPreview {
        EditSleepGoalScreen(
            state =
                EditSleepGoalState(
                    loading = false,
                    bedtime = LocalTime.parse("23:00"),
                    sleepMinutes = 480,
                ),
            onBack = {},
            onBedtimeChange = {},
            onWakeUpTimeChange = {},
            onSleepMinutesChange = {},
            onSave = {},
            onSaveAnyway = {},
            onDismissMismatch = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EditSleepGoalScreenMismatchPreview() {
    val bedtime = LocalTime.parse("23:00")
    val wakeUpTime = LocalTime.parse("06:00")
    RemirPreview {
        EditSleepGoalScreen(
            state =
                EditSleepGoalState(
                    loading = false,
                    bedtime = bedtime,
                    wakeUpTime = wakeUpTime,
                    sleepMinutes = 480,
                    mismatch = checkSleepGoalFits(bedtime, wakeUpTime, sleepMinutes = 480),
                ),
            onBack = {},
            onBedtimeChange = {},
            onWakeUpTimeChange = {},
            onSleepMinutesChange = {},
            onSave = {},
            onSaveAnyway = {},
            onDismissMismatch = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EditSleepGoalScreenEmptyErrorPreview() {
    RemirPreview {
        EditSleepGoalScreen(
            state =
                EditSleepGoalState(
                    loading = false,
                    fieldErrors = mapOf(SleepGoalValidation.WHOLE_GOAL to "set at least one of the three"),
                ),
            onBack = {},
            onBedtimeChange = {},
            onWakeUpTimeChange = {},
            onSleepMinutesChange = {},
            onSave = {},
            onSaveAnyway = {},
            onDismissMismatch = {},
        )
    }
}
