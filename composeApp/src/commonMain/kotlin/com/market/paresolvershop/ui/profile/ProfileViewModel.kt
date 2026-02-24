package com.market.paresolvershop.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.model.AuthUserEntity
import com.market.paresolvershop.domain.auth.DeleteAccountUseCase
import com.market.paresolvershop.domain.auth.ObserveAuthState
import com.market.paresolvershop.domain.auth.SignOut
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object NotAuthenticated : ProfileUiState
    data class Authenticated(val user: AuthUserEntity, val isAdmin: Boolean = user.role == "admin") : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface ProfileEvent {
    data class Success(val message: String) : ProfileEvent
    data class Error(val message: String) : ProfileEvent
}

class ProfileViewModel(
    observeAuthState: ObserveAuthState,
    private val signOut: SignOut,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<ProfileUiState> = observeAuthState()
        .map { user ->
            if (user != null) {
                ProfileUiState.Authenticated(user)
            } else {
                ProfileUiState.NotAuthenticated
            }
        }
        .onStart { emit(ProfileUiState.Loading) }
        .catch { emit(ProfileUiState.Error(it.message ?: "Error desconocido")) }
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

    fun deleteAccount() {
        viewModelScope.launch {
            when (val result = deleteAccountUseCase()) {
                is DataResult.Success -> {
                    _events.emit(ProfileEvent.Success("Cuenta eliminada con éxito"))
                    signOut() // Cerrar sesión tras borrar cuenta
                }
                is DataResult.Error -> {
                    _events.emit(ProfileEvent.Error(result.message))
                }
            }
        }
    }
}
