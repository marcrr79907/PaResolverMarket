package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckoutSummaryScreen(onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Resumen del Pedido")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Subtotal: $ 100.00")
        Text("Envío: $ 10.00")
        Text("Total: $ 110.00")

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Ir a Pagar")
        }
    }
}
