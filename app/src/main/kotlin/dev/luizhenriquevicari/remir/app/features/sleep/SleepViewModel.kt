package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.isNotFound
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SleepState(
    val loading: Boolean = true,
    val records: List<SleepRecordDto> = emptyList(),
    /** Null while the user has not set one. */
    val goal: SleepGoalDto? = null,
    val error: UiText? = null,
)

class SleepViewModel(
    private val api: SleepApi,
) : ViewModel() {
    private val _state = MutableStateFlow(SleepState())
    val state: StateFlow<SleepState> = _state.asStateFlow()

    /** Silent when data is already on screen, so coming back from another screen does not flash a spinner. */
    fun refresh() {
        viewModelScope.launch { loadRecords() }
        viewModelScope.launch { loadGoal() }
    }

    fun retry() {
        _state.update { it.copy(loading = true, error = null) }
        refresh()
    }

    private suspend fun loadRecords() {
        apiCall { api.list() }
            .onSuccess { records -> _state.update { it.copy(loading = false, records = records, error = null) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage) } }
    }

    /** A 404 just means no goal yet; any other failure keeps what is on screen, the card is not worth an error. */
    private suspend fun loadGoal() {
        apiCall { api.goal() }
            .onSuccess { goal -> _state.update { it.copy(goal = goal) } }
            .onFailure { e -> if (e.isNotFound) _state.update { it.copy(goal = null) } }
    }
}
