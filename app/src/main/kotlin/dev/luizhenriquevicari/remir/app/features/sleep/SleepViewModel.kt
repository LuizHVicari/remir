package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SleepState(
    val loading: Boolean = true,
    val records: List<SleepRecordDto> = emptyList(),
    val error: UiText? = null,
)

class SleepViewModel(
    private val api: SleepApi,
) : ViewModel() {
    private val _state = MutableStateFlow(SleepState())
    val state: StateFlow<SleepState> = _state.asStateFlow()

    /** Silent when records are already on screen, so coming back from the log screen does not flash a spinner. */
    fun refresh() {
        viewModelScope.launch {
            apiCall { api.list() }
                .onSuccess { records -> _state.update { it.copy(loading = false, records = records, error = null) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun retry() {
        _state.update { it.copy(loading = true, error = null) }
        refresh()
    }
}
