package dev.luizhenriquevicari.remir.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.luizhenriquevicari.remir.app.features.profile.ProfileApi
import dev.luizhenriquevicari.remir.app.network.apiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    /** From the server (token role claim). Shows the admin entry; the server enforces it on every admin call. */
    val isAdmin: Boolean = false,
)

class HomeViewModel(
    private val profileApi: ProfileApi,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        load()
    }

    /** A failure only hides the admin entry; the rest of the home screen does not depend on it. */
    private fun load() {
        viewModelScope.launch {
            apiCall { profileApi.get() }.onSuccess { profile -> _state.update { it.copy(isAdmin = profile.isAdmin) } }
        }
    }
}
