package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.ui.components.ScrollIndicator
import com.market.paresolvershop.ui.components.formatPrice
import com.market.paresolvershop.ui.payments.PaymentResult
import com.market.paresolvershop.ui.payments.rememberPaymentHandler
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

data class CheckoutPaymentScreen(
    val selectedAddress: UserAddress,
    val cartItems: List<CartItem>
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CheckoutPaymentViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val scope = rememberCoroutineScope()
        
        var paymentMethod by remember { mutableStateOf("Stripe") }
        
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val config = uiState.config
        val currency = config?.currencySymbol ?: "$"
        val shipping = config?.shippingFee ?: 0.0
        val tax = config?.taxFee ?: 0.0
        val total = subtotal + shipping + tax

        val listState = rememberLazyListState()
        val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }

        // Inicializamos el manejador de pagos nativo de Stripe
        val paymentHandler = rememberPaymentHandler { result ->
            when (result) {
                is PaymentResult.Completed -> {
                    val orderId = (uiState.status as? CheckoutStatus.StripeRedirect)?.orderId ?: ""
                    navigator.replaceAll(CheckoutSummaryScreen(orderId))
                }
                is PaymentResult.Failed, is PaymentResult.Canceled -> {
                    viewModel.resetStatus() // Volvemos al estado Idle
                }
            }
        }

        LaunchedEffect(uiState.status) {
            when (val status = uiState.status) {
                is CheckoutStatus.Success -> {
                    navigator.replaceAll(CheckoutSummaryScreen(status.orderId))
                }
                is CheckoutStatus.StripeRedirect -> {
                    paymentHandler.presentPaymentSheet(
                        paymentIntentClientSecret = status.paymentIntent,
                        customerId = status.customer,
                        customerEphemeralKeySecret = status.ephemeralKey,
                        publishableKey = status.publishableKey,
                        merchantName = config?.storeName ?: "PaResolver Shop"
                    )
                }
                else -> {} // Loading e Idle no requieren navegación
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Finalizar Compra", style = Typography.titleLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
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
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    border = BorderStroke(1.dp, SurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .navigationBarsPadding()
                    ) {
                        CostSummaryRow("Subtotal", "$currency${subtotal.formatPrice()}")
                        CostSummaryRow("Envío a Cuba", "$currency${shipping.formatPrice()}")
                        CostSummaryRow("Tarifas de servicio", "$currency${tax.formatPrice()}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", style = Typography.titleMedium, fontFamily = Inter, fontWeight = FontWeight.Bold)
                            Text("$currency${total.formatPrice()}", style = Typography.headlineMedium, fontFamily = SpaceGrotesk, color = Primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(20.dp))

                        if (uiState.status is CheckoutStatus.Error) {
                            Surface(
                                color = Error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    (uiState.status as CheckoutStatus.Error).message, 
                                    color = Error,
                                    modifier = Modifier.padding(12.dp),
                                    style = Typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.placeOrder(selectedAddress, cartItems, paymentMethod)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnSurface),
                            enabled = uiState.status !is CheckoutStatus.Loading && config != null
                        ) {
                            if (uiState.status is CheckoutStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                val buttonText = if (paymentMethod == "Stripe") "Pagar con Tarjeta" else "Confirmar Pedido"
                                Text(buttonText, style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                Text("Dirección de Entrega en Cuba", style = Typography.titleSmall, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, SurfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(FontAwesomeIcons.Solid.MapMarkerAlt, null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("${selectedAddress.firstName} ${selectedAddress.lastName}", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(selectedAddress.addressLine, style = Typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Método de Pago (EE.UU.)", style = Typography.titleSmall, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                Row(modifier = Modifier.padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocalPaymentMethodCard(
                        title = "Tarjeta / Apple Pay",
                        icon = FontAwesomeIcons.Solid.CreditCard,
                        isSelected = paymentMethod == "Stripe",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Stripe" }
                    )
                    LocalPaymentMethodCard(
                        title = "Zelle / Otros",
                        icon = FontAwesomeIcons.Solid.ExchangeAlt,
                        isSelected = paymentMethod == "Manual",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Manual" }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Resumen de Productos", style = Typography.titleSmall, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                    Surface(color = SurfaceVariant, shape = CircleShape) {
                        Text("${cartItems.size} ítems", style = Typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Box(modifier = Modifier.weight(1f).padding(top = 12.dp)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(cartItems) { item ->
                            CheckoutItemRow(item, currency)
                        }
                    }

                    ScrollIndicator(
                        visible = showScrollIndicator,
                        text = "Desliza para ver más",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(cartItems.size - 1)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CostSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = Typography.bodyMedium, color = OnSurfaceVariant)
        Text(value, style = Typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LocalPaymentMethodCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.White,
        border = BorderStroke(2.dp, if (isSelected) Primary else SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (isSelected) Primary else OnSurfaceVariant, modifier = Modifier.size(24.dp))
            Text(title, style = Typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Primary else OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun CheckoutItemRow(item: CartItem, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariant.copy(alpha = 0.3f)
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(item.product.name, style = Typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${item.quantity} x $currency${item.product.price.formatPrice()}", style = Typography.bodySmall, color = OnSurfaceVariant)
        }
        Text("$currency${(item.product.price * item.quantity).formatPrice()}", style = Typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
