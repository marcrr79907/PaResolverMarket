package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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

        // EFECTO DE NAVEGACIÓN AUTOMÁTICA HACIA SUMMARY
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Entrega en
                Text("Deliver to", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

                Spacer(Modifier.height(16.dp))

                // Método de Pago
                Text("Payment Method", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocalPaymentMethodCard(
                        title = "Cash",
                        subtitle = "Pay at delivery",
                        icon = FontAwesomeIcons.Solid.MoneyBillWave,
                        isSelected = paymentMethod == "Cash",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Cash" }
                    )
                    LocalPaymentMethodCard(
                        title = "Bank Transfer",
                        subtitle = "Online payment",
                        icon = FontAwesomeIcons.Solid.University,
                        isSelected = paymentMethod == "Bank",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Bank" }
                    )
                }

                Spacer(Modifier.weight(1f))

                // Resumen de Costos
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    CostSummaryRow("Order Subtotal", "$$subtotal")
                    CostSummaryRow("Shipping & Fees", "$13.00")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$$total", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Primary)
                    }
                }

                if (state is CheckoutPaymentUiState.Error) {
                    Text(
                        (state as CheckoutPaymentUiState.Error).message, 
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { viewModel.placeOrder(selectedAddress, cartItems, paymentMethod) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
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
}

@Composable
fun LocalPaymentMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) Primary else SoftGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isSelected) Primary else OnSurface)
                Spacer(Modifier.width(8.dp))
                Text(title, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) Primary else OnSurface)
            }
            Spacer(Modifier.height(8.dp))
            Text(subtitle, fontSize = 10.sp, color = OnSurfaceVariant, lineHeight = 12.sp)
        }
    }
}

@Composable
fun CostSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 14.sp)
        Text(value, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
