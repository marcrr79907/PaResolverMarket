package com.market.paresolvershop.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.domain.model.StoreConfig
import com.market.paresolvershop.domain.orders.PlaceOrderUseCase
import com.market.paresolvershop.domain.store.GetStoreConfigUseCase
import com.market.paresolvershop.domain.payments.CreateStripeSessionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckoutPaymentUiState(
    val status: CheckoutStatus = CheckoutStatus.Idle,
    val config: StoreConfig? = null
)

sealed interface CheckoutStatus {
    data object Idle : CheckoutStatus
    data object Loading : CheckoutStatus
    data class Success(val orderId: String) : CheckoutStatus
    data class StripeRedirect(
        val paymentIntent: String,
        val ephemeralKey: String,
        val customer: String,
        val publishableKey: String,
        val orderId: String
    ) : CheckoutStatus
    data class Error(val message: String) : CheckoutStatus
}

class CheckoutPaymentViewModel(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getStoreConfigUseCase: GetStoreConfigUseCase,
    private val createStripeSessionUseCase: CreateStripeSessionUseCase
) : ViewModel() {

    private val _status = MutableStateFlow<CheckoutStatus>(CheckoutStatus.Idle)

    val uiState: StateFlow<CheckoutPaymentUiState> = combine(
        _status,
        getStoreConfigUseCase.storeConfig
    ) { status, config ->
        CheckoutPaymentUiState(status = status, config = config)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutPaymentUiState()
    )

    init {
        viewModelScope.launch {
            getStoreConfigUseCase()
        }
    }

    fun placeOrder(address: UserAddress, items: List<CartItem>, paymentMethod: String) {
        viewModelScope.launch {
            _status.value = CheckoutStatus.Loading
            
            val config = uiState.value.config ?: StoreConfig()
            val userId = address.userId ?: ""
            val addressId = address.id ?: ""

            val subtotal = items.sumOf { it.product.price * it.quantity }
            val totalAmount = subtotal + config.shippingFee + config.taxFee

            // DEFINIMOS EL ESTADO INICIAL: 
            // Si es Stripe, la orden nace como "unpaid". 
            // Si es Manual (Zelle), nace como "pending" para que el admin la verifique.
            val initialStatus = if (paymentMethod == "Stripe") "unpaid" else "pending"

            val order = Order(
                userId = userId,
                addressId = addressId,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                status = initialStatus
            )

            when (val result = placeOrderUseCase(order, items)) {
                is DataResult.Success -> {
                    val orderId = result.data
                    
                    if (paymentMethod == "Stripe") {
                        when (val stripeResult = createStripeSessionUseCase(orderId, totalAmount)) {
                            is DataResult.Success -> {
                                _status.value = CheckoutStatus.StripeRedirect(
                                    paymentIntent = stripeResult.data.paymentIntent,
                                    ephemeralKey = stripeResult.data.ephemeralKey,
                                    customer = stripeResult.data.customer,
                                    publishableKey = stripeResult.data.publishableKey,
                                    orderId = orderId
                                )
                            }
                            is DataResult.Error -> {
                                _status.value = CheckoutStatus.Error("Error en pasarela: ${stripeResult.message}")
                            }
                        }
                    } else {
                        _status.value = CheckoutStatus.Success(orderId)
                    }
                }
                is DataResult.Error -> {
                    _status.value = CheckoutStatus.Error(result.message)
                }
            }
        }
    }

    fun resetStatus() {
        _status.value = CheckoutStatus.Idle
    }
}
