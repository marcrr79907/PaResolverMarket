package com.market.paresolvershop.ui.payments

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPaymentHandler(
    onResult: (PaymentResult) -> Unit
): PaymentHandler

interface PaymentHandler {
    fun presentPaymentSheet(
        paymentIntentClientSecret: String,
        customerId: String? = null,
        customerEphemeralKeySecret: String? = null,
        publishableKey: String,
        merchantName: String
    )
}

sealed class PaymentResult {
    data object Completed : PaymentResult()
    data object Canceled : PaymentResult()
    data class Failed(val message: String) : PaymentResult()
}
