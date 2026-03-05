package com.market.paresolvershop.ui.orders

import androidx.compose.foundation.BorderStroke
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
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.StoreConfig
import com.market.paresolvershop.ui.checkout.CheckoutStatus
import com.market.paresolvershop.ui.checkout.CheckoutSummaryScreen
import com.market.paresolvershop.ui.navigation.bottombar.CartTab
import com.market.paresolvershop.ui.orders.components.OrderProductItemRow
import com.market.paresolvershop.ui.orders.components.StatusBadge
import com.market.paresolvershop.ui.payments.PaymentResult
import com.market.paresolvershop.ui.payments.rememberPaymentHandler
import com.market.paresolvershop.ui.profile.AddressManagementScreen
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.CreditCard
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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

        val paymentHandler = rememberPaymentHandler { result ->
            when (result) {
                is PaymentResult.Completed -> {
                    viewModel.fetchOrderDetails()
                    navigator.push(CheckoutSummaryScreen(orderId))
                }
                is PaymentResult.Failed, is PaymentResult.Canceled -> {
                    viewModel.resetPaymentStatus()
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is OrderDetailEvent.ReOrderSuccess -> {
                        tabNavigator.current = CartTab
                        navigator.popUntilRoot()
                    }
                    is OrderDetailEvent.PaymentSuccess -> viewModel.fetchOrderDetails()
                    is OrderDetailEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        if (uiState is OrderDetailUiState.Success) {
            val state = uiState as OrderDetailUiState.Success
            LaunchedEffect(state.paymentStatus) {
                if (state.paymentStatus is CheckoutStatus.StripeRedirect) {
                    val status = state.paymentStatus
                    paymentHandler.presentPaymentSheet(
                        paymentIntentClientSecret = status.paymentIntent,
                        customerId = status.customer,
                        customerEphemeralKeySecret = status.ephemeralKey,
                        publishableKey = status.publishableKey,
                        merchantName = state.config.storeName
                    )
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
                    is OrderDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                    is OrderDetailUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is OrderDetailUiState.Success -> {
                        OrderDetailContent(
                            orderId = orderId,
                            items = state.items,
                            order = state.order,
                            config = state.config,
                            paymentStatus = state.paymentStatus,
                            onAddAddressClick = { navigator.push(AddressManagementScreen()) },
                            onReOrderClick = { viewModel.reOrder(state.items) },
                            onRetryPayment = { viewModel.retryPayment(state.order.totalAmount) }
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
    order: Order,
    config: StoreConfig,
    paymentStatus: CheckoutStatus,
    onAddAddressClick: () -> Unit,
    onReOrderClick: () -> Unit,
    onRetryPayment: () -> Unit
) {
    val subtotal = items.sumOf { it.first.priceAtPurchase * it.first.quantity }
    val currency = config.currencySymbol
    
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Detalle del Pedido", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Primary, fontWeight = FontWeight.Bold)) { append("ID: ") }
                            append(orderId.take(8).uppercase())
                        }, fontSize = 12.sp)
                    }
                    StatusBadge(status = order.status)
                }
            }

            if (order.status == "unpaid") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusPending.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, StatusPending.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Pago pendiente", fontWeight = FontWeight.Bold, color = StatusPending)
                            Text("Tu pedido ha sido creado pero el pago no se completó. Pulsa abajo para intentarlo de nuevo.", fontSize = 12.sp, color = OnSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onRetryPayment,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPending),
                                enabled = paymentStatus !is CheckoutStatus.Loading
                            ) {
                                if (paymentStatus is CheckoutStatus.Loading) {
                                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(FontAwesomeIcons.Solid.CreditCard, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Pagar con Tarjeta", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dirección de Entrega", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    TextButton(onClick = onAddAddressClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Añadir dirección", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("Tel: ${order.recipientPhone ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fecha de realización", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    Text(order.createdAt?.take(16)?.replace("T", " ") ?: "Desconocida", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Método de pago", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                    val cardBrand = order.paymentDetails?.get("brand")?.jsonPrimitive?.contentOrNull
                    val cardLast4 = order.paymentDetails?.get("last4")?.jsonPrimitive?.contentOrNull

                    if (cardBrand != null && cardLast4 != null) {
                        Text("${cardBrand.uppercase()} **** $cardLast4", fontWeight = FontWeight.Bold, color = Primary, fontSize = 13.sp)
                    } else {
                        Text(if (order.paymentMethod == "Stripe") "Tarjeta / Apple Pay" else order.paymentMethod, fontWeight = FontWeight.Bold, color = Primary)
                    }
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
                    DetailRow("Subtotal", "$currency$subtotal")
                    DetailRow("Costo de Envío", "$currency${config.shippingFee}")
                    DetailRow("Tarifas de servicio", "$currency${config.taxFee}")
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$currency${order.totalAmount}", color = Primary, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }

        if (order.status != "unpaid") {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = onReOrderClick,
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Repetir Pedido", fontWeight = FontWeight.Bold)
                }
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
