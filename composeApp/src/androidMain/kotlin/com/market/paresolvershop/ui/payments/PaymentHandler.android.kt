package com.market.paresolvershop.ui.payments

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.market.paresolvershop.ui.theme.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
actual fun rememberPaymentHandler(
    onResult: (PaymentResult) -> Unit
): PaymentHandler {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    // Usamos el nuevo patrón de Builder para registrar el manejador de resultados
    // Esto reemplaza al antiguo rememberPaymentSheet deprecado
    val paymentSheet = PaymentSheet.Builder { result ->
        when (result) {
            is PaymentSheetResult.Completed -> onResult(PaymentResult.Completed)
            is PaymentSheetResult.Canceled -> onResult(PaymentResult.Canceled)
            is PaymentSheetResult.Failed -> onResult(PaymentResult.Failed(result.error.message ?: "Error desconocido"))
        }
    }.build()

    val brandColor = Primary.toArgb()
    val textColor = OnSurface.toArgb()
    val errorColor = Error.toArgb()
    val componentBg = SurfaceVariant.toArgb()

    return remember {
        object : PaymentHandler {
            override fun presentPaymentSheet(
                paymentIntentClientSecret: String,
                customerId: String?,
                customerEphemeralKeySecret: String?,
                publishableKey: String,
                merchantName: String
            ) {
                PaymentConfiguration.init(context, publishableKey)

                // Personalización estética usando los 11 parámetros Int requeridos
                val appearance = PaymentSheet.Appearance(
                    colorsLight = PaymentSheet.Colors(
                        primary = brandColor,
                        surface = android.graphics.Color.WHITE,
                        component = componentBg,
                        componentBorder = android.graphics.Color.LTGRAY,
                        componentDivider = android.graphics.Color.LTGRAY,
                        onComponent = textColor,
                        onSurface = textColor,
                        subtitle = android.graphics.Color.DKGRAY,
                        placeholderText = android.graphics.Color.GRAY,
                        appBarIcon = textColor,
                        error = errorColor
                    ),
                    shapes = PaymentSheet.Shapes(cornerRadiusDp = 16f, borderStrokeWidthDp = 1f),
                    typography = PaymentSheet.Typography.default
                )

                val configuration = PaymentSheet.Configuration(
                    merchantDisplayName = merchantName,
                    customer = if (customerId != null && customerEphemeralKeySecret != null) {
                        PaymentSheet.CustomerConfiguration(customerId, customerEphemeralKeySecret)
                    } else null,
                    appearance = appearance,
                    googlePay = PaymentSheet.GooglePayConfiguration(
                        environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                        countryCode = "US"
                    ),
                    allowsDelayedPaymentMethods = false
                )

                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
            }
        }
    }
}
