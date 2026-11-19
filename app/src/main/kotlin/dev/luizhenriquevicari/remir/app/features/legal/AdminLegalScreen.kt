package dev.luizhenriquevicari.remir.app.features.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.asString
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTextField
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import org.koin.compose.viewmodel.koinViewModel

private const val EDITOR_MIN_LINES = 12

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun AdminLegalRoute(
    onBack: () -> Unit,
    viewModel: AdminLegalViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AdminLegalScreen(
        state = state,
        onBack = onBack,
        onSelectType = viewModel::selectType,
        onContentChange = viewModel::onContentChange,
        onRequiresAcceptanceChange = viewModel::onRequiresAcceptanceChange,
        onPublish = viewModel::publish,
    )
}

/** Stateless: state + lambdas only. */
@Composable
fun AdminLegalScreen(
    state: AdminLegalState,
    onBack: () -> Unit,
    onSelectType: (LegalDocumentType) -> Unit,
    onContentChange: (String) -> Unit,
    onRequiresAcceptanceChange: (Boolean) -> Unit,
    onPublish: () -> Unit,
) {
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.legal_admin_title), onBack = onBack) }) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TypeSelector(state.type, onSelectType)
            VersionLabel(state)
            if (state.loading) {
                Loading(Modifier.padding(32.dp))
                return@Column
            }
            RemirTextField(
                value = state.content,
                onValueChange = onContentChange,
                label = stringResource(R.string.legal_admin_content),
                error = state.fieldErrors["content"],
                enabled = !state.publishing,
                singleLine = false,
                minLines = EDITOR_MIN_LINES,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(checked = state.requiresAcceptance, onCheckedChange = onRequiresAcceptanceChange)
                Text(
                    stringResource(R.string.legal_admin_requires_acceptance),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            state.publishedVersion?.let {
                Text(stringResource(R.string.legal_admin_published, it), color = MaterialTheme.colorScheme.primary)
            }
            PrimaryButton(stringResource(R.string.legal_admin_publish), onClick = onPublish, loading = state.publishing)
        }
    }
}

@Composable
private fun TypeSelector(
    selected: LegalDocumentType,
    onSelect: (LegalDocumentType) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegalDocumentType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(stringResource(type.titleRes())) },
            )
        }
    }
}

@Composable
private fun VersionLabel(state: AdminLegalState) {
    val text =
        state.currentVersion
            ?.let { stringResource(R.string.legal_admin_current_version, it) }
            ?: stringResource(R.string.legal_admin_not_published)
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@PreviewLightDark
@Composable
private fun AdminLegalScreenPreview() {
    RemirPreview {
        AdminLegalScreen(
            state =
                AdminLegalState(
                    loading = false,
                    content = "# Privacy Policy\n\nRemir is operated by Luiz Vicari.",
                    currentVersion = 2,
                    publishedVersion = 2,
                    fieldErrors = emptyMap(),
                ),
            onBack = {},
            onSelectType = {},
            onContentChange = {},
            onRequiresAcceptanceChange = {},
            onPublish = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun AdminLegalScreenEmptyPreview() {
    RemirPreview {
        AdminLegalScreen(
            state =
                AdminLegalState(
                    loading = false,
                    type = LegalDocumentType.Terms,
                    fieldErrors = mapOf("content" to "must not be blank or start/end with spaces"),
                ),
            onBack = {},
            onSelectType = {},
            onContentChange = {},
            onRequiresAcceptanceChange = {},
            onPublish = {},
        )
    }
}
