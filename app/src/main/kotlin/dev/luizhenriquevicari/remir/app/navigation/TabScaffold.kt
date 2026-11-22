package dev.luizhenriquevicari.remir.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ComingSoon
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar

/**
 * Layout shared by the [TopLevelDestination] screens: a top bar with the profile shortcut, an optional
 * [floatingActionButton] and the content.
 */
@Composable
fun TabScaffold(
    title: String,
    onOpenProfile: () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            RemirTopBar(
                title = title,
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(R.string.open_profile))
                    }
                },
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun TabScaffoldPreview() {
    RemirPreview {
        TabScaffold(title = "Sleep", onOpenProfile = {}) { padding ->
            ComingSoon(Modifier.padding(padding))
        }
    }
}
