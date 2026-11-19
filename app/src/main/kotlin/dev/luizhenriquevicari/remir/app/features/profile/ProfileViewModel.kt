package dev.luizhenriquevicari.remir.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.auth.ClerkSession
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.fieldErrors
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.profile.ProfileDto
import dev.luizhenriquevicari.remir.core.profile.ProfileValidation
import dev.luizhenriquevicari.remir.core.profile.UpdateProfileRequest
import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val loading: Boolean = true,
    val profile: ProfileDto? = null,
    val displayName: String = "",
    /**
     * Errors per field (key = JSON field name), from local Konform validation and from the server
     * (`ApiError.fields`).
     */
    val fieldErrors: Map<String, String> = emptyMap(),
    /** General error (network, 500...). */
    val error: UiText? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val signingOut: Boolean = false,
)

class ProfileViewModel(
    private val api: ProfileApi,
    private val session: ClerkSession,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            apiCall { api.get() }
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            loading = false,
                            profile = profile,
                            displayName = profile.displayName.orEmpty(),
                        )
                    }
                }.onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun onDisplayNameChange(displayName: String) {
        _state.update {
            it.copy(
                displayName = displayName,
                fieldErrors = it.fieldErrors - DISPLAY_NAME_FIELD,
                saved = false,
            )
        }
    }

    fun save() {
        val request = UpdateProfileRequest(displayName = _state.value.displayName.trim())
        // Same validator as the server, before calling the API.
        val validation = ProfileValidation.update(request)
        if (validation is Invalid) {
            _state.update { it.copy(fieldErrors = validation.errors.byField()) }
            return
        }
        _state.update { it.copy(saving = true, fieldErrors = emptyMap(), error = null) }
        viewModelScope.launch {
            apiCall { api.update(request) }
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            saving = false,
                            saved = true,
                            profile = profile,
                            displayName = profile.displayName.orEmpty(),
                        )
                    }
                }.onFailure { e ->
                    val fields = e.fieldErrors
                    _state.update {
                        it.copy(
                            saving = false,
                            fieldErrors = fields,
                            error = e.userMessage.takeIf { fields.isEmpty() },
                        )
                    }
                }
        }
    }

    fun signOut() {
        _state.update { it.copy(signingOut = true, error = null) }
        viewModelScope.launch {
            // On success the App gate switches to sign-in on its own.
            session.signOut().onFailure { e -> _state.update { it.copy(signingOut = false, error = e.userMessage) } }
        }
    }

    private companion object {
        const val DISPLAY_NAME_FIELD = "displayName"
    }
}
