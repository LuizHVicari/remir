package dev.luizhenriquevicari.remir.app.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

/**
 * Wraps a `@PreviewLightDark` in the app theme on the theme's background, so previews look like the app.
 * Use it in every preview instead of repeating `RemirTheme { Surface { ... } }`.
 */
@Composable
internal fun RemirPreview(content: @Composable () -> Unit) {
    RemirTheme {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}
