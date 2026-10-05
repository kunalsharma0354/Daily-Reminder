package com.nexora.reminder.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.reminder.auth.LoginResult
import com.nexora.reminder.auth.NexoraAuthApi
import com.nexora.reminder.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: NexoraAuthApi,
    private val session: SessionManager
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun login(username: String, password: String) {
        if (_ui.value.loading) return
        viewModelScope.launch {
            _ui.value = LoginUiState(loading = true)
            when (val r = api.login(username, password)) {
                is LoginResult.Success -> {
                    session.save(r.token, r.username, r.expiresAt)
                    _ui.value = LoginUiState(loading = false)
                }
                is LoginResult.Error -> {
                    _ui.value = LoginUiState(loading = false, error = r.message)
                }
            }
        }
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }
}
