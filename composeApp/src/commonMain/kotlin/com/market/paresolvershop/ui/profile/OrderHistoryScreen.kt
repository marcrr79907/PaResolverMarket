package com.market.paresolvershop.ui.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrderHistoryScreen(onOrderClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { Text("Mis Órdenes") }
        items(5) {
            Text("Orden #$it - Entregado", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
