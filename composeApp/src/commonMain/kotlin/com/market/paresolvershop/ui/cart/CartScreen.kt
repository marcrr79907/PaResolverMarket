package com.market.paresolvershop.ui.cart

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.components.ScrollIndicator
import com.market.paresolvershop.ui.components.formatPrice
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Minus
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.Trash
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cartViewModel: CartViewModel, onCheckout: () -> Unit) {
    val uiState by cartViewModel.uiState.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    val showScrollIndicator by remember { derivedStateOf { listState.canScrollForward } }

    var showClearCartDialog by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        cartViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CartEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is CartEvent.Success -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showClearCartDialog) {
        AlertDialog(
            onDismissRequest = { showClearCartDialog = false },
            title = {
                Text(
                    "Vaciar Carrito",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text("¿Estás seguro de que deseas eliminar todos los productos del carrito?") },
            confirmButton = {
                Button(
                    onClick = {
                        cartViewModel.clearCart()
                        showClearCartDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCartDialog = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    if (itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            title = {
                Text(
                    "Eliminar Producto",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text("¿Deseas eliminar '${itemToRemove!!.name}' del carrito?") },
            confirmButton = {
                Button(
                    onClick = {
                        cartViewModel.removeFromCart(itemToRemove!!.id)
                        itemToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Cart",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearCartDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                                .background(Error.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                FontAwesomeIcons.Solid.Trash,
                                contentDescription = "Vaciar",
                                tint = Error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.items.isEmpty()) {
                EmptyCartView()
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    if (!uiState.isValid) {
                        StockWarningView()
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                        ) {
                            items(uiState.items) { item ->
                                CartListItem(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        cartViewModel.updateQuantity(item.product.id, newQuantity)
                                    },
                                    onRemove = { itemToRemove = item.product }
                                )
                            }
                        }
                        ScrollIndicator(
                            visible = showScrollIndicator,
                            text = "More items",
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                        )
                    }

                    CartSummarySection(
                        subtotal = uiState.subtotal,
                        isValid = uiState.isValid,
                        onCheckout = onCheckout
                    )
                }
            }
        }
    }
}

@Composable
fun CartListItem(item: CartItem, onQuantityChange: (Int) -> Unit, onRemove: () -> Unit) {
    val isOutOfStock = item.quantity > item.product.stock

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isOutOfStock) Error.copy(alpha = 0.5f) else SurfaceVariant),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier.size(85.dp).clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariant.copy(alpha = 0.5f)).padding(8.dp)
            ) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info del producto
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.product.name,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Botón de eliminar mejorado
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                            .background(Error.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            FontAwesomeIcons.Solid.Trash,
                            contentDescription = "Eliminar",
                            tint = Error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = "$${item.product.price.formatPrice()} c/u",
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontFamily = Inter
                )

                if (isOutOfStock) {
                    Text(
                        "Solo hay ${item.product.stock} disponibles",
                        color = Error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${(item.product.price * item.quantity).formatPrice()}",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isOutOfStock) Error else Primary
                    )

                    // Selector de cantidad
                    QuantitySelector(
                        quantity = item.quantity,
                        maxStock = item.product.stock,
                        onQuantityChange = onQuantityChange
                    )
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, maxStock: Int, onQuantityChange: (Int) -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Botón Minus
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable { onQuantityChange(quantity - 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                FontAwesomeIcons.Solid.Minus,
                contentDescription = "Restar",
                modifier = Modifier.size(10.dp),
                tint = OnSurface
            )
        }

        Text(
            text = quantity.toString(),
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Botón Plus
        val canAdd = quantity < maxStock
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (canAdd) Primary else SoftGray.copy(alpha = 0.3f))
                .clickable(enabled = canAdd) { onQuantityChange(quantity + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                FontAwesomeIcons.Solid.Plus,
                contentDescription = "Añadir",
                modifier = Modifier.size(10.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun CartSummarySection(subtotal: Double, isValid: Boolean, onCheckout: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))

        SummaryRow("Subtotal", "$${subtotal.formatPrice()}")
        SummaryRow("Envío", "$10.00")
        SummaryRow("Impuestos", "$3.00")

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Total a pagar",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                "$${(subtotal + 13).formatPrice()}",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = OnSurface
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onCheckout,
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OnSurface,
                disabledContainerColor = SoftGray
            )
        ) {
            Text(
                "Finalizar Compra",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = SpaceGrotesk
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 14.sp, fontFamily = Inter)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFamily = Inter)
    }
}

@Composable
fun EmptyCartView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            FontAwesomeIcons.Solid.ShoppingCart,
            null,
            modifier = Modifier.size(80.dp),
            tint = SoftGray.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Tu carrito está vacío",
            fontFamily = SpaceGrotesk,
            fontSize = 18.sp,
            color = OnSurfaceVariant
        )
        Text(
            "¡Añade algunos productos para comenzar!",
            fontSize = 14.sp,
            color = OnSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StockWarningView() {
    Surface(
        color = Error.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        border = BorderStroke(1.dp, Error.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "⚠️ Algunos productos exceden el stock disponible. Por favor, ajusta las cantidades.",
                color = Error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
