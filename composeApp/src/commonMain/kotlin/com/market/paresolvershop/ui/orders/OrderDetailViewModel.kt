package com.market.paresolvershop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(val items: List<Pair<OrderItem, Product>>) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

class OrderDetailViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchOrderDetails()
    }

    fun fetchOrderDetails() {
        viewModelScope.launch {
            _uiState.value = OrderDetailUiState.Loading
            when (val result = orderRepository.getOrderItems(orderId)) {
                is DataResult.Success -> _uiState.value = OrderDetailUiState.Success(result.data)
                is DataResult.Error -> _uiState.value = OrderDetailUiState.Error(result.message)
            }
        }
    }
}
