package dev.luizhenriquevicari.remir.app.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar

/** Screen without state of its own: no ViewModel needed (add one once it has state). */
@Composable
fun HomeRoute(onOpenProfile: () -> Unit) {
    HomeScreen(onOpenProfile = onOpenProfile)
}

@Composable
fun HomeScreen(onOpenProfile: () -> Unit) {
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.home_title)) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.home_coming_soon), style = MaterialTheme.typography.bodyLarge)
            PrimaryButton(stringResource(R.string.home_open_profile), onClick = onOpenProfile)
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    RemirPreview { HomeScreen(onOpenProfile = {}) }
}
