package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckoutPaymentScreen(onPaymentSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pasarela de Pago")
        Text("(Stripe Integration Placeholder)")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onPaymentSuccess, modifier = Modifier.fillMaxWidth()) {
            Text("Pagar $ 110.00")
        }
    }
}
