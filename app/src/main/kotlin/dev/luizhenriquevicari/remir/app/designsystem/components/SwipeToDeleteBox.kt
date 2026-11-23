package dev.luizhenriquevicari.remir.app.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview

/**
 * Swipe the [content] to either side to delete it, as in an email list. A red strip with a bin shows behind it
 * while it moves. [onDelete] runs once the swipe is complete; the caller is expected to take the item out of its
 * list, which is what makes it disappear. [deleteDescription] is what screen readers say for the bin.
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    deleteDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val state = rememberSwipeToDismissBoxState()
    // The swipe has settled on one side: the item is gone as far as the user is concerned.
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) currentOnDelete()
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        backgroundContent = { DeleteStrip(state.dismissDirection, deleteDescription) },
    ) {
        // Opaque, so the strip behind never shows through the content.
        Surface(color = MaterialTheme.colorScheme.surface, content = content)
    }
}

@Composable
private fun DeleteStrip(
    direction: SwipeToDismissBoxValue,
    description: String,
) {
    // Nothing while at rest, so no red edge peeks out from behind the content.
    val color = if (direction == SwipeToDismissBoxValue.Settled) Color.Transparent else MaterialTheme.colorScheme.error
    val alignment =
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            SwipeToDismissBoxValue.Settled -> Alignment.Center
        }
    Box(Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp), contentAlignment = alignment) {
        if (direction != SwipeToDismissBoxValue.Settled) {
            Icon(Icons.Filled.Delete, contentDescription = description, tint = MaterialTheme.colorScheme.onError)
        }
    }
}

@PreviewLightDark
@Composable
private fun SwipeToDeleteBoxPreview() {
    RemirPreview {
        SwipeToDeleteBox(onDelete = {}, deleteDescription = "Delete") {
            Text("Swipe me", Modifier.fillMaxWidth().padding(24.dp))
        }
    }
}
