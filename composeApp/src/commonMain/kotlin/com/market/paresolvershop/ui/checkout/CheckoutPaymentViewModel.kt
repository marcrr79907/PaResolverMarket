package com.market.paresolvershop.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.domain.orders.PlaceOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CheckoutPaymentUiState {
    data object Idle : CheckoutPaymentUiState
    data object Loading : CheckoutPaymentUiState
    data class Success(val orderId: String) : CheckoutPaymentUiState
    data class Error(val message: String) : CheckoutPaymentUiState
}

class CheckoutPaymentViewModel(
    private val placeOrderUseCase: PlaceOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutPaymentUiState>(CheckoutPaymentUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun placeOrder(address: UserAddress, items: List<CartItem>, paymentMethod: String) {
        viewModelScope.launch {
            _uiState.value = CheckoutPaymentUiState.Loading
            
            val userId = address.userId ?: ""
            val addressId = address.id ?: ""
            // Nota: El cálculo del total debería idealmente ser validado en el dominio (UseCase)
            val subtotal = items.sumOf { it.product.price * it.quantity }
            val totalAmount = subtotal + 13.0 

            val order = Order(
                userId = userId,
                addressId = addressId,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod
            )

            // CORRECCIÓN: Pasamos 'items' (List<CartItem>) directamente. 
            // El UseCase se encarga del mapeo a OrderItem y de limpiar el carrito.
            when (val result = placeOrderUseCase(order, items)) {
                is DataResult.Success -> {
                    _uiState.value = CheckoutPaymentUiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = CheckoutPaymentUiState.Error(result.message)
                }
            }
        }
    }
}
