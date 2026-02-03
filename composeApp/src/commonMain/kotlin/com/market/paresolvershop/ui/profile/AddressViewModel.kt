package com.market.paresolvershop.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.UserAddress
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddressUiState {
    data object Loading : AddressUiState
    data class Success(val addresses: List<UserAddress>) : AddressUiState
    data class Error(val message: String) : AddressUiState
}

sealed interface AddressEvent {
    data class Success(val message: String) : AddressEvent
    data class Error(val message: String) : AddressEvent
}

class AddressViewModel(
    private val repository: AddressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddressUiState>(AddressUiState.Loading)
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddressEvent>()
    val eventFlow: SharedFlow<AddressEvent> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.addresses.collect { list ->
                _uiState.value = AddressUiState.Success(list)
            }
        }
        refreshAddresses()
    }

    fun refreshAddresses() {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            when (val result = repository.fetchAddresses()) {
                is DataResult.Error -> _uiState.value = AddressUiState.Error(result.message)
                is DataResult.Success -> {
                    // Forzamos el estado Success con el valor actual del repositorio
                    // por si el flow no emitió (ej. lista vacía a lista vacía)
                    _uiState.value = AddressUiState.Success(repository.addresses.value)
                }
            }
        }
    }

    fun saveAddress(address: UserAddress) {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            when (val result = repository.saveAddress(address)) {
                is DataResult.Error -> {
                    _eventFlow.emit(AddressEvent.Error(result.message))
                    _uiState.value = AddressUiState.Success(repository.addresses.value)
                }
                is DataResult.Success -> {
                    _eventFlow.emit(AddressEvent.Success("Dirección guardada correctamente"))
                    // Success se actualizará vía collect del init
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteAddress(addressId)) {
                is DataResult.Error -> _eventFlow.emit(AddressEvent.Error(result.message))
                is DataResult.Success -> _eventFlow.emit(AddressEvent.Success("Dirección eliminada"))
            }
        }
    }

    fun setDefault(addressId: String) {
        viewModelScope.launch {
            repository.setDefaultAddress(addressId)
        }
    }
}
