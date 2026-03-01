package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.market.paresolvershop.ui.orders.DateHeader
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import kotlinx.datetime.*

object OrderManagementScreen : Screen {

    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<OrderManagementViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val sortType by viewModel.sortType.collectAsState()
        val isAscending by viewModel.isAscending.collectAsState()
        val startDate by viewModel.startDate.collectAsState()
        val endDate by viewModel.endDate.collectAsState()
        
        val snackbarHostState = remember { SnackbarHostState() }
        var showStatusDialog by remember { mutableStateOf<Order?>(null) }
        var showSortMenu by remember { mutableStateOf(false) }
        var showRangePicker by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is OrderManagementEvent.Success -> snackbarHostState.showSnackbar(event.message)
                    is OrderManagementEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        if (showRangePicker) {
            val dateRangePickerState = rememberDateRangePickerState()
            DatePickerDialog(
                onDismissRequest = { showRangePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis
                        
                        if (startMillis != null) {
                            val start = Instant.fromEpochMilliseconds(startMillis)
                                .toLocalDateTime(TimeZone.UTC).date.toString()
                            
                            // Si end es null, tratamos como un solo día (start == end)
                            val end = if (endMillis != null) {
                                Instant.fromEpochMilliseconds(endMillis)
                                    .toLocalDateTime(TimeZone.UTC).date.toString()
                            } else start
                            
                            viewModel.setDateRange(start, end)
                        }
                        showRangePicker = false
                    }) { Text("Seleccionar") }
                },
                dismissButton = {
                    TextButton(onClick = { showRangePicker = false }) { Text("Cancelar") }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f),
                    title = { Text("Filtrar por fecha", modifier = Modifier.padding(16.dp)) },
                    headline = { 
                        Text(
                            "Selecciona un día o un rango", 
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    showModeToggle = true
                )
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
            currentScreen = OrderManagementScreen,
            actions = {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = if (isAscending) FontAwesomeIcons.Solid.SortAmountUp else FontAwesomeIcons.Solid.SortAmountDown,
                            contentDescription = "Ordenar",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        OrderSortMenuItem("Fecha", sortType == OrderSortType.DATE, isAscending) {
                            viewModel.toggleSort(OrderSortType.DATE); showSortMenu = false
                        }
                        OrderSortMenuItem("Monto", sortType == OrderSortType.AMOUNT, isAscending) {
                            viewModel.toggleSort(OrderSortType.AMOUNT); showSortMenu = false
                        }
                        OrderSortMenuItem("Cliente", sortType == OrderSortType.CUSTOMER, isAscending) {
                            viewModel.toggleSort(OrderSortType.CUSTOMER); showSortMenu = false
                        }
                    }
                }
                IconButton(onClick = { showRangePicker = true }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.CalendarAlt, 
                        contentDescription = "Filtrar por fecha", 
                        tint = if (startDate != null) StatusPending else Primary, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            extraHeader = {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Buscar por ID o cliente...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(FontAwesomeIcons.Solid.Times, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SurfaceVariant,
                            focusedBorderColor = Primary
                        )
                    )

                    if (uiState is OrderManagementUiState.Success) {
                        val state = uiState as OrderManagementUiState.Success
                        val statuses = listOf(null, "pending", "shipped", "delivered", "cancelled")
                        val selectedIndex = statuses.indexOf(state.selectedStatus)

                        SecondaryScrollableTabRow(
                            selectedTabIndex = if (selectedIndex != -1) selectedIndex else 0,
                            containerColor = Color.White,
                            edgePadding = 16.dp,
                            divider = {},
                            indicator = {
                                if (selectedIndex != -1) {
                                    val indicatorColor = when(state.selectedStatus) {
                                        "pending" -> StatusPending
                                        "shipped" -> StatusShipped
                                        "delivered" -> StatusDelivered
                                        "cancelled" -> StatusCancelled
                                        else -> Primary
                                    }
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(selectedIndex),
                                        color = indicatorColor
                                    )
                                }
                            }
                        ) {
                            statuses.forEach { status ->
                                val count = if (status == null) state.orders.size else state.orders.count { it.status == status }
                                val statusColor = when(status) {
                                    "pending" -> StatusPending
                                    "shipped" -> StatusShipped
                                    "delivered" -> StatusDelivered
                                    "cancelled" -> StatusCancelled
                                    else -> Primary
                                }

                                Tab(
                                    selected = state.selectedStatus == status,
                                    onClick = { viewModel.filterByStatus(status) },
                                    selectedContentColor = statusColor,
                                    unselectedContentColor = statusColor.copy(alpha = 0.6f),
                                    text = {
                                        Text(
                                            text = when(status) {
                                                null -> "Todos ($count)"
                                                "pending" -> "Pendientes ($count)"
                                                "shipped" -> "Enviados ($count)"
                                                "delivered" -> "Entregados ($count)"
                                                "cancelled" -> "Cancelados ($count)"
                                                else -> status
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = if (state.selectedStatus == status) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Background)) {
                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
                
                when (val state = uiState) {
                    is OrderManagementUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                    is OrderManagementUiState.Error -> Text(state.message, color = StatusCancelled, modifier = Modifier.align(Alignment.Center))
                    is OrderManagementUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Resumen de la vista actual
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (startDate != null) {
                                    val dateText = if (startDate == endDate) startDate else "$startDate → $endDate"
                                    Surface(
                                        color = StatusPending.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        onClick = { viewModel.setDateRange(null, null) }
                                    ) {
                                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(dateText!!, fontSize = 11.sp, color = StatusPending, fontWeight = FontWeight.Bold)
                                            Icon(FontAwesomeIcons.Solid.Times, null, Modifier.size(10.dp).padding(start = 4.dp), tint = StatusPending)
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.width(1.dp))
                                }
                                
                                Surface(color = Primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        "Ingresos: $${state.totalAmountInView}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (state.filteredOrders.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(FontAwesomeIcons.Solid.Inbox, null, modifier = Modifier.size(64.dp), tint = SoftGray)
                                    Spacer(Modifier.height(16.dp))
                                    Text("No hay pedidos que coincidan", color = OnSurfaceVariant)
                                }
                            } else {
                                val groupedOrders = state.filteredOrders.groupBy { it.createdAt?.take(10) ?: "Desconocido" }
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    groupedOrders.forEach { (date, ordersInDate) ->
                                        stickyHeader {
                                            DateHeader(date)
                                        }
                                        items(ordersInDate) { order ->
                                            AdminOrderCard(
                                                order = order,
                                                onClick = { navigator.push(AdminOrderDetailScreen(order.id!!)) },
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
    }
}

@Composable
fun OrderSortMenuItem(label: String, isSelected: Boolean, isAscending: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        imageVector = if (isAscending) FontAwesomeIcons.Solid.ArrowUp else FontAwesomeIcons.Solid.ArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Primary
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
fun AdminOrderCard(
    order: Order,
    onClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    val statusColor = when (order.status) {
        "pending" -> StatusPending
        "shipped" -> StatusShipped
        "delivered" -> StatusDelivered
        "cancelled" -> StatusCancelled
        else -> OnSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Pedido #${order.id?.take(8)}",
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk,
                        fontSize = 16.sp
                    )
                    Text(
                        order.customerName ?: "Cliente desconocido",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when(order.status) {
                            "pending" -> "Pendiente"
                            "shipped" -> "Enviado"
                            "delivered" -> "Entregado"
                            "cancelled" -> "Cancelado"
                            else -> order.status
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total", fontSize = 11.sp, color = OnSurfaceVariant)
                    Text(
                        "$${order.totalAmount}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary,
                        fontSize = 18.sp,
                        fontFamily = SpaceGrotesk
                    )
                }

                Button(
                    onClick = onStatusClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.05f), contentColor = Primary),
                    elevation = null
                ) {
                    Icon(FontAwesomeIcons.Solid.ExchangeAlt, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cambiar Estado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        title = { Text("Actualizar Estado", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
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
                        Text(
                            text = when(status) {
                                "pending" -> "Pendiente"
                                "shipped" -> "Enviado"
                                "delivered" -> "Entregado"
                                "cancelled" -> "Cancelado"
                                else -> status
                            },
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnSurfaceVariant) }
        }
    )
}
