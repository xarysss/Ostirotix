package com.ostirotix.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.model.LeaderEntry
import com.ostirotix.app.data.model.ProfileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val leaderboard: List<LeaderEntry> = emptyList(),
    val profile: ProfileData? = null,
)

/** Leaderboard + profil serveur (nécessite le backend). */
class AccountViewModel : ViewModel() {
    private val api = ServiceLocator.api
    val prefs = ServiceLocator.prefs

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state

    fun loadLeaderboard() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                _state.update { it.copy(leaderboard = api.leaderboard(), loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Backend injoignable", loading = false) }
            }
        }
    }

    fun loadProfile() {
        val acc = prefs.account ?: run {
            _state.update { it.copy(profile = null) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                _state.update { it.copy(profile = api.profile(acc.id), loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Backend injoignable", loading = false) }
            }
        }
    }
}
