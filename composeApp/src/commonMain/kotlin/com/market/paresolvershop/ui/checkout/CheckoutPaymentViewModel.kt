package com.market.paresolvershop.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.UserAddress
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CheckoutPaymentUiState {
    data object Idle : CheckoutPaymentUiState
    data object Loading : CheckoutPaymentUiState
    data class Success(val orderId: String) : CheckoutPaymentUiState
    data class Error(val message: String) : CheckoutPaymentUiState
}

class CheckoutPaymentViewModel(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutPaymentUiState>(CheckoutPaymentUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun placeOrder(address: UserAddress, items: List<CartItem>, paymentMethod: String) {
        viewModelScope.launch {
            _uiState.value = CheckoutPaymentUiState.Loading
            
            val userId = address.userId ?: ""
            val addressId = address.id ?: ""
            val totalAmount = items.sumOf { it.product.price * it.quantity } + 13.0 // Subtotal + Fees

            val order = Order(
                userId = userId,
                addressId = addressId,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod
            )

            val orderItems = items.map { item ->
                OrderItem(
                    orderId = "", // Se asigna en el repositorio
                    productId = item.product.id,
                    quantity = item.quantity,
                    priceAtPurchase = item.product.price
                )
            }

            when (val result = orderRepository.createOrder(order, orderItems)) {
                is DataResult.Success -> {
                    cartRepository.clearCart() // Vaciar el carrito tras éxito
                    _uiState.value = CheckoutPaymentUiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = CheckoutPaymentUiState.Error(result.message)
                }
            }
        }
    }
}
