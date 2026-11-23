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

data class SleepHistoryState(
    /** True until the first page arrives. */
    val loading: Boolean = true,
    val items: List<SleepRecordDto> = emptyList(),
    val loadingMore: Boolean = false,
    /** A page came back shorter than asked for, so there is nothing left to load. */
    val endReached: Boolean = false,
    val error: UiText? = null,
)

/** Every night, loaded a page at a time as the user scrolls (infinite scroll). */
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
}
