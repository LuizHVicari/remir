package dev.luizhenriquevicari.remir.app.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview

/** Text field showing a validation error (message from the state's error map). */
@Composable
fun RemirTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    )
}

@PreviewLightDark
@Composable
private fun RemirTextFieldPreview() {
    RemirPreview {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RemirTextField(value = "", onValueChange = {}, label = "Display name")
            RemirTextField(value = "Luiz", onValueChange = {}, label = "Display name")
            RemirTextField(
                value = "L",
                onValueChange = {},
                label = "Display name",
                error = "must have between 2 and 60 characters",
            )
            RemirTextField(value = "Luiz", onValueChange = {}, label = "Display name", enabled = false)
            RemirTextField(
                value = "# Title\n\nSome text",
                onValueChange = {},
                label = "Content",
                singleLine = false,
                minLines = 4,
            )
        }
    }
}
