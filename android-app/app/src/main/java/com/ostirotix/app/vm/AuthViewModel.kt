package com.ostirotix.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.auth.AuthResult
import com.ostirotix.app.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val account: UserAccount? = ServiceLocator.auth.currentUser(),
)

class AuthViewModel : ViewModel() {
    private val auth = ServiceLocator.auth

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        applyResult(auth.login(email, password), onSuccess)
    }

    fun register(
        username: String,
        email: String,
        password: String,
        acceptsMarketing: Boolean,
        acceptsTerms: Boolean,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            applyResult(
                auth.register(username, email, password, acceptsMarketing, acceptsTerms),
                onSuccess,
            )
        }
    }

    fun googleLogin() {
        applyResult(auth.googleLogin()) {}
    }

    fun logout() {
        auth.logout()
        _state.update { it.copy(account = null, error = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun applyResult(result: AuthResult, onSuccess: () -> Unit) {
        if (result.success && result.account != null) {
            _state.update { it.copy(loading = false, error = null, account = result.account) }
            onSuccess()
        } else {
            _state.update { it.copy(loading = false, error = result.message ?: "Connexion impossible.") }
        }
    }
}
