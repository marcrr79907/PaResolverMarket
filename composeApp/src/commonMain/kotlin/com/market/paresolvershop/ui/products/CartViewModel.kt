package com.market.paresolvershop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0
)

sealed interface CartEvent {
    data class Success(val message: String) : CartEvent
    data class Error(val message: String) : CartEvent
}

class CartViewModel(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cartRepository.getCartItems()
        .map { items ->
            val subtotal = items.sumOf { it.product.price * it.quantity }
            CartUiState(items = items, subtotal = subtotal)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CartUiState()
        )

    private val _eventFlow = MutableSharedFlow<CartEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUser() != null
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            when (val result = cartRepository.addToCart(product)) {
                is DataResult.Success -> {
                    _eventFlow.emit(CartEvent.Success("${product.name} añadido al carrito"))
                }
                is DataResult.Error -> {
                    _eventFlow.emit(CartEvent.Error(result.message))
                }
            }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}
