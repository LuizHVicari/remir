package dev.luizhenriquevicari.remir.app.features.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.ApiException
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.fieldErrors
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import dev.luizhenriquevicari.remir.core.legal.LegalValidation
import dev.luizhenriquevicari.remir.core.legal.PublishLegalDocumentRequest
import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val HTTP_NOT_FOUND = 404

data class AdminLegalState(
    val type: LegalDocumentType = LegalDocumentType.Privacy,
    val loading: Boolean = true,
    val content: String = "",
    val requiresAcceptance: Boolean = true,
    /** Version currently published for [type]; null when none yet. */
    val currentVersion: Int? = null,
    val publishing: Boolean = false,
    /** Version just published, to confirm to the admin. */
    val publishedVersion: Int? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: UiText? = null,
)

/** Admin screen: loads the current version of a document, lets the admin edit it and publish a new version. */
class AdminLegalViewModel(
    private val api: LegalApi,
) : ViewModel() {
    private val _state = MutableStateFlow(AdminLegalState())
    val state: StateFlow<AdminLegalState> = _state.asStateFlow()

    init {
        load()
    }

    fun selectType(type: LegalDocumentType) {
        _state.update { it.copy(type = type, publishedVersion = null) }
        load()
    }

    fun load() {
        val type = _state.value.type
        _state.update { it.copy(loading = true, error = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            apiCall { api.current(type) }
                .onSuccess { doc ->
                    _state.update { it.copy(loading = false, content = doc.content, currentVersion = doc.version) }
                }
                .onFailure { e -> onLoadFailed(e) }
        }
    }

    /** A 404 only means "never published": start from an empty editor instead of showing an error. */
    private fun onLoadFailed(e: Throwable) {
        val notPublished = (e as? ApiException)?.status == HTTP_NOT_FOUND
        _state.update {
            it.copy(
                loading = false,
                content = if (notPublished) "" else it.content,
                currentVersion = if (notPublished) null else it.currentVersion,
                error = e.userMessage.takeUnless { notPublished },
            )
        }
    }

    fun onContentChange(content: String) {
        _state.update { it.copy(content = content, fieldErrors = it.fieldErrors - CONTENT_FIELD, publishedVersion = null) }
    }

    fun onRequiresAcceptanceChange(requires: Boolean) {
        _state.update { it.copy(requiresAcceptance = requires, publishedVersion = null) }
    }

    fun publish() {
        val current = _state.value
        val request = PublishLegalDocumentRequest(current.content.trim(), current.requiresAcceptance)
        // Same validator as the server, before calling the API.
        val validation = LegalValidation.publish(request)
        if (validation is Invalid) {
            _state.update { it.copy(fieldErrors = validation.errors.byField()) }
            return
        }
        _state.update { it.copy(publishing = true, fieldErrors = emptyMap(), error = null, publishedVersion = null) }
        viewModelScope.launch {
            apiCall { api.publish(current.type, request) }
                .onSuccess { doc ->
                    _state.update {
                        it.copy(
                            publishing = false,
                            content = doc.content,
                            currentVersion = doc.version,
                            publishedVersion = doc.version,
                        )
                    }
                }
                .onFailure { e ->
                    val fields = e.fieldErrors
                    _state.update { it.copy(publishing = false, fieldErrors = fields, error = e.userMessage.takeIf { fields.isEmpty() }) }
                }
        }
    }

    private companion object {
        const val CONTENT_FIELD = "content"
    }
}
