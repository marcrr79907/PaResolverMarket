package com.market.paresolvershop.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.address.GetAddressesUseCase
import com.market.paresolvershop.domain.cart.GetCartItemsUseCase
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.UserAddress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CheckoutUiState {
    data object Loading : CheckoutUiState
    data class Success(
        val addresses: List<UserAddress>,
        val cartItems: List<CartItem>,
        val subtotal: Double
    ) : CheckoutUiState
    data class Error(val message: String) : CheckoutUiState
}

sealed interface CheckoutEvent {
    data class Success(val message: String) : CheckoutEvent
    data class Error(val message: String) : CheckoutEvent
}

class CheckoutViewModel(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase
) : ViewModel() {

    private val _errorState = MutableStateFlow<String?>(null)

    private val _eventFlow = MutableSharedFlow<CheckoutEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val uiState: StateFlow<CheckoutUiState> = combine(
        getAddressesUseCase.addresses,
        getCartItemsUseCase(),
        _errorState
    ) { addresses, items, error ->
        // Si hay un error y no hay datos previos, mostramos pantalla de error
        if (error != null && addresses.isEmpty()) {
            CheckoutUiState.Error(error)
        } else {
            val subtotal = items.sumOf { it.product.price * it.quantity }
            CheckoutUiState.Success(
                addresses = addresses,
                cartItems = items,
                subtotal = subtotal
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState.Loading
    )

    init {
        loadCheckoutData()
    }

    fun loadCheckoutData(isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            // No cambiamos el estado global a Loading si ya hay datos (UX silencioso)
            val result = getAddressesUseCase()
            if (result is DataResult.Error) {
                // Notificamos el error vía evento (para Snackbar)
                _eventFlow.emit(CheckoutEvent.Error(result.message))
                // Persistimos el error en el estado si es necesario
                _errorState.value = result.message
            } else {
                _errorState.value = null
                // Si el usuario refrescó manualmente, confirmamos éxito
                if (isManualRefresh) {
                    _eventFlow.emit(CheckoutEvent.Success("Datos actualizados correctamente"))
                }
            }
        }
    }
}
