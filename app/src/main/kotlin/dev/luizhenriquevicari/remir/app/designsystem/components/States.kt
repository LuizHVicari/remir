package dev.luizhenriquevicari.remir.app.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Full-screen error. The retry button only shows when [onRetry] is given. */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        onRetry?.let { SecondaryButton(stringResource(R.string.retry), onClick = it) }
    }
}

/** Placeholder body for a section that has no content yet. */
@Composable
fun ComingSoon(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.coming_soon), style = MaterialTheme.typography.bodyLarge)
    }
}

@PreviewLightDark
@Composable
private fun ComingSoonPreview() {
    RemirPreview { ComingSoon() }
}

@PreviewLightDark
@Composable
private fun LoadingPreview() {
    RemirPreview { Loading() }
}

@PreviewLightDark
@Composable
private fun ErrorStateWithRetryPreview() {
    RemirPreview { ErrorState(message = "Could not reach the server", onRetry = {}) }
}

@PreviewLightDark
@Composable
private fun ErrorStateWithoutRetryPreview() {
    RemirPreview { ErrorState(message = "Set clerk.publishableKey in local.properties and rebuild.") }
}
