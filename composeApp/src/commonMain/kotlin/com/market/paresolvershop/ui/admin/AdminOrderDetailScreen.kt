package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.ui.admin.components.AdminScaffold
import com.market.paresolvershop.ui.orders.OrderDetailUiState
import com.market.paresolvershop.ui.orders.OrderDetailViewModel
import com.market.paresolvershop.ui.orders.components.OrderProductItemRow
import com.market.paresolvershop.ui.orders.components.StatusBadge
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

data class AdminOrderDetailScreen(val orderId: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<OrderDetailViewModel> { parametersOf(orderId) }
        val adminViewModel = koinViewModel<OrderManagementViewModel>() // Para actualizar estados
        val uiState by viewModel.uiState.collectAsState()
        
        var showStatusDialog by remember { mutableStateOf(false) }

        AdminScaffold(
            title = "Gestión de Pedido",
            currentScreen = OrderManagementScreen, // Mantenemos el foco en Órdenes
            actions = {
                IconButton(onClick = { showStatusDialog = true }) {
                    Icon(FontAwesomeIcons.Solid.Edit, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
                when (val state = uiState) {
                    is OrderDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                    is OrderDetailUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is OrderDetailUiState.Success -> {
                        val order = state.order
                        
                        if (showStatusDialog) {
                            StatusSelectionDialog(
                                currentStatus = order.status,
                                onStatusSelected = { newStatus ->
                                    adminViewModel.updateStatus(order.id!!, newStatus)
                                    showStatusDialog = false
                                    navigator.pop() // Volvemos atrás tras actualizar
                                },
                                onDismiss = { showStatusDialog = false }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Card de Información del Cliente
                            item {
                                AdminSectionCard("Información del Cliente", FontAwesomeIcons.Solid.User) {
                                    Column {
                                        Text(order.customerName ?: "Desconocido", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("ID Usuario: ${order.userId}", fontSize = 11.sp, color = OnSurfaceVariant)
                                    }
                                }
                            }

                            // Card de Entrega
                            item {
                                AdminSectionCard("Detalles de Entrega", FontAwesomeIcons.Solid.MapMarkerAlt) {
                                    Column {
                                        Text(order.fullRecipientName, fontWeight = FontWeight.Bold)
                                        Text(order.recipientAddress ?: "Sin dirección", fontSize = 14.sp)
                                        Text("Tel: ${order.recipientPhone}", fontSize = 14.sp)
                                        Text("Ciudad: ${order.recipientCity}", fontSize = 14.sp)
                                    }
                                }
                            }

                            // Estado y Pago
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AdminSectionCard("Estado", FontAwesomeIcons.Solid.InfoCircle, Modifier.weight(1f)) {
                                        StatusBadge(order.status)
                                    }
                                    AdminSectionCard("Pago", FontAwesomeIcons.Solid.CreditCard, Modifier.weight(1f)) {
                                        Text(order.paymentMethod.uppercase(), fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                            }

                            // Resumen de Productos
                            item {
                                Text("Productos", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            items(state.items) { (item, product) ->
                                OrderProductItemRow(
                                    name = product.name,
                                    imageUrl = product.imageUrl,
                                    category = product.categoryName ?: "General",
                                    quantity = item.quantity,
                                    price = item.priceAtPurchase
                                )
                            }

                            // Totales
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White
                                ) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("Subtotal", color = OnSurfaceVariant)
                                            Text("$${state.order.totalAmount - 13.0}")
                                        }
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("Costo de Envío", color = OnSurfaceVariant)
                                            Text("$13.00")
                                        }
                                        HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.5f))
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text("$${state.order.totalAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Primary)
                                        }
                                    }
                                }
                            }
                            
                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = Typography.labelLarge, color = OnSurfaceVariant)
            }
            content()
        }
    }
}
