package dev.luizhenriquevicari.remir.app.features.legal

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import dev.luizhenriquevicari.remir.app.common.asString
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.designsystem.components.MarkdownText
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@StringRes
fun LegalDocumentType.titleRes(): Int = when (this) {
    LegalDocumentType.Privacy -> R.string.legal_privacy_title
    LegalDocumentType.Terms -> R.string.legal_terms_title
}

/**
 * Shows [content] once nothing is pending. While a document is pending, the user sees it and must accept
 * it to continue. Wraps the signed-in part of the app.
 */
@Composable
fun LegalGate(
    viewModel: LegalGateViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val document = state.pending.firstOrNull()
    when {
        state.loading -> Loading()
        document != null -> LegalAcceptanceScreen(document, state, onAccept = viewModel::accept)
        state.error != null -> ErrorState(state.error?.asString().orEmpty(), onRetry = viewModel::load)
        else -> content()
    }
}

/** Stateless: the document to accept, plus the accept action. */
@Composable
fun LegalAcceptanceScreen(
    document: LegalDocumentDto,
    state: LegalGateState,
    onAccept: () -> Unit,
) {
    Scaffold(topBar = { RemirTopBar(stringResource(document.type.titleRes())) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.legal_review_prompt), style = MaterialTheme.typography.bodyMedium)
                MarkdownText(document.content)
            }
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                PrimaryButton(stringResource(R.string.legal_accept), onClick = onAccept, loading = state.accepting)
            }
        }
    }
}

private val sampleDocument = LegalDocumentDto(
    type = LegalDocumentType.Privacy,
    version = 2,
    content = "# Privacy Policy\n\nRemir is operated by **Luiz Vicari**.\n\n## Data I process\n\n- Account data, handled by Clerk\n- The display name you choose",
    requiresAcceptance = true,
    effectiveAt = Instant.parse("2026-11-19T12:00:00Z"),
)

@PreviewLightDark
@Composable
private fun LegalAcceptanceScreenPreview() {
    RemirPreview {
        LegalAcceptanceScreen(sampleDocument, LegalGateState(loading = false, pending = listOf(sampleDocument)), onAccept = {})
    }
}
