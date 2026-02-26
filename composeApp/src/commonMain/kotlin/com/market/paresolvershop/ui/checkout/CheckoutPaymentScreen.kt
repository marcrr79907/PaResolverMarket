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
        
        var paymentMethod by remember { mutableStateOf("Cash") }
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        
        val config = uiState.config
        val currency = config?.currencySymbol ?: "$"
        val shipping = config?.shippingFee ?: 0.0
        val tax = config?.taxFee ?: 0.0
        val total = subtotal + shipping + tax

        val listState = rememberLazyListState()
        val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }

        LaunchedEffect(uiState.status) {
            if (uiState.status is CheckoutStatus.Success) {
                navigator.replaceAll(CheckoutSummaryScreen((uiState.status as CheckoutStatus.Success).orderId))
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Resumen del Pedido", style = Typography.titleLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
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
                        CostSummaryRow("Envío", "$currency${shipping.formatPrice()}")
                        CostSummaryRow("Impuestos", "$currency${tax.formatPrice()}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total a pagar", style = Typography.titleMedium, fontFamily = Inter, fontWeight = FontWeight.Bold)
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
                            onClick = { viewModel.placeOrder(selectedAddress, cartItems, paymentMethod) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnSurface),
                            enabled = uiState.status !is CheckoutStatus.Loading && config != null
                        ) {
                            if (uiState.status is CheckoutStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Confirmar Pedido", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
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
                Text("Enviar a", style = Typography.titleSmall, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Productos", style = Typography.titleSmall, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
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
                        text = "Ver más",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(cartItems.size - 1)
                            }
                        }
                    )
                }

                Text("Método de Pago", style = Typography.titleSmall, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                Row(modifier = Modifier.padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocalPaymentMethodCard(
                        title = "Efectivo",
                        icon = FontAwesomeIcons.Solid.MoneyBillWave,
                        isSelected = paymentMethod == "Cash",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Cash" }
                    )
                    LocalPaymentMethodCard(
                        title = "Transferencia",
                        icon = FontAwesomeIcons.Solid.University,
                        isSelected = paymentMethod == "Bank",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Bank" }
                    )
                }
            }
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
            Text("Cantidad: ${item.quantity}", style = Typography.bodySmall, color = OnSurfaceVariant)
        }
        Text(
            "$currency${(item.product.price * item.quantity).formatPrice()}",
            style = Typography.bodyLarge,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
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
        modifier = modifier.height(64.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) Primary else SoftGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isSelected) Primary else OnSurface)
            Spacer(Modifier.width(8.dp))
            Text(title, style = Typography.bodyMedium, fontFamily = Inter, fontWeight = FontWeight.Bold, color = if (isSelected) Primary else OnSurface)
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
        Text(value, style = Typography.bodyMedium, fontFamily = Inter, fontWeight = FontWeight.SemiBold)
    }
}
