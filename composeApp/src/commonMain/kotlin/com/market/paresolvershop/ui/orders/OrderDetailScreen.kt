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
import com.market.paresolvershop.ui.profile.ProfileUiState
import com.market.paresolvershop.ui.profile.ProfileViewModel
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.UserCircle
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
        val profileViewModel = koinViewModel<ProfileViewModel>()
        
        val uiState by viewModel.uiState.collectAsState()
        val profileState by profileViewModel.uiState.collectAsState()
        val isAdmin = (profileState as? ProfileUiState.Authenticated)?.isAdmin ?: false
        
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
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Back", modifier = Modifier.size(18.dp))
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
                            isAdmin = isAdmin,
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
    isAdmin: Boolean,
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
                        Text("Order Information", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Primary, fontWeight = FontWeight.Bold)) {
                                append("ID: ")
                            }
                            append(orderId.take(8))
                        }, fontSize = 12.sp)
                    }
                    StatusBadge(status = order.status)
                }
            }

            // Customer Info (Solo Admin)
            if (isAdmin && order.customerName != null) {
                item {
                    Text("Customer Info", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(FontAwesomeIcons.Solid.UserCircle, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Text(order.customerName, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Delivery Address Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delivery to", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    if (!isAdmin) {
                        TextButton(onClick = onAddAddressClick, contentPadding = PaddingValues(0.dp)) {
                            Text("Add new address", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
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
                            Text(order.recipientAddress ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(order.fullRecipientName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            Text(order.recipientPhone ?: "N/A", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // Delivery Time
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Order Date", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    Text(order.createdAt?.take(16)?.replace("T", " ") ?: "N/A", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            items(items) { (orderItem, product) ->
                OrderProductItemRow(
                    name = product.name,
                    imageUrl = product.imageUrl,
                    category = product.categoryName ?: "Sin categoría",
                    quantity = orderItem.quantity,
                    price = orderItem.priceAtPurchase
                )
            }

            item {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    DetailRow("Subtotal (${items.size} items)", "$$subtotal")
                    DetailRow("Ship Fee", "$13.00")
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$${subtotal + 13.0}", color = Primary, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            // Note Section
            item {
                Text("Note", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        "No special notes for this order.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        // El botón Re-Order no es necesario para el admin en la gestión de otros
        if (!isAdmin) {
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
                    Text("Re-Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
