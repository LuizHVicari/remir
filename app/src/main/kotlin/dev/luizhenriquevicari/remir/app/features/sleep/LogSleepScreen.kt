package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import dev.luizhenriquevicari.remir.app.common.toLocalizedDate
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.PickerField
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirDatePickerDialog
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTimePickerDialog
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.LocalTime

/** Wires the ViewModel to the screen. Leaves the screen once the night is saved. */
@Composable
fun LogSleepRoute(
    onBack: () -> Unit,
    viewModel: LogSleepViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    LogSleepScreen(
        state = state,
        onBack = onBack,
        onBedDateChange = viewModel::onBedDateChange,
        onBedTimeChange = viewModel::onBedTimeChange,
        onFellAsleepTimeChange = viewModel::onFellAsleepTimeChange,
        onWokeUpTimeChange = viewModel::onWokeUpTimeChange,
        onGotUpTimeChange = viewModel::onGotUpTimeChange,
        onSave = viewModel::save,
    )
}

/** Which picker dialog is open. Saved so it survives rotation. */
private enum class Picker { BedDate, BedTime, FellAsleep, WokeUp, GotUp }

/** Stateless: state + lambdas only. The open dialog is the only local UI state. */
@Composable
fun LogSleepScreen(
    state: LogSleepState,
    onBack: () -> Unit,
    onBedDateChange: (LocalDate) -> Unit,
    onBedTimeChange: (LocalTime) -> Unit,
    onFellAsleepTimeChange: (LocalTime?) -> Unit,
    onWokeUpTimeChange: (LocalTime) -> Unit,
    onGotUpTimeChange: (LocalTime) -> Unit,
    onSave: () -> Unit,
) {
    var picker by rememberSaveable { mutableStateOf<Picker?>(null) }
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.sleep_log_title), onBack = onBack) }) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PickerField(
                value = state.bedDate.toLocalizedDate(),
                label = stringResource(R.string.sleep_went_to_bed_date),
                onClick = { picker = Picker.BedDate },
            )
            PickerField(
                value = state.bedTime.toLocalizedTime(),
                label = stringResource(R.string.sleep_went_to_bed),
                onClick = { picker = Picker.BedTime },
            )
            PickerField(
                value = state.fellAsleepTime?.toLocalizedTime() ?: stringResource(R.string.sleep_not_set),
                label = stringResource(R.string.sleep_fell_asleep),
                onClick = { picker = Picker.FellAsleep },
                error = state.fieldErrors["fellAsleepAt"],
                onClear = state.fellAsleepTime?.let { { onFellAsleepTimeChange(null) } },
            )
            PickerField(
                value = state.wokeUpTime.toLocalizedTime(),
                label = stringResource(R.string.sleep_woke_up),
                onClick = { picker = Picker.WokeUp },
                error = state.fieldErrors["wokeUpAt"],
            )
            PickerField(
                value = state.gotUpTime.toLocalizedTime(),
                label = stringResource(R.string.sleep_got_up),
                onClick = { picker = Picker.GotUp },
                error = state.fieldErrors["gotUpAt"],
            )
            state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            PrimaryButton(stringResource(R.string.sleep_save), onClick = onSave, loading = state.saving)
        }
    }
    PickerDialog(
        picker = picker,
        state = state,
        onDismiss = { picker = null },
        onBedDateChange = onBedDateChange,
        onBedTimeChange = onBedTimeChange,
        onFellAsleepTimeChange = onFellAsleepTimeChange,
        onWokeUpTimeChange = onWokeUpTimeChange,
        onGotUpTimeChange = onGotUpTimeChange,
    )
}

@Composable
private fun PickerDialog(
    picker: Picker?,
    state: LogSleepState,
    onDismiss: () -> Unit,
    onBedDateChange: (LocalDate) -> Unit,
    onBedTimeChange: (LocalTime) -> Unit,
    onFellAsleepTimeChange: (LocalTime?) -> Unit,
    onWokeUpTimeChange: (LocalTime) -> Unit,
    onGotUpTimeChange: (LocalTime) -> Unit,
) {
    // Confirming a dialog applies the value and closes it.
    when (picker) {
        null -> Unit
        Picker.BedDate ->
            RemirDatePickerDialog(
                initial = state.bedDate,
                onConfirm = {
                    onBedDateChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        Picker.BedTime ->
            RemirTimePickerDialog(
                initial = state.bedTime,
                onConfirm = {
                    onBedTimeChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        Picker.FellAsleep ->
            RemirTimePickerDialog(
                // Opens near the bedtime, since falling asleep is usually shortly after going to bed.
                initial = state.fellAsleepTime ?: state.bedTime.plusMinutes(DEFAULT_SLEEP_LATENCY_MINUTES),
                onConfirm = {
                    onFellAsleepTimeChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        Picker.WokeUp ->
            RemirTimePickerDialog(
                initial = state.wokeUpTime,
                onConfirm = {
                    onWokeUpTimeChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        Picker.GotUp ->
            RemirTimePickerDialog(
                initial = state.gotUpTime,
                onConfirm = {
                    onGotUpTimeChange(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
    }
}

private const val DEFAULT_SLEEP_LATENCY_MINUTES = 15L

private val sampleDate = LocalDate.parse("2026-03-10")

@PreviewLightDark
@Composable
private fun LogSleepScreenPreview() {
    RemirPreview {
        LogSleepScreen(
            state = LogSleepState(bedDate = sampleDate, fellAsleepTime = LocalTime.parse("23:30")),
            onBack = {},
            onBedDateChange = {},
            onBedTimeChange = {},
            onFellAsleepTimeChange = {},
            onWokeUpTimeChange = {},
            onGotUpTimeChange = {},
            onSave = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun LogSleepScreenErrorPreview() {
    RemirPreview {
        LogSleepScreen(
            state =
                LogSleepState(
                    bedDate = sampleDate,
                    fieldErrors = mapOf("gotUpAt" to "must not be before waking up"),
                ),
            onBack = {},
            onBedDateChange = {},
            onBedTimeChange = {},
            onFellAsleepTimeChange = {},
            onWokeUpTimeChange = {},
            onGotUpTimeChange = {},
            onSave = {},
        )
    }
}
