package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.fieldErrors
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.sleep.SleepValidation
import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.uuid.Uuid

/** The defaults describe a typical night that just ended, so most nights only need a couple of taps. */
data class LogSleepState(
    val bedDate: LocalDate = LocalDate.now().minusDays(1),
    val bedTime: LocalTime = LocalTime.parse("23:00"),
    /** Null while unknown: the user may leave it out. */
    val fellAsleepTime: LocalTime? = null,
    val wokeUpTime: LocalTime = LocalTime.parse("07:00"),
    val gotUpTime: LocalTime = LocalTime.parse("07:15"),
    /** Errors per JSON field name, from local Konform validation and from the server (`ApiError.fields`). */
    val fieldErrors: Map<String, String> = emptyMap(),
    /** General error (network, 500...). */
    val error: UiText? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    /** True while an existing night is being edited rather than a new one logged. */
    val editing: Boolean = false,
    /** True while the night being edited is fetched, so the form does not show defaults for a moment. */
    val loading: Boolean = false,
    /** Why the night being edited could not be fetched; the screen offers to try again. */
    val loadError: UiText? = null,
)

/** Logs a new night, or edits the one with [editingId]: same form, same validation. */
class LogSleepViewModel(
    private val api: SleepApi,
    private val editingId: Uuid? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(LogSleepState(editing = editingId != null, loading = editingId != null))
    val state: StateFlow<LogSleepState> = _state.asStateFlow()

    init {
        load()
    }

    /** Fills the form with the night being edited. Nothing to do when logging a new one. */
    fun load() {
        val id = editingId ?: return
        _state.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            apiCall { api.get(id) }
                .onSuccess { record -> _state.value = record.toFormState().copy(editing = true) }
                .onFailure { e -> _state.update { it.copy(loading = false, loadError = e.userMessage) } }
        }
    }

    fun onBedDateChange(date: LocalDate) = edit { it.copy(bedDate = date) }

    fun onBedTimeChange(time: LocalTime) = edit { it.copy(bedTime = time) }

    fun onFellAsleepTimeChange(time: LocalTime?) = edit { it.copy(fellAsleepTime = time) }

    fun onWokeUpTimeChange(time: LocalTime) = edit { it.copy(wokeUpTime = time) }

    fun onGotUpTimeChange(time: LocalTime) = edit { it.copy(gotUpTime = time) }

    fun save() {
        val current = _state.value
        val request =
            buildSleepRequest(
                bedDate = current.bedDate,
                bedTime = current.bedTime,
                fellAsleepTime = current.fellAsleepTime,
                wokeUpTime = current.wokeUpTime,
                gotUpTime = current.gotUpTime,
            )
        // Same validator as the server, before calling the API.
        val validation = SleepValidation.create(request)
        if (validation is Invalid) {
            _state.update { it.copy(fieldErrors = validation.errors.byField()) }
            return
        }
        _state.update { it.copy(saving = true, fieldErrors = emptyMap(), error = null) }
        viewModelScope.launch {
            apiCall { if (editingId == null) api.create(request) else api.update(editingId, request) }
                .onSuccess { _state.update { it.copy(saving = false, saved = true) } }
                .onFailure { e ->
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

    private fun edit(change: (LogSleepState) -> LogSleepState) {
        _state.update { change(it).copy(fieldErrors = emptyMap(), error = null) }
    }
}
