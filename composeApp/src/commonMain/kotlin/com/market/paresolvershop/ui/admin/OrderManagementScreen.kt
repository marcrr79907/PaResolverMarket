package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.ui.admin.components.AdminScaffold
import com.market.paresolvershop.ui.orders.OrderDetailScreen
import com.market.paresolvershop.ui.orders.components.OrderCard
import com.market.paresolvershop.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object OrderManagementScreen : Screen {

    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<OrderManagementViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        var showStatusDialog by remember { mutableStateOf<Order?>(null) }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is OrderManagementEvent.Success -> snackbarHostState.showSnackbar(event.message)
                    is OrderManagementEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        if (showStatusDialog != null) {
            StatusSelectionDialog(
                currentStatus = showStatusDialog!!.status,
                onStatusSelected = { newStatus ->
                    viewModel.updateStatus(showStatusDialog!!.id!!, newStatus)
                    showStatusDialog = null
                },
                onDismiss = { showStatusDialog = null }
            )
        }

        AdminScaffold(
            title = "Gestión de Pedidos",
            currentScreen = OrderManagementScreen
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
                
                when (val state = uiState) {
                    is OrderManagementUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                    is OrderManagementUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is OrderManagementUiState.Success -> {
                        if (state.orders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay pedidos en el sistema", color = OnSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.orders) { order ->
                                    OrderCard(
                                        order = order,
                                        isAdmin = true,
                                        onClick = { 
                                            order.id?.let { id ->
                                                navigator.push(OrderDetailScreen(id))
                                            }
                                        },
                                        onStatusClick = { showStatusDialog = order }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSelectionDialog(
    currentStatus: String,
    onStatusSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val statuses = listOf("pending", "shipped", "delivered", "cancelled")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Estado", fontFamily = SpaceGrotesk) },
        text = {
            Column {
                statuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusSelected(status) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = status == currentStatus, onClick = null)
                        Text(status.replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
