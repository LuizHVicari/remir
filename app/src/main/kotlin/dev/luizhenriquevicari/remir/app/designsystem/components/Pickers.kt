package dev.luizhenriquevicari.remir.app.designsystem.components

import android.text.format.DateFormat
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Read-only field that opens a picker when tapped. Looks like [RemirTextField], so forms stay consistent.
 * [onClear] adds a trailing clear button, for optional values.
 */
@Composable
fun PickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    onClear: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // A read-only text field does not report clicks, but it does report the press that ends in one.
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) onClick() }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon =
            onClear?.let {
                {
                    IconButton(onClick = it) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear))
                    }
                }
            },
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Material3's time picker state is still experimental. The opt-in (approved by the project owner) is kept to this
 * one function so the rest of the app does not depend on it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemirTimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    title: String? = null,
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
) {
    val state =
        rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = is24Hour,
        )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = title?.let { { Text(it) } },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        text = { TimePicker(state = state) },
    )
}

/** Date dialog where days after [maxDate] (today by default) are disabled, so nothing can be logged in the future. */
@Composable
fun RemirDatePickerDialog(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    maxDate: LocalDate = LocalDate.now(),
) {
    // The picker works in UTC milliseconds at midnight, whatever the device's time zone is.
    val initialMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val selectableDates =
        remember(maxDate) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    !Instant
                        .ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                        .isAfter(maxDate)

                override fun isSelectableYear(year: Int) = year <= maxDate.year
            }
        }
    val state =
        rememberDatePickerState(initialSelectedDateMillis = initialMillis, selectableDates = selectableDates)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

@PreviewLightDark
@Composable
private fun PickerFieldPreview() {
    RemirPreview {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PickerField(value = "11:00 PM", label = "Went to bed", onClick = {})
            PickerField(value = "11:30 PM", label = "Fell asleep", onClick = {}, onClear = {})
            PickerField(
                value = "7:00 AM",
                label = "Woke up",
                onClick = {},
                error = "must be after bedtime",
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RemirTimePickerDialogPreview() {
    RemirPreview { RemirTimePickerDialog(initial = LocalTime.parse("23:00"), onConfirm = {}, onDismiss = {}) }
}
