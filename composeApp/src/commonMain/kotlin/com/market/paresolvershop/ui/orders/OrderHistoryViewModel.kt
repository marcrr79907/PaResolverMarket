package com.market.paresolvershop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.orders.GetOrderHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderHistoryUiState {
    data object Loading : OrderHistoryUiState
    data class Success(val orders: List<Order>) : OrderHistoryUiState
    data class Error(val message: String) : OrderHistoryUiState
}

class OrderHistoryViewModel(
    private val getOrderHistoryUseCase: GetOrderHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getOrderHistoryUseCase.orders.collect { ordersList ->
                _uiState.value = OrderHistoryUiState.Success(ordersList)
            }
        }
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            if (_uiState.value !is OrderHistoryUiState.Success) {
                _uiState.value = OrderHistoryUiState.Loading
            }
            
            val result = getOrderHistoryUseCase()
            if (result is DataResult.Error) {
                _uiState.value = OrderHistoryUiState.Error(result.message)
            }
        }
    }
}
