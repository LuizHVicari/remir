package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.common.UiText
import dev.luizhenriquevicari.remir.app.network.apiCall
import dev.luizhenriquevicari.remir.app.network.isNotFound
import dev.luizhenriquevicari.remir.app.network.userMessage
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

data class SleepState(
    val loading: Boolean = true,
    val records: List<SleepRecordDto> = emptyList(),
    /** Null while the user has not set one. */
    val goal: SleepGoalDto? = null,
    val period: SummaryPeriod = SummaryPeriod.Week,
    val summary: SleepSummary = SleepSummary(),
    /** True while the summary of [period] is being fetched and there is nothing to show for it yet. */
    val summaryLoading: Boolean = true,
    val error: UiText? = null,
)

class SleepViewModel(
    private val api: SleepApi,
) : ViewModel() {
    private val _state = MutableStateFlow(SleepState())
    val state: StateFlow<SleepState> = _state.asStateFlow()

    /** Switching periods quickly must not let a slow answer for the old period overwrite the new one. */
    private var summaryJob: Job? = null

    /** Silent when data is already on screen, so coming back from another screen does not flash a spinner. */
    fun refresh() {
        viewModelScope.launch { loadRecords() }
        viewModelScope.launch { loadGoal() }
        loadSummary()
    }

    fun retry() {
        _state.update { it.copy(loading = true, error = null) }
        refresh()
    }

    fun onPeriodChange(period: SummaryPeriod) {
        if (period == _state.value.period) return
        _state.update { it.copy(period = period, summaryLoading = true) }
        loadSummary()
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

    /** A failure keeps the previous summary: it is secondary to the list, which already reports the errors. */
    private fun loadSummary() {
        summaryJob?.cancel()
        val period = _state.value.period
        summaryJob =
            viewModelScope.launch {
                apiCall { api.list(since = Clock.System.now() - period.days.days) }
                    .onSuccess { records ->
                        _state.update { it.copy(summary = summarize(records), summaryLoading = false) }
                    }.onFailure { _state.update { it.copy(summaryLoading = false) } }
            }
    }
}
