package com.market.paresolvershop.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.navigation.bottombar.CartTab
import com.market.paresolvershop.ui.orders.components.OrderProductItemRow
import com.market.paresolvershop.ui.orders.components.StatusBadge
import com.market.paresolvershop.ui.profile.AddressManagementScreen
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

data class OrderDetailScreen(val orderId: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        
        val viewModel = koinViewModel<OrderDetailViewModel> { parametersOf(orderId) }
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is OrderDetailEvent.ReOrderSuccess -> {
                        tabNavigator.current = CartTab
                        navigator.popUntilRoot()
                    }
                    is OrderDetailEvent.Error -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier.padding(start = 12.dp).background(SurfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Atrás", modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
                when (val state = uiState) {
                    is OrderDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                    is OrderDetailUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Error)
                    }
                    is OrderDetailUiState.Success -> {
                        OrderDetailContent(
                            orderId = orderId,
                            items = state.items,
                            order = state.order,
                            onAddAddressClick = { navigator.push(AddressManagementScreen()) },
                            onReOrderClick = { viewModel.reOrder(state.items) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailContent(
    orderId: String, 
    items: List<Pair<OrderItem, Product>>,
    order: com.market.paresolvershop.domain.model.Order,
    onAddAddressClick: () -> Unit,
    onReOrderClick: () -> Unit
) {
    val subtotal = items.sumOf { it.first.priceAtPurchase * it.first.quantity }
    
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con Status
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Detalle del Pedido", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Primary, fontWeight = FontWeight.Bold)) {
                                append("ID: ")
                            }
                            append(orderId.take(8).uppercase())
                        }, fontSize = 12.sp)
                    }
                    StatusBadge(status = order.status)
                }
            }

            // Delivery Address Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dirección de Entrega", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    TextButton(onClick = onAddAddressClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Gestionar direcciones", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(FontAwesomeIcons.Solid.MapMarkerAlt, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(order.recipientAddress ?: "Dirección no especificada", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(order.fullRecipientName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            Text("Tel: ${order.recipientPhone ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // Order Date
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fecha de realización", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    Text(order.createdAt?.take(16)?.replace("T", " ") ?: "Desconocida", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text("Resumen de compra", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
            }

            items(items) { (orderItem, product) ->
                OrderProductItemRow(
                    name = product.name,
                    imageUrl = product.imageUrl,
                    category = product.categoryName ?: "General",
                    quantity = orderItem.quantity,
                    price = orderItem.priceAtPurchase
                )
            }

            item {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    DetailRow("Subtotal (${items.size} productos)", "$${subtotal}")
                    DetailRow("Costo de Envío", "$13.00")
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$${subtotal + 13.0}", color = Primary, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Button(
                onClick = onReOrderClick,
                modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Repetir Pedido", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
