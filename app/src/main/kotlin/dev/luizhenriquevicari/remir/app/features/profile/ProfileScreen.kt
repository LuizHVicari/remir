package dev.luizhenriquevicari.remir.app.features.profile

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
import dev.luizhenriquevicari.remir.app.common.toLocalizedDate
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.designsystem.components.PrimaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTextField
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.app.designsystem.components.SecondaryButton
import dev.luizhenriquevicari.remir.core.profile.ProfileDto
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onBack = onBack,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onSave = viewModel::save,
        onSignOut = viewModel::signOut,
        onRetry = viewModel::load,
    )
}

/** Stateless: state + lambdas only. */
@Composable
fun ProfileScreen(
    state: ProfileState,
    onBack: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onSignOut: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.profile_title), onBack = onBack) }) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.loading -> Loading(modifier)
            state.profile == null -> ErrorState(state.error?.asString() ?: stringResource(R.string.profile_unavailable), modifier, onRetry)
            else -> ProfileContent(state, state.profile, onDisplayNameChange, onSave, onSignOut, modifier)
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    profile: ProfileDto,
    onDisplayNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(profile.displayName ?: stringResource(R.string.profile_no_name), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.profile_member_since, profile.createdAt.toLocalizedDate()), style = MaterialTheme.typography.bodyMedium)
        RemirTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChange,
            label = stringResource(R.string.profile_display_name),
            error = state.fieldErrors["displayName"],
            enabled = !state.saving,
        )
        state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        if (state.saved) Text(stringResource(R.string.profile_saved), color = MaterialTheme.colorScheme.primary)
        PrimaryButton(stringResource(R.string.profile_save), onClick = onSave, loading = state.saving)
        SecondaryButton(stringResource(R.string.profile_sign_out), onClick = onSignOut, enabled = !state.signingOut)
    }
}

private val sampleProfile = ProfileDto(
    id = "user_2abc",
    displayName = "Luiz",
    createdAt = Instant.parse("2026-01-10T12:00:00Z"),
    updatedAt = Instant.parse("2026-01-10T12:00:00Z"),
)

@PreviewLightDark
@Composable
private fun ProfileScreenPreview() {
    RemirPreview {
        ProfileScreen(
            state = ProfileState(
                loading = false,
                profile = sampleProfile,
                displayName = "L",
                fieldErrors = mapOf("displayName" to "must have between 2 and 60 characters"),
            ),
            onBack = {}, onDisplayNameChange = {}, onSave = {}, onSignOut = {}, onRetry = {},
        )
    }
}
