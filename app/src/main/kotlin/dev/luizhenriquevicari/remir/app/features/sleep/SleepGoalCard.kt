package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Labels sit slightly back from the values, so the values read first. */
private const val LABEL_ALPHA = 0.72f

/** A value that is not set is fainter than a label, so it clearly reads as empty. */
private const val UNSET_ALPHA = 0.4f

/**
 * Call to action at the top of the Sleep tab. It uses the primary container colors, the same family as the
 * floating button, so it stands out from the night cards. Without a [goal] it invites the user to set one; with
 * one it shows the goal as up to three values side by side. Either way the whole card opens the editor.
 */
@Composable
fun SleepGoalCard(
    goal: SleepGoalDto?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onEdit,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val title = if (goal == null) R.string.sleep_goal_cta_title else R.string.sleep_goal_title
                    Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
                    if (goal == null) {
                        Text(stringResource(R.string.sleep_goal_cta_body), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                // A standard icon, with no container: the whole card is the button, so this only signals the action.
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.sleep_goal_edit))
            }
            goal?.let { GoalValues(it) }
        }
    }
}

@Composable
private fun GoalValues(goal: SleepGoalDto) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // All three always show, so the user sees what exists; the ones not set carry a muted placeholder.
        GoalValue(
            icon = Icons.Filled.Bedtime,
            label = stringResource(R.string.sleep_goal_bedtime),
            value = goal.bedtime?.toJavaLocalTime()?.toLocalizedTime(),
            placeholder = stringResource(R.string.sleep_goal_unset_time),
        )
        GoalValue(
            icon = Icons.Filled.WbSunny,
            label = stringResource(R.string.sleep_goal_wake_up),
            value = goal.wakeUpTime?.toJavaLocalTime()?.toLocalizedTime(),
            placeholder = stringResource(R.string.sleep_goal_unset_time),
        )
        GoalValue(
            icon = Icons.Filled.Schedule,
            label = stringResource(R.string.sleep_goal_sleep),
            value = goal.sleepMinutes?.minutes?.asHoursAndMinutes(),
            placeholder = stringResource(R.string.sleep_goal_unset_duration),
        )
    }
}

/**
 * One goal value: a small icon and label over the value, which gets the display typeface. A null [value] shows
 * the muted [placeholder] instead, and screen readers hear "not set" and not the dashes.
 */
@Composable
private fun RowScope.GoalValue(
    icon: ImageVector,
    label: String,
    value: String?,
    placeholder: String,
) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = LABEL_ALPHA),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = LABEL_ALPHA),
            )
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
        } else {
            val notSet = stringResource(R.string.sleep_not_set)
            Text(
                placeholder,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = UNSET_ALPHA),
                modifier = Modifier.semantics { contentDescription = notSet },
            )
        }
    }
}

private val sampleUpdatedAt = Instant.parse("2026-03-10T12:00:00Z")
private const val SAMPLE_EIGHT_HOURS = 480
private const val SAMPLE_SEVEN_AND_A_HALF_HOURS = 450

@PreviewLightDark
@Composable
private fun SleepGoalCardInvitationPreview() {
    RemirPreview { SleepGoalCard(goal = null, onEdit = {}, modifier = Modifier.padding(16.dp)) }
}

@PreviewLightDark
@Composable
private fun SleepGoalCardFullPreview() {
    RemirPreview {
        SleepGoalCard(
            goal =
                SleepGoalDto(
                    LocalTime.parse("23:00"),
                    LocalTime.parse("07:00"),
                    SAMPLE_EIGHT_HOURS,
                    sampleUpdatedAt,
                ),
            onEdit = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepGoalCardOnlyDurationPreview() {
    RemirPreview {
        SleepGoalCard(
            goal = SleepGoalDto(null, null, SAMPLE_SEVEN_AND_A_HALF_HOURS, sampleUpdatedAt),
            onEdit = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepGoalCardBedtimeAndWakeUpPreview() {
    RemirPreview {
        SleepGoalCard(
            goal = SleepGoalDto(LocalTime.parse("22:30"), LocalTime.parse("06:30"), null, sampleUpdatedAt),
            onEdit = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
