package com.market.paresolvershop.ui.profilemagnament

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    userEmail: String,
    userName: String,
    isAdmin: Boolean,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = userName, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Admin (condicional)
        if (isAdmin) {
            Button(
                onClick = onNavigateToAdmin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Panel de Administrador")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth()) {
            Text("Historial de Órdenes")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToAddresses, modifier = Modifier.fillMaxWidth()) {
            Text("Gestionar Direcciones")
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun GuestProfileScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido a PaResolver", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Inicia sesión para ver tu perfil y pedidos", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }
    }
}
