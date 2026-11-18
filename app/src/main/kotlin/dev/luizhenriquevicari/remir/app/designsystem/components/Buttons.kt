package dev.luizhenriquevicari.remir.app.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview

/** Same height as Clerk's sign-in buttons, so the app and the auth screen feel like one product. */
private val ButtonMinHeight = 48.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        // Not disabled while loading: disabled colors would wash out the spinner. Clicks are ignored instead.
        onClick = { if (!loading) onClick() },
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth().heightIn(min = ButtonMinHeight),
    ) {
        if (loading) {
            // Default color would be `primary`, the same as the button background.
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth().heightIn(min = ButtonMinHeight),
    ) {
        Text(text)
    }
}

@PreviewLightDark
@Composable
private fun PrimaryButtonPreview() {
    RemirPreview {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Save", onClick = {})
            PrimaryButton("Save", onClick = {}, loading = true)
            PrimaryButton("Save", onClick = {}, enabled = false)
        }
    }
}

@PreviewLightDark
@Composable
private fun SecondaryButtonPreview() {
    RemirPreview {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("Sign out", onClick = {})
            SecondaryButton("Sign out", onClick = {}, enabled = false)
        }
    }
}
