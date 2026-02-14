package com.market.paresolvershop.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.auth.IsUserLoggedInUseCase
import com.market.paresolvershop.domain.cart.AddToCartUseCase
import com.market.paresolvershop.domain.cart.ClearCartUseCase
import com.market.paresolvershop.domain.cart.GetCartItemsUseCase
import com.market.paresolvershop.domain.cart.RemoveFromCartUseCase
import com.market.paresolvershop.domain.cart.UpdateCartQuantityUseCase
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
    val subtotal: Double = 0.0,
    val isValid: Boolean = true // Indica si todos los items tienen stock suficiente
)

sealed interface CartEvent {
    data class Success(val message: String) : CartEvent
    data class Error(val message: String) : CartEvent
}

class CartViewModel(
    private val addToCartUseCase: AddToCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = getCartItemsUseCase()
        .map { items ->
            val subtotal = items.sumOf { it.product.price * it.quantity }
            val isValid = items.all { it.quantity <= it.product.stock }
            CartUiState(items = items, subtotal = subtotal, isValid = isValid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CartUiState()
        )

    private val _eventFlow = MutableSharedFlow<CartEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun isUserLoggedIn(): Boolean {
        return isUserLoggedInUseCase()
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            when (val result = addToCartUseCase(product)) {
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
            when (val result = updateCartQuantityUseCase(productId, quantity)) {
                is DataResult.Success -> { /* No-op, la UI ya se actualiza */ }
                is DataResult.Error -> {
                    _eventFlow.emit(CartEvent.Error(result.message))
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            removeFromCartUseCase(productId)
            _eventFlow.emit(CartEvent.Success("Producto eliminado"))
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase()
            _eventFlow.emit(CartEvent.Success("Carrito vaciado"))
        }
    }
}
