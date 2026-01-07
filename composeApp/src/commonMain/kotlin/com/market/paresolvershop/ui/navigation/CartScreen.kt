package com.market.paresolvershop.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CartScreen(onCheckout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carrito de Compras")

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(3) {
                Text("Item en carrito #$it", modifier = Modifier.padding(8.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total:")
            Text("$ 300.00")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
            Text("Proceder al Pago")
        }
    }
}
