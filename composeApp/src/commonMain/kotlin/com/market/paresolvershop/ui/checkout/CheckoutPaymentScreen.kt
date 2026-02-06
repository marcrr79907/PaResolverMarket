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
        val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }

        LaunchedEffect(state) {
            if (state is CheckoutPaymentUiState.Success) {
                navigator.replaceAll(CheckoutSummaryScreen)
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Order Summary", style = Typography.headlineMedium, fontFamily = SpaceGrotesk) },
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
                    color = Surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .navigationBarsPadding()
                    ) {
                        CostSummaryRow("Order Subtotal", "$$subtotal")
                        CostSummaryRow("Shipping & Fees", "$13.00")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Amount", style = Typography.titleLarge, fontFamily = Inter)
                            Text("$$total", style = Typography.headlineMedium, fontFamily = SpaceGrotesk, color = Primary)
                        }

                        Spacer(Modifier.height(16.dp))

                        if (state is CheckoutPaymentUiState.Error) {
                            Text(
                                (state as CheckoutPaymentUiState.Error).message, 
                                color = Error,
                                modifier = Modifier.padding(bottom = 12.dp),
                                style = Typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { viewModel.placeOrder(selectedAddress, cartItems, paymentMethod) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = OnSurface),
                            enabled = state !is CheckoutPaymentUiState.Loading
                        ) {
                            if (state is CheckoutPaymentUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceVariant, strokeWidth = 2.dp)
                            } else {
                                Text("Place Order", style = Typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
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
                Text("Deliver to", style = Typography.bodyLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = SurfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(FontAwesomeIcons.Solid.MapMarkerAlt, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("${selectedAddress.firstName} ${selectedAddress.lastName}", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(selectedAddress.addressLine, style = Typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Order Items", style = MaterialTheme.typography.bodyLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                    Text("${cartItems.size} items", style = Typography.labelLarge, color = OnSurfaceVariant)
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

                    // USO DEL COMPONENTE REUTILIZABLE CENTRALIZADO
                    ScrollIndicator(
                        visible = showScrollIndicator,
                        text = "More products",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                    )
                }

                Text("Payment Method", style = Typography.bodyLarge, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
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
            shape = MaterialTheme.shapes.small,
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
            Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        Text(
            "$${item.product.price}",
            style = MaterialTheme.typography.bodyLarge,
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
        modifier = modifier.height(56.dp).clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) Primary.copy(alpha = 0.1f) else Surface,
        border = BorderStroke(1.dp, if (isSelected) Primary else SoftGray)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isSelected) Primary else OnSurface)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontFamily = Inter, fontWeight = FontWeight.Bold, color = if (isSelected) Primary else OnSurface)
        }
    }
}

@Composable
fun CostSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = Inter, fontWeight = FontWeight.Medium)
    }
}
