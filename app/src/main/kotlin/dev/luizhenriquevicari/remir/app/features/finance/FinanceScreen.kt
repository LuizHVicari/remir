package dev.luizhenriquevicari.remir.app.features.finance

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ComingSoon
import dev.luizhenriquevicari.remir.app.navigation.TabScaffold

/** Stateless placeholder; gets a ViewModel and a Route when the feature has real state. */
@Composable
fun FinanceScreen(onOpenProfile: () -> Unit) {
    TabScaffold(stringResource(R.string.tab_finance), onOpenProfile) { padding ->
        ComingSoon(Modifier.padding(padding))
    }
}

@PreviewLightDark
@Composable
private fun FinanceScreenPreview() {
    RemirPreview { FinanceScreen(onOpenProfile = {}) }
}
