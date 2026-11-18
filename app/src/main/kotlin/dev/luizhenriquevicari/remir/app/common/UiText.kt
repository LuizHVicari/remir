package dev.luizhenriquevicari.remir.app.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Text a ViewModel can expose without a `Context`: either a string that already came as text (e.g. from the
 * server) or a `strings.xml` resource. The screen turns it into a `String` with [asString].
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    data class Resource(@StringRes val id: Int) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> stringResource(id)
}
