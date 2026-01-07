package com.market.paresolvershop.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.auth.SignInWithGoogle
import com.market.paresolvershop.domain.auth.SignUpWithEmail
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(
    private val signUpWithEmail: SignUpWithEmail,
    // 1. Inyectamos el caso de uso para el login/registro con Google
    private val signInWithGoogle: SignInWithGoogle
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onRegisterClick(name: String, email: String, pass: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = RegisterUiState.Error("Completa todos los campos")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = RegisterUiState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val result = signUpWithEmail(name, email, pass)
            _uiState.value = when (result) {
                is DataResult.Success -> RegisterUiState.Success
                is DataResult.Error -> RegisterUiState.Error(result.message)
            }
        }
    }

    // 2. Nueva función para manejar el flujo de Google
    fun onGoogleRegisterSuccess(idToken: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            // Reutilizamos el mismo caso de uso que el Login, ya que Firebase crea la cuenta si no existe.
            val result = signInWithGoogle(idToken)
            _uiState.value = when (result) {
                is DataResult.Success -> RegisterUiState.Success
                is DataResult.Error -> RegisterUiState.Error(result.message)
            }
        }
    }

    // Función auxiliar para mostrar errores desde la UI
    fun onError(message: String) {
        _uiState.value = RegisterUiState.Error(message)
    }
}
