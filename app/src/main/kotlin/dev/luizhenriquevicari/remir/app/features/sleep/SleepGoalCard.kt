package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Call to action at the top of the Sleep tab. It uses the primary container colors, the same family as the
 * floating button, so it stands out from the night cards. Without a [goal] it invites the user to set one; with
 * one it shows the goal. Either way the whole card opens the editor.
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
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (goal == null) GoalInvitation() else GoalSummary(goal)
            }
            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.sleep_goal_edit))
        }
    }
}

@Composable
private fun GoalInvitation() {
    Text(stringResource(R.string.sleep_goal_cta_title), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.sleep_goal_cta_body), style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun GoalSummary(goal: SleepGoalDto) {
    Text(stringResource(R.string.sleep_goal_title), style = MaterialTheme.typography.titleMedium)
    goal.bedtime?.let { GoalLine(stringResource(R.string.sleep_goal_bedtime), it.toJavaLocalTime().toLocalizedTime()) }
    goal.wakeUpTime?.let {
        GoalLine(stringResource(R.string.sleep_goal_wake_up), it.toJavaLocalTime().toLocalizedTime())
    }
    goal.sleepMinutes?.let { GoalLine(stringResource(R.string.sleep_goal_sleep), it.minutes.asHoursAndMinutes()) }
}

@Composable
private fun GoalLine(
    label: String,
    value: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
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
