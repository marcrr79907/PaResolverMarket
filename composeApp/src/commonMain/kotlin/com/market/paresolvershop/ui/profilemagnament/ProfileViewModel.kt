package com.market.paresolvershop.ui.profilemagnament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.model.AuthUserEntity
import com.market.paresolvershop.domain.auth.ObserveAuthState
import com.market.paresolvershop.domain.auth.SignOut
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object NotAuthenticated : ProfileUiState
    data class Authenticated(val user: AuthUserEntity, val isAdmin: Boolean = user.role == "admin") : ProfileUiState
    data class Error(val message: String) : ProfileUiState // Estado de error añadido
}

class ProfileViewModel(
    observeAuthState: ObserveAuthState,
    private val signOut: SignOut
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = observeAuthState()
        .map { user ->
            if (user != null) {
                ProfileUiState.Authenticated(user)
            } else {
                ProfileUiState.NotAuthenticated
            }
        }
        .onStart { emit(ProfileUiState.Loading) } // Emitir Loading al empezar
        .catch { emit(ProfileUiState.Error(it.message ?: "Error desconocido")) } // Capturar errores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState.Loading
        )

    fun logOut() {
        viewModelScope.launch {
            signOut()
        }
    }
}
