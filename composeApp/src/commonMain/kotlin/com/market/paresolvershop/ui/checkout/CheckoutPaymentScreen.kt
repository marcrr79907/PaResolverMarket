package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.ui.cart.PaymentMethodCard
import com.market.paresolvershop.ui.theme.Inter
import com.market.paresolvershop.ui.theme.OnSurfaceVariant
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.SoftGray
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import com.market.paresolvershop.ui.theme.SurfaceVariant
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.CheckCircle
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.MoneyBillWave
import compose.icons.fontawesomeicons.solid.University
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
        val total = subtotal + 13.0 // Fees fixed for MVP

        LaunchedEffect(state) {
            if (state is CheckoutPaymentUiState.Success) {
                navigator.push(OrderSuccessScreen)
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

                // 1. Entrega en
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

                // 2. Método de Pago
                Text("Payment Method", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PaymentMethodCard(
                        title = "Cash",
                        subtitle = "Pay at delivery",
                        icon = FontAwesomeIcons.Solid.MoneyBillWave,
                        isSelected = paymentMethod == "Cash",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Cash" }
                    )
                    PaymentMethodCard(
                        title = "Bank Transfer",
                        subtitle = "Online payment",
                        icon = FontAwesomeIcons.Solid.University,
                        isSelected = paymentMethod == "Bank",
                        modifier = Modifier.weight(1f),
                        onClick = { paymentMethod = "Bank" }
                    )
                }

                Spacer(Modifier.weight(1f))

                // 3. Resumen de Costos
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    CostSummaryRow("Order Subtotal", "$$subtotal")
                    CostSummaryRow("Shipping & Fees", "$13.00")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$$total", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Primary)
                    }
                }

                Button(
                    onClick = { viewModel.placeOrder(selectedAddress, cartItems, paymentMethod) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                    enabled = state !is CheckoutPaymentUiState.Loading
                ) {
                    if (state is CheckoutPaymentUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
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

object OrderSuccessScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = Primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(FontAwesomeIcons.Solid.CheckCircle, null, modifier = Modifier.size(60.dp), tint = Primary)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Order Placed Successfully!", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(
                "Your order has been received and is being processed by the vendor.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = { navigator.popUntilRoot() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back to Home", fontWeight = FontWeight.Bold)
            }
        }
    }
}
