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

private const val PAGE_SIZE = 20

/** The most the server returns at once; a refresh asks for what is already on screen, up to this. */
private const val MAX_REFRESH = 400

data class SleepHistoryState(
    /** True until the first page arrives. */
    val loading: Boolean = true,
    val items: List<SleepRecordDto> = emptyList(),
    val loadingMore: Boolean = false,
    /** A page came back shorter than asked for, so there is nothing left to load. */
    val endReached: Boolean = false,
    val error: UiText? = null,
    /** The night just deleted, kept so it can be brought back. The screen offers "Undo" while it is set. */
    val deleted: SleepRecordDto? = null,
    /** Why a delete or an undo failed. The screen shows it once and then clears it. */
    val actionError: UiText? = null,
)

/**
 * Every night, loaded a page at a time as the user scrolls (infinite scroll). A night can be deleted, which takes
 * it off the list right away and undone for a moment afterwards.
 */
class SleepHistoryViewModel(
    private val api: SleepApi,
) : ViewModel() {
    private val _state = MutableStateFlow(SleepHistoryState())
    val state: StateFlow<SleepHistoryState> = _state.asStateFlow()

    init {
        loadMore()
    }

    /** Loads the next page. Safe to call again and again: it does nothing while a page is loading or at the end. */
    fun loadMore() {
        val current = _state.value
        if (current.loadingMore || current.endReached) return
        _state.value = current.copy(loadingMore = true, error = null)
        viewModelScope.launch {
            apiCall { api.list(limit = PAGE_SIZE, offset = current.items.size) }
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            // Distinct, because a new night logged meanwhile shifts the pages by one.
                            items = (it.items + page).distinctBy { record -> record.id },
                            endReached = page.size < PAGE_SIZE,
                        )
                    }
                }.onFailure { e ->
                    _state.update { it.copy(loading = false, loadingMore = false, error = e.userMessage) }
                }
        }
    }

    /**
     * Fetches again what is already on screen, so a night edited elsewhere shows up changed. Silent: the list stays
     * as it is until the answer arrives, and a failure keeps it.
     */
    fun refresh() {
        val current = _state.value
        // Nothing to refresh yet: the first page is still on its way, and it will be fresh.
        if (current.items.isEmpty() || current.loadingMore) return
        val wanted = current.items.size.coerceIn(PAGE_SIZE, MAX_REFRESH)
        viewModelScope.launch {
            apiCall { api.list(limit = wanted) }
                .onSuccess { page ->
                    _state.update { it.copy(items = page, endReached = page.size < wanted) }
                }
        }
    }

    /** Takes the night off the list at once, then tells the server. Brought back if the server refuses. */
    fun delete(record: SleepRecordDto) {
        _state.update { it.copy(items = it.items - record, deleted = record) }
        viewModelScope.launch {
            apiCall { api.delete(record.id) }
                .onFailure { e ->
                    _state.update {
                        it.copy(items = it.items.withNight(record), deleted = null, actionError = e.userMessage)
                    }
                }
        }
    }

    /** Logs the deleted night again. It gets a new id, which nothing else refers to. */
    fun undoDelete() {
        val record = _state.value.deleted ?: return
        _state.update { it.copy(deleted = null) }
        viewModelScope.launch {
            apiCall { api.create(record.toRequest()) }
                .onSuccess { restored -> _state.update { it.copy(items = it.items.withNight(restored)) } }
                .onFailure { e -> _state.update { it.copy(actionError = e.userMessage) } }
        }
    }

    /** The "Undo" offer ran out. */
    fun onDeletedDismissed() {
        _state.update { it.copy(deleted = null) }
    }

    fun onActionErrorShown() {
        _state.update { it.copy(actionError = null) }
    }
}

/** Puts [night] back in the order the server lists nights: newest first, the id breaking ties. */
private fun List<SleepRecordDto>.withNight(night: SleepRecordDto): List<SleepRecordDto> =
    (this + night)
        .distinctBy { it.id }
        .sortedWith(compareByDescending<SleepRecordDto> { it.bedtime }.thenByDescending { it.id })
