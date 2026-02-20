package com.market.paresolvershop.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.address.*
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
    private val getAddressesUseCase: GetAddressesUseCase,
    private val saveAddressUseCase: SaveAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddressUiState>(AddressUiState.Loading)
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddressEvent>()
    val eventFlow: SharedFlow<AddressEvent> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            getAddressesUseCase.addresses.collect { list ->
                _uiState.value = AddressUiState.Success(list)
            }
        }
        refreshAddresses()
    }

    fun refreshAddresses() {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            when (val result = getAddressesUseCase()) {
                is DataResult.Error -> _uiState.value = AddressUiState.Error(result.message)
                is DataResult.Success -> {
                    _uiState.value = AddressUiState.Success(getAddressesUseCase.addresses.value)
                }
            }
        }
    }

    fun saveAddress(address: UserAddress) {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            when (val result = saveAddressUseCase(address)) {
                is DataResult.Error -> {
                    _eventFlow.emit(AddressEvent.Error(result.message))
                    _uiState.value = AddressUiState.Success(getAddressesUseCase.addresses.value)
                }
                is DataResult.Success -> {
                    _eventFlow.emit(AddressEvent.Success("Dirección guardada correctamente"))
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            when (val result = deleteAddressUseCase(addressId)) {
                is DataResult.Error -> _eventFlow.emit(AddressEvent.Error(result.message))
                is DataResult.Success -> _eventFlow.emit(AddressEvent.Success("Dirección eliminada"))
            }
        }
    }

    fun setDefault(addressId: String) {
        viewModelScope.launch {
            when (val result = setDefaultAddressUseCase(addressId)) {
                is DataResult.Error -> _eventFlow.emit(AddressEvent.Error(result.message))
                is DataResult.Success -> _eventFlow.emit(AddressEvent.Success("Dirección predeterminada establecida"))
            }
        }
    }
}
