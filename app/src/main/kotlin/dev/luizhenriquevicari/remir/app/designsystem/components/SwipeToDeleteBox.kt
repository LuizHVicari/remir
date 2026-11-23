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

/** Swipe [content] to either side to delete it, as in an email list. The caller removes the item in [onDelete]. */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    deleteDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) currentOnDelete()
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        backgroundContent = { DeleteStrip(state.dismissDirection, deleteDescription) },
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, content = content)
    }
}

@Composable
private fun DeleteStrip(
    direction: SwipeToDismissBoxValue,
    description: String,
) {
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
