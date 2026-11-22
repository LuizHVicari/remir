package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview

/** Labels sit slightly back from the values, so the values read first. */
private const val LABEL_ALPHA = 0.72f

/** A value that is not set is fainter than a label, so it clearly reads as empty. */
private const val UNSET_ALPHA = 0.4f

/**
 * One value of the Sleep tab: a small icon and label over the value, which gets the display typeface. Shared by the
 * goal card and the summary; [contentColor] is the color of the card it sits on. A null [value] shows the muted
 * [placeholder] instead, and screen readers hear "not set" and not the dashes. [iconTint] colors the icon, for when
 * it also stands for something else (the summary tints it like the chart line the value belongs to).
 */
@Composable
internal fun RowScope.LabeledValue(
    icon: ImageVector,
    label: String,
    value: String?,
    placeholder: String,
    contentColor: Color,
    iconTint: Color = contentColor.copy(alpha = LABEL_ALPHA),
) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconTint,
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor.copy(alpha = LABEL_ALPHA))
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = contentColor)
        } else {
            val notSet = stringResource(R.string.sleep_not_set)
            Text(
                placeholder,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor.copy(alpha = UNSET_ALPHA),
                modifier = Modifier.semantics { contentDescription = notSet },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LabeledValuePreview() {
    RemirPreview {
        Row(Modifier.padding(16.dp)) {
            LabeledValue(Icons.Filled.Bedtime, "Bedtime", "11:00 PM", "--:--", MaterialTheme.colorScheme.onSurface)
            LabeledValue(Icons.Filled.Bedtime, "Wake up", null, "--:--", MaterialTheme.colorScheme.onSurface)
        }
    }
}
