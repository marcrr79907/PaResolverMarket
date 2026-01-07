package com.market.paresolvershop.ui.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckoutShippingScreen(onNext: () -> Unit) {
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dirección de Envío (Cuba)")
        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = address, onValueChange = { address = it }, label = { Text("Calle y número") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = city, onValueChange = { city = it }, label = { Text("Provincia/Municipio") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Continuar al Resumen")
        }
    }
}
