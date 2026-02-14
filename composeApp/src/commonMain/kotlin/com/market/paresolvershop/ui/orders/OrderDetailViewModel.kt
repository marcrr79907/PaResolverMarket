package com.market.paresolvershop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.cart.AddToCartUseCase
import com.market.paresolvershop.domain.cart.ClearCartUseCase
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.orders.GetOrderDetailsUseCase
import com.market.paresolvershop.domain.orders.GetOrderItemsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(
        val order: Order,
        val items: List<Pair<OrderItem, Product>>
    ) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

sealed interface OrderDetailEvent {
    data object ReOrderSuccess : OrderDetailEvent
    data class Error(val message: String) : OrderDetailEvent
}

class OrderDetailViewModel(
    private val orderId: String,
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val getOrderItemsUseCase: GetOrderItemsUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<OrderDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchOrderDetails()
    }

    fun fetchOrderDetails() {
        viewModelScope.launch {
            _uiState.value = OrderDetailUiState.Loading
            
            val orderResult = getOrderDetailsUseCase(orderId)
            val itemsResult = getOrderItemsUseCase(orderId)

            if (orderResult is DataResult.Success && itemsResult is DataResult.Success) {
                _uiState.value = OrderDetailUiState.Success(
                    order = orderResult.data,
                    items = itemsResult.data
                )
            } else {
                val errorMsg = (orderResult as? DataResult.Error)?.message 
                    ?: (itemsResult as? DataResult.Error)?.message 
                    ?: "Error al cargar los detalles del pedido"
                _uiState.value = OrderDetailUiState.Error(errorMsg)
            }
        }
    }

    fun reOrder(items: List<Pair<OrderItem, Product>>) {
        viewModelScope.launch {
            try {
                if (items.isEmpty()) {
                    _eventFlow.emit(OrderDetailEvent.Error("No hay productos para reordenar"))
                    return@launch
                }
                
                // Clean cart before re-order
                clearCartUseCase()
                
                // Add products with quantity
                items.forEach { (orderItem, product) ->
                    addToCartUseCase(product, orderItem.quantity)
                }
                
                _eventFlow.emit(OrderDetailEvent.ReOrderSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(OrderDetailEvent.Error("Error al procesar el Re-Order"))
            }
        }
    }
}
