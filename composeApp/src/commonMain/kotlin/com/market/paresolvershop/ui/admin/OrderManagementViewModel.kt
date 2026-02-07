package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderManagementUiState {
    data object Loading : OrderManagementUiState
    data class Success(val orders: List<Order>) : OrderManagementUiState
    data class Error(val message: String) : OrderManagementUiState
}

sealed interface OrderManagementEvent {
    data class Success(val message: String) : OrderManagementEvent
    data class Error(val message: String) : OrderManagementEvent
}

class OrderManagementViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderManagementUiState>(OrderManagementUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OrderManagementEvent>()
    val events = _events.asSharedFlow()

    init {
        fetchAllOrders()
    }

    fun fetchAllOrders() {
        viewModelScope.launch {
            _uiState.value = OrderManagementUiState.Loading
            when (val result = orderRepository.fetchAllOrdersAdmin()) {
                is DataResult.Success -> {
                    _uiState.value = OrderManagementUiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = OrderManagementUiState.Error(result.message)
                }
            }
        }
    }

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            // No cambiamos el estado global a Loading para no parpadear la lista entera,
            // pero podríamos manejar un estado de "procesando" por item si fuera necesario.
            when (val result = orderRepository.updateOrderStatus(orderId, newStatus)) {
                is DataResult.Success -> {
                    _events.emit(OrderManagementEvent.Success("Pedido actualizado a $newStatus"))
                    // Recargamos la lista para reflejar el cambio
                    refreshListSilently()
                }
                is DataResult.Error -> {
                    _events.emit(OrderManagementEvent.Error(result.message))
                }
            }
        }
    }

    private fun refreshListSilently() {
        viewModelScope.launch {
            val result = orderRepository.fetchAllOrdersAdmin()
            if (result is DataResult.Success) {
                _uiState.value = OrderManagementUiState.Success(result.data)
            }
        }
    }
}
