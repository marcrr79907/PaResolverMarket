package com.market.paresolvershop.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.ui.checkout.CheckoutItemRow
import com.market.paresolvershop.ui.components.ScrollIndicator
import com.market.paresolvershop.ui.theme.Inter
import com.market.paresolvershop.ui.theme.OnSurface
import com.market.paresolvershop.ui.theme.OnSurfaceVariant
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.Secondary
import com.market.paresolvershop.ui.theme.SoftGray
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import com.market.paresolvershop.ui.theme.SurfaceVariant
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Minus
import compose.icons.fontawesomeicons.solid.MoneyBillWave
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.University
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cartViewModel: CartViewModel, onCheckout: () -> Unit) {
    val uiState by cartViewModel.uiState.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }

    LaunchedEffect(Unit) {
        cartViewModel.eventFlow.collectLatest { event ->
            if (event is CartEvent.Error) {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Cart", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        FontAwesomeIcons.Solid.ShoppingCart, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = SoftGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Tu carrito está vacío", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).padding(top = 12.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.items) { item ->
                            CartListItem(
                                item = item,
                                onQuantityChange = { newQuantity ->
                                    cartViewModel.updateQuantity(item.product.id, newQuantity)
                                }
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    ScrollIndicator(
                        visible = showScrollIndicator,
                        text = "More products",
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }

                // Resumen de costos
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    CostRow("Subtotal", "$${uiState.subtotal}")
                    CostRow("Fee Delivery", "$10")
                    CostRow("Total Tax", "$3")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$${uiState.subtotal + 13}", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
                ) {
                    Text("Check Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CartListItem(item: CartItem, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen redondeada con fondo sutil
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceVariant)
                .padding(8.dp)
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("$${item.product.price / item.quantity}", color = Primary, fontSize = 12.sp) // Precio unitario
            Spacer(Modifier.height(12.dp))
            Text("$${item.product.price}", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Selector de cantidad (+ / -) estilizado
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, SoftGray)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                IconButton(onClick = { onQuantityChange(item.quantity - 1) }, modifier = Modifier.size(24.dp)) {
                    Icon(FontAwesomeIcons.Solid.Minus, contentDescription = null, modifier = Modifier.size(12.dp), tint = Secondary)
                }
                Text(
                    text = "${item.quantity}",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary,
                    modifier = Modifier.size(24.dp).clickable { onQuantityChange(item.quantity + 1) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(FontAwesomeIcons.Solid.Plus, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CostRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 14.sp)
        Text(value, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun PaymentMethodCard(
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
