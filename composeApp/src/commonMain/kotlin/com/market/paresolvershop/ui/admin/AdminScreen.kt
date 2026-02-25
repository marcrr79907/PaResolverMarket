package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import androidx.compose.ui.text.font.FontWeight

object AdminScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Panel de Administrador", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Volver", modifier = Modifier.size(20.dp))
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Gestión de Inventario", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Button(
                    onClick = { navigator.push(InventoryScreen) }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Gestionar Productos e Inventario")
                }
                
                Button(
                    onClick = { navigator.push(CategoryManagementScreen) }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Gestionar Categorías")
                }

                Spacer(Modifier.height(8.dp))
                Text("Operaciones", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                Button(
                    onClick = { navigator.push(OrderManagementScreen) }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Gestionar Órdenes")
                }

                // NUEVO: Atajo a la configuración de la tienda
                Button(
                    onClick = { navigator.push(StoreManagementScreen) }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Ajustes de Tienda (Tasas y Nombre)")
                }
            }
        }
    }
}
