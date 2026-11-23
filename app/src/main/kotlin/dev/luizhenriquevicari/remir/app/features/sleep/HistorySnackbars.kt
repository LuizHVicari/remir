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

/** Says a night was deleted for a few seconds, with "Undo". Reports which way it ended. */
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
        // A snackbar with an action never goes away on its own by default, so ask for a duration.
        when (host.showSnackbar(message, actionLabel = undo, duration = SnackbarDuration.Long)) {
            SnackbarResult.ActionPerformed -> onUndo()
            SnackbarResult.Dismissed -> onDismissed()
        }
    }
}

/** Shows a failure of a delete or an undo once. */
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
