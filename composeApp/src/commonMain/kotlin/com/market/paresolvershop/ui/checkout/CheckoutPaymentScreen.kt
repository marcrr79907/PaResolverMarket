package com.market.paresolvershop.ui.checkout

import androidx.compose.animation.*
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
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
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
        val state by viewModel.uiState.collectAsState()
        
        var paymentMethod by remember { mutableStateOf("Cash") }
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val total = subtotal + 13.0

        val listState = rememberLazyListState()
        // Indicador de si hay más items abajo
        val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }

        LaunchedEffect(state) {
            if (state is CheckoutPaymentUiState.Success) {
                navigator.replaceAll(CheckoutSummaryScreen)
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Order Summary", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
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
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .navigationBarsPadding()
                    ) {
                        // Resumen de Costos Fijo en BottomBar
                        CostSummaryRow("Order Subtotal", "$$subtotal")
                        CostSummaryRow("Shipping & Fees", "$13.00")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Amount", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("$$total", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Primary)
                        }

                        Spacer(Modifier.height(16.dp))

                        if (state is CheckoutPaymentUiState.Error) {
                            Text(
                                (state as CheckoutPaymentUiState.Error).message, 
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { viewModel.placeOrder(selectedAddress, cartItems, paymentMethod) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnSurface),
                            enabled = state !is CheckoutPaymentUiState.Loading
                        ) {
                            if (state is CheckoutPaymentUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceVariant, strokeWidth = 2.dp)
                            } else {
                                Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                // SECCIÓN FIJA SUPERIOR: Dirección
                Text("Deliver to", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(FontAwesomeIcons.Solid.MapMarkerAlt, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("${selectedAddress.firstName} ${selectedAddress.lastName}", fontWeight = FontWeight.Bold)
                            Text(selectedAddress.addressLine, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }

                // SECCIÓN FIJA SUPERIOR: Pago
                Text("Payment Method", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocalPaymentMethodCard(
                        title = "Cash",
                        icon = FontAwesomeIcons.Solid.MoneyBillWave,
                        isSelected = paymentMethod == "Cash",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Cash" }
                    )
                    LocalPaymentMethodCard(
                        title = "Transfer",
                        icon = FontAwesomeIcons.Solid.University,
                        isSelected = paymentMethod == "Bank",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Bank" }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // SECCIÓN DESPLAZABLE: Items
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Order Items", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${cartItems.size} items", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }

                Box(modifier = Modifier.weight(1f).padding(top = 12.dp)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(cartItems) { item ->
                            CheckoutItemRow(item)
                        }
                    }

                    // INDICADOR DE SCROLL: "More items"
                    AnimatedVisibility(
                        visible = showScrollIndicator,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                    ) {
                        Surface(
                            color = Primary.copy(alpha = 0.9f),
                            shape = CircleShape,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(FontAwesomeIcons.Solid.ChevronDown, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("More products", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariant.copy(alpha = 0.3f)
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
                contentScale = ContentScale.Fit
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text("Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        Text(
            "$${item.product.price}",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
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
        modifier = modifier.height(56.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) Primary else SoftGray)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isSelected) Primary else OnSurface)
            Spacer(Modifier.width(8.dp))
            Text(title, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Primary else OnSurface)
        }
    }
}

@Composable
fun CostSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Text(value, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
