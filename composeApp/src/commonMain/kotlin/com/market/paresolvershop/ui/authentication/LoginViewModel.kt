package com.market.paresolvershop.ui.authentication

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.auth.SignInWithEmail
import com.market.paresolvershop.domain.auth.SignInWithGoogle
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val signInWithEmail: SignInWithEmail,
    private val signInWithGoogle: SignInWithGoogle
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = signInWithEmail(email, password)
            _uiState.value = when (result) {
                is DataResult.Success -> LoginUiState.Success
                is DataResult.Error -> LoginUiState.Error(result.message)
            }
        }
    }

    // Nueva función para manejar el login con Google
    fun onGoogleLoginSuccess(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = signInWithGoogle(idToken)
            _uiState.value = when (result) {
                is DataResult.Success -> LoginUiState.Success
                is DataResult.Error -> LoginUiState.Error(result.message)
            }
        }
    }

    fun onError(message: String) {
        _uiState.value = LoginUiState.Error(message)
    }
}