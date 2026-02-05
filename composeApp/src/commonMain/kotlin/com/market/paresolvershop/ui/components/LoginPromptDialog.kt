package com.market.paresolvershop.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.market.paresolvershop.ui.theme.SpaceGrotesk

@Composable
fun LoginPromptDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inicia sesión", fontFamily = SpaceGrotesk) },
        text = { Text("Para añadir productos al carrito y realizar compras, necesitas tener una cuenta activa.") },
        confirmButton = {
            Button(
                onClick = onLoginClick,
                shape = RoundedCornerShape(8.dp)
            ) { Text("Iniciar Sesión") }
        },
        dismissButton = {
            TextButton(onClick = onRegisterClick) { Text("Crear Cuenta") }
        }
    )
}
