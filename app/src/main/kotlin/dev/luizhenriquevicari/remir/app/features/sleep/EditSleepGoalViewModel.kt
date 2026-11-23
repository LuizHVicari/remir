package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.fieldErrors
import dev.luizhenriquevicari.remir.app.network.isNotFound
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalValidation
import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalTime
import java.time.LocalTime

data class EditSleepGoalState(
    val loading: Boolean = true,
    /** Each part is optional (null = not part of the goal), but the server needs at least one. */
    val bedtime: LocalTime? = null,
    val wakeUpTime: LocalTime? = null,
    val sleepMinutes: Int? = null,
    /** Errors per JSON field name; the one for the whole goal uses `SleepGoalValidation.WHOLE_GOAL`. */
    val fieldErrors: Map<String, String> = emptyMap(),
    /** General error (network, 500...). */
    val error: UiText? = null,
    /** Set while the "these times don't add up" dialog is open. The user may still save. */
    val mismatch: SleepGoalMismatch? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

class EditSleepGoalViewModel(
    private val api: SleepApi,
) : ViewModel() {
    private val _state = MutableStateFlow(EditSleepGoalState())
    val state: StateFlow<EditSleepGoalState> = _state.asStateFlow()

    init {
        load()
    }

    /** Fills the form with the current goal. A 404 only means there is none yet, so the form starts empty. */
    private fun load() {
        viewModelScope.launch {
            apiCall { api.goal() }
                .onSuccess { goal ->
                    _state.update {
                        it.copy(
                            loading = false,
                            bedtime = goal.bedtime?.toJavaLocalTime(),
                            wakeUpTime = goal.wakeUpTime?.toJavaLocalTime(),
                            sleepMinutes = goal.sleepMinutes,
                        )
                    }
                }.onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.userMessage.takeUnless { e.isNotFound }) }
                }
        }
    }

    fun onBedtimeChange(time: LocalTime?) = edit { it.copy(bedtime = time) }

    fun onWakeUpTimeChange(time: LocalTime?) = edit { it.copy(wakeUpTime = time) }

    fun onSleepMinutesChange(minutes: Int?) = edit { it.copy(sleepMinutes = minutes) }

    /** Validates, warns once if the times do not add up, and otherwise saves. */
    fun save() {
        val current = _state.value
        val request = current.toRequest()
        // Same validator as the server, before calling the API.
        val validation = SleepGoalValidation.save(request)
        if (validation is Invalid) {
            _state.update { it.copy(fieldErrors = validation.errors.byField()) }
            return
        }
        val mismatch = checkSleepGoalFits(current.bedtime, current.wakeUpTime, current.sleepMinutes)
        if (mismatch != null) {
            _state.update { it.copy(mismatch = mismatch) }
            return
        }
        send(request)
    }

    /** The user saw the warning and wants the goal as it is. */
    fun saveAnyway() {
        _state.update { it.copy(mismatch = null) }
        send(_state.value.toRequest())
    }

    fun dismissMismatch() {
        _state.update { it.copy(mismatch = null) }
    }

    private fun send(request: SaveSleepGoalRequest) {
        _state.update { it.copy(saving = true, fieldErrors = emptyMap(), error = null) }
        viewModelScope.launch {
            apiCall { api.saveGoal(request) }
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

    private fun EditSleepGoalState.toRequest() =
        SaveSleepGoalRequest(
            bedtime = bedtime?.toKotlinLocalTime(),
            wakeUpTime = wakeUpTime?.toKotlinLocalTime(),
            sleepMinutes = sleepMinutes,
        )

    private fun edit(change: (EditSleepGoalState) -> EditSleepGoalState) {
        _state.update { change(it).copy(fieldErrors = emptyMap(), error = null) }
    }
}
