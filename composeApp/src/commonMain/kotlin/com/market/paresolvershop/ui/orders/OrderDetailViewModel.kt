package com.market.paresolvershop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.domain.cart.AddToCartUseCase
import com.market.paresolvershop.domain.cart.ClearCartUseCase
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.StoreConfig
import com.market.paresolvershop.domain.orders.GetOrderDetailsUseCase
import com.market.paresolvershop.domain.orders.GetOrderItemsUseCase
import com.market.paresolvershop.domain.store.GetStoreConfigUseCase
import com.market.paresolvershop.domain.payments.CreateStripeSessionUseCase
import com.market.paresolvershop.ui.checkout.CheckoutStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(
        val order: Order,
        val items: List<Pair<OrderItem, Product>>,
        val config: StoreConfig,
        val paymentStatus: CheckoutStatus = CheckoutStatus.Idle
    ) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

sealed interface OrderDetailEvent {
    data object ReOrderSuccess : OrderDetailEvent
    data class Error(val message: String) : OrderDetailEvent
    data object PaymentSuccess : OrderDetailEvent
}

class OrderDetailViewModel(
    private val orderId: String,
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val getOrderItemsUseCase: GetOrderItemsUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getStoreConfigUseCase: GetStoreConfigUseCase,
    private val createStripeSessionUseCase: CreateStripeSessionUseCase,
    private val authRepository: AuthRepository
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
            getStoreConfigUseCase()
            
            val orderResult = getOrderDetailsUseCase(orderId)
            val itemsResult = getOrderItemsUseCase(orderId)
            val config = getStoreConfigUseCase.storeConfig.value ?: StoreConfig()

            if (orderResult is DataResult.Success && itemsResult is DataResult.Success) {
                _uiState.value = OrderDetailUiState.Success(
                    order = orderResult.data,
                    items = itemsResult.data,
                    config = config
                )
            } else {
                val errorMsg = (orderResult as? DataResult.Error)?.message 
                    ?: (itemsResult as? DataResult.Error)?.message 
                    ?: "Error al cargar los detalles del pedido"
                _uiState.value = OrderDetailUiState.Error(errorMsg)
            }
        }
    }

    fun retryPayment(amount: Double) {
        val currentState = _uiState.value as? OrderDetailUiState.Success ?: return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(paymentStatus = CheckoutStatus.Loading)
            
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _uiState.value = currentState.copy(paymentStatus = CheckoutStatus.Error("Sesión expirada"))
                _eventFlow.emit(OrderDetailEvent.Error("Sesión expirada. Inicia sesión nuevamente."))
                return@launch
            }

            val customerName = user.name ?: "Cliente"
            val customerEmail = user.email ?: ""

            when (val result = createStripeSessionUseCase(orderId, amount, customerEmail, customerName)) {
                is DataResult.Success -> {
                    _uiState.value = currentState.copy(
                        paymentStatus = CheckoutStatus.StripeRedirect(
                            paymentIntent = result.data.paymentIntent,
                            ephemeralKey = result.data.ephemeralKey,
                            customer = result.data.customer,
                            publishableKey = result.data.publishableKey,
                            orderId = orderId
                        )
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = currentState.copy(paymentStatus = CheckoutStatus.Error(result.message))
                    _eventFlow.emit(OrderDetailEvent.Error(result.message))
                }
            }
        }
    }

    fun resetPaymentStatus() {
        val currentState = _uiState.value as? OrderDetailUiState.Success ?: return
        _uiState.value = currentState.copy(paymentStatus = CheckoutStatus.Idle)
    }

    fun reOrder(items: List<Pair<OrderItem, Product>>) {
        viewModelScope.launch {
            try {
                if (items.isEmpty()) {
                    _eventFlow.emit(OrderDetailEvent.Error("No hay productos para reordenar"))
                    return@launch
                }
                clearCartUseCase()
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
