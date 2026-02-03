package com.market.paresolvershop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderHistoryUiState {
    data object Loading : OrderHistoryUiState
    data class Success(val orders: List<Order>) : OrderHistoryUiState
    data class Error(val message: String) : OrderHistoryUiState
}

class OrderHistoryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            orderRepository.orders.collect { ordersList ->
                _uiState.value = OrderHistoryUiState.Success(ordersList)
            }
        }
        // Forzar la primera carga desde Supabase
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            if (_uiState.value !is OrderHistoryUiState.Success) {
                _uiState.value = OrderHistoryUiState.Loading
            }
            
            val result = orderRepository.fetchOrders()
            if (result is DataResult.Error) {
                _uiState.value = OrderHistoryUiState.Error(result.message)
            }
        }
    }
}
