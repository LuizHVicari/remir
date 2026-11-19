package dev.luizhenriquevicari.remir.app.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.app.designsystem.components.SecondaryButton
import org.koin.compose.viewmodel.koinViewModel

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun HomeRoute(
    onOpenProfile: () -> Unit,
    onOpenLegalAdmin: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, onOpenProfile = onOpenProfile, onOpenLegalAdmin = onOpenLegalAdmin)
}

@Composable
fun HomeScreen(
    state: HomeState,
    onOpenProfile: () -> Unit,
    onOpenLegalAdmin: () -> Unit,
) {
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.home_title)) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.home_coming_soon), style = MaterialTheme.typography.bodyLarge)
            PrimaryButton(stringResource(R.string.home_open_profile), onClick = onOpenProfile)
            if (state.isAdmin) {
                SecondaryButton(stringResource(R.string.home_open_legal_admin), onClick = onOpenLegalAdmin)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    RemirPreview { HomeScreen(HomeState(), onOpenProfile = {}, onOpenLegalAdmin = {}) }
}

@PreviewLightDark
@Composable
private fun HomeScreenAdminPreview() {
    RemirPreview { HomeScreen(HomeState(isAdmin = true), onOpenProfile = {}, onOpenLegalAdmin = {}) }
}
