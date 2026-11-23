package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.core.sleep.SleepFeeling

/** The face that stands for this feeling, from the saddest to the happiest. */
val SleepFeeling.icon: ImageVector
    get() =
        when (this) {
            SleepFeeling.EXHAUSTED -> Icons.Outlined.SentimentVeryDissatisfied
            SleepFeeling.TIRED -> Icons.Outlined.SentimentDissatisfied
            SleepFeeling.OK -> Icons.Outlined.SentimentNeutral
            SleepFeeling.RESTED -> Icons.Outlined.SentimentSatisfied
            SleepFeeling.ENERGIZED -> Icons.Outlined.SentimentVerySatisfied
        }

/** The word shown under the face, and read out by screen readers. */
@get:StringRes
val SleepFeeling.label: Int
    get() =
        when (this) {
            SleepFeeling.EXHAUSTED -> R.string.sleep_feeling_exhausted
            SleepFeeling.TIRED -> R.string.sleep_feeling_tired
            SleepFeeling.OK -> R.string.sleep_feeling_ok
            SleepFeeling.RESTED -> R.string.sleep_feeling_rested
            SleepFeeling.ENERGIZED -> R.string.sleep_feeling_energized
        }

/**
 * Asks how the user felt on waking up: one face per feeling, in a row. Tapping the chosen one again clears the
 * answer, since it is optional.
 */
@Composable
fun SleepFeelingPicker(
    selected: SleepFeeling?,
    onSelect: (SleepFeeling?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.sleep_feeling_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.SpaceBetween) {
            SleepFeeling.entries.forEach { feeling ->
                FeelingOption(
                    feeling = feeling,
                    selected = feeling == selected,
                    onClick = { onSelect(if (feeling == selected) null else feeling) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FeelingOption(
    feeling: SleepFeeling,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (selected) colors.primaryContainer else colors.surface
    val content = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
    Column(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            feeling.icon,
            contentDescription = null,
            tint = content,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(container)
                    .padding(10.dp),
        )
        Text(
            stringResource(feeling.label),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepFeelingPickerPreview() {
    RemirPreview { SleepFeelingPicker(selected = null, onSelect = {}, modifier = Modifier.padding(16.dp)) }
}

@PreviewLightDark
@Composable
private fun SleepFeelingPickerSelectedPreview() {
    RemirPreview {
        SleepFeelingPicker(selected = SleepFeeling.RESTED, onSelect = {}, modifier = Modifier.padding(16.dp))
    }
}
