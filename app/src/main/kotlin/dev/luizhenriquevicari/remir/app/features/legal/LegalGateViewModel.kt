package dev.luizhenriquevicari.remir.app.features.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.ApiException
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val HTTP_CONFLICT = 409

data class LegalGateState(
    val loading: Boolean = true,
    /** Documents still to be accepted; the first one is shown. */
    val pending: List<LegalDocumentDto> = emptyList(),
    val accepting: Boolean = false,
    val error: UiText? = null,
)

/** Asks the signed-in user to accept the documents that are pending (new account or new material version). */
class LegalGateViewModel(
    private val api: LegalApi,
) : ViewModel() {
    private val _state = MutableStateFlow(LegalGateState())
    val state: StateFlow<LegalGateState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            apiCall { api.pending() }
                .onSuccess { status -> _state.update { it.copy(loading = false, pending = status.pending) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun accept() {
        val document = _state.value.pending.firstOrNull() ?: return
        _state.update { it.copy(accepting = true, error = null) }
        viewModelScope.launch {
            apiCall { api.accept(document.type, document.version) }
                .onSuccess { _state.update { it.copy(accepting = false, pending = it.pending - document) } }
                .onFailure { e -> onAcceptFailed(e) }
        }
    }

    /** A newer version was published while the screen was open: reload so the user sees the current text. */
    private fun onAcceptFailed(e: Throwable) {
        val stale = (e as? ApiException)?.status == HTTP_CONFLICT
        _state.update { it.copy(accepting = false, error = e.userMessage.takeUnless { stale }) }
        if (stale) load()
    }
}
