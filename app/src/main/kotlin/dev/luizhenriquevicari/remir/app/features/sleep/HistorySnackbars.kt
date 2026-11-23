package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.common.asString

@Composable
internal fun DeletedNightSnackbar(
    host: SnackbarHostState,
    showing: Boolean,
    onUndo: () -> Unit,
    onDismissed: () -> Unit,
) {
    val message = stringResource(R.string.sleep_deleted)
    val undo = stringResource(R.string.sleep_undo)
    LaunchedEffect(showing) {
        if (!showing) return@LaunchedEffect
        // Without an explicit duration, a snackbar with an action never goes away on its own.
        when (host.showSnackbar(message, actionLabel = undo, duration = SnackbarDuration.Long)) {
            SnackbarResult.ActionPerformed -> onUndo()
            SnackbarResult.Dismissed -> onDismissed()
        }
    }
}

@Composable
internal fun ActionErrorSnackbar(
    host: SnackbarHostState,
    error: UiText?,
    onShown: () -> Unit,
) {
    val message = error?.asString()
    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        host.showSnackbar(message)
        onShown()
    }
}
