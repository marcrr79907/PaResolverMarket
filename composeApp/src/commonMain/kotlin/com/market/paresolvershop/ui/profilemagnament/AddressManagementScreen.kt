package com.market.paresolvershop.ui.profilemagnament

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddressManagementScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis Direcciones")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(2) {
                Text("Dirección $it, La Habana, Cuba", modifier = Modifier.padding(8.dp))
            }
        }
        Button(onClick = { /* Agregar nueva */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar Nueva Dirección")
        }
    }
}
