package dev.luizhenriquevicari.remir.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview

/** Bottom bar with one item per [TopLevelDestination]. Stateless: the caller owns the selection. */
@Composable
fun RemirNavigationBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RemirNavigationBarPreview() {
    RemirPreview { RemirNavigationBar(selected = TopLevelDestination.Faith, onSelect = {}) }
}
