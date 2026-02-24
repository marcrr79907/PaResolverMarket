package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StoreConfig
import com.market.paresolvershop.domain.store.GetStoreConfigUseCase
import com.market.paresolvershop.domain.store.UpdateStoreConfigUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface StoreManagementUiState {
    data object Loading : StoreManagementUiState
    data class Success(val config: StoreConfig) : StoreManagementUiState
    data class Error(val message: String) : StoreManagementUiState
}

sealed interface StoreManagementEvent {
    data class Success(val message: String) : StoreManagementEvent
    data class Error(val message: String) : StoreManagementEvent
}

class StoreManagementViewModel(
    private val getStoreConfigUseCase: GetStoreConfigUseCase,
    private val updateStoreConfigUseCase: UpdateStoreConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StoreManagementUiState>(StoreManagementUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StoreManagementEvent>()
    val events = _events.asSharedFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = StoreManagementUiState.Loading
            getStoreConfigUseCase.storeConfig.collect { config ->
                if (config != null) {
                    _uiState.value = StoreManagementUiState.Success(config)
                } else {
                    getStoreConfigUseCase()
                }
            }
        }
    }

    fun updateConfig(name: String, shipping: String, tax: String, currency: String) {
        viewModelScope.launch {
            val config = StoreConfig(
                storeName = name,
                shippingFee = shipping.toDoubleOrNull() ?: 0.0,
                taxFee = tax.toDoubleOrNull() ?: 0.0,
                currencySymbol = currency
            )

            when (val result = updateStoreConfigUseCase(config)) {
                is DataResult.Success -> _events.emit(StoreManagementEvent.Success("Configuración guardada"))
                is DataResult.Error -> _events.emit(StoreManagementEvent.Error(result.message))
            }
        }
    }
}
