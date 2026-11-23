package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.toLocalizedDate
import dev.luizhenriquevicari.remir.app.common.toLocalizedTime
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * One night in a list, on a single compact line: the day and when it started and ended on the left, the time
 * actually asleep on the right. Used by the recent nights on the Sleep tab and by the full history.
 */
@Composable
fun SleepNightRow(
    record: SleepRecordDto,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(record.wokeUpAt.toLocalizedDate(), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(
                    R.string.sleep_in_bed,
                    record.bedtime.toLocalizedTime(),
                    record.gotUpAt.toLocalizedTime(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(record.timeAsleep.asHoursAndMinutes(), style = MaterialTheme.typography.titleLarge)
    }
}

private fun sampleNight(
    bedtime: String,
    wokeUpAt: String,
    gotUpAt: String,
) = SleepRecordDto(
    id = Uuid.random(),
    bedtime = Instant.parse(bedtime),
    fellAsleepAt = null,
    wokeUpAt = Instant.parse(wokeUpAt),
    gotUpAt = Instant.parse(gotUpAt),
    createdAt = Instant.parse(gotUpAt) + 1.minutes,
)

/** Three nights for the previews of the screens that list them. */
internal val sampleNights =
    listOf(
        sampleNight("2026-03-10T23:00:00Z", "2026-03-11T07:00:00Z", "2026-03-11T07:15:00Z"),
        sampleNight("2026-03-09T22:45:00Z", "2026-03-10T06:30:00Z", "2026-03-10T06:45:00Z"),
        sampleNight("2026-03-08T23:30:00Z", "2026-03-09T07:20:00Z", "2026-03-09T07:30:00Z"),
    )

@PreviewLightDark
@Composable
private fun SleepNightRowPreview() {
    RemirPreview { SleepNightRow(sampleNights.first(), Modifier.padding(horizontal = 16.dp)) }
}
