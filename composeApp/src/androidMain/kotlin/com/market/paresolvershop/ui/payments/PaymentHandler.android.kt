package com.market.paresolvershop.ui.payments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
actual fun rememberPaymentHandler(
    onResult: (PaymentResult) -> Unit
): PaymentHandler {
    val context = LocalContext.current

    // Usamos el nuevo patrón de Builder para registrar el manejador de resultados
    // Esto reemplaza al antiguo rememberPaymentSheet deprecado
    val paymentSheet = PaymentSheet.Builder { result ->
        when (result) {
            is PaymentSheetResult.Completed -> onResult(PaymentResult.Completed)
            is PaymentSheetResult.Canceled -> onResult(PaymentResult.Canceled)
            is PaymentSheetResult.Failed -> onResult(PaymentResult.Failed(result.error.message ?: "Error desconocido"))
        }
    }.build()

    return remember {
        object : PaymentHandler {
            override fun presentPaymentSheet(
                paymentIntentClientSecret: String,
                customerId: String?,
                customerEphemeralKeySecret: String?,
                publishableKey: String
            ) {
                PaymentConfiguration.init(context, publishableKey)

                val configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "PaResolver Market",
                    customer = if (customerId != null && customerEphemeralKeySecret != null) {
                        PaymentSheet.CustomerConfiguration(customerId, customerEphemeralKeySecret)
                    } else null,
                    googlePay = PaymentSheet.GooglePayConfiguration(
                        environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                        countryCode = "US"
                    ),
                    allowsDelayedPaymentMethods = false
                )

                // Lanzamos el Payment Sheet usando la instancia construida
                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
            }
        }
    }
}
