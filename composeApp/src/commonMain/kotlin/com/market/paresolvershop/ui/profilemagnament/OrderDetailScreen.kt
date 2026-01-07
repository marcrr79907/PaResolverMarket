package com.market.paresolvershop.ui.profilemagnament

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrderDetailScreen(orderId: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Detalle de Orden: $orderId")
        Text("Estado: En camino")
        Text("Items: ...")
    }
}
