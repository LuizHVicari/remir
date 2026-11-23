package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto

/**
 * The last few nights, as simple rows in one card, with "See all" to open the full history. Tapping a night
 * opens it for editing. With no nights yet it says how to log the first one.
 */
@Composable
fun RecentNightsCard(
    records: List<SleepRecordDto>,
    onSeeAll: () -> Unit,
    onEdit: (SleepRecordDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 20.dp, top = 12.dp, end = 12.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.sleep_recent_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (records.isNotEmpty()) {
                    TextButton(
                        onClick = onSeeAll,
                    ) { Text(stringResource(R.string.sleep_see_all)) }
                }
            }
            if (records.isEmpty()) EmptyNights() else NightRows(records, onEdit)
        }
    }
}

@Composable
private fun EmptyNights() {
    Text(
        stringResource(R.string.sleep_empty),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp, end = 8.dp, bottom = 12.dp),
    )
}

@Composable
private fun NightRows(
    records: List<SleepRecordDto>,
    onEdit: (SleepRecordDto) -> Unit,
) {
    Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        records.forEachIndexed { index, record ->
            SleepNightRow(record, onClick = { onEdit(record) })
            if (index < records.lastIndex) HorizontalDivider()
        }
    }
}

@PreviewLightDark
@Composable
private fun RecentNightsCardPreview() {
    RemirPreview { RecentNightsCard(sampleNights, onSeeAll = {}, onEdit = {}, modifier = Modifier.padding(16.dp)) }
}

@PreviewLightDark
@Composable
private fun RecentNightsCardEmptyPreview() {
    RemirPreview { RecentNightsCard(emptyList(), onSeeAll = {}, onEdit = {}, modifier = Modifier.padding(16.dp)) }
}
