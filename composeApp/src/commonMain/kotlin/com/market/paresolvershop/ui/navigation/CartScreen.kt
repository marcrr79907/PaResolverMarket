package com.market.paresolvershop.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.ui.products.CartEvent
import com.market.paresolvershop.ui.products.CartViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Trash
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cartViewModel: CartViewModel, onCheckout: () -> Unit) {
    val uiState by cartViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<CartItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cartViewModel.eventFlow.collectLatest { event ->
            if (event is CartEvent.Error) {
                snackbarHostState.showSnackbar(event.message)
            }
            // Los eventos de éxito se manejan en ProductDetailScreen, no aquí.
        }
    }

    showDeleteDialog?.let { item ->
        RemoveFromCartDialog(
            productName = item.product.name,
            onConfirm = {
                cartViewModel.removeFromCart(item.product.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.items) { item ->
                        CartListItem(
                            item = item,
                            onQuantityChange = { newQuantity ->
                                cartViewModel.updateQuantity(item.product.id, newQuantity)
                            },
                            onRemove = { showDeleteDialog = item }
                        )
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Subtotal:", style = MaterialTheme.typography.titleLarge)
                    Text("$ ${uiState.subtotal}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth(), enabled = uiState.items.isNotEmpty()) {
                    Text("Proceder al Pago")
                }
            }
        }
    }
}

@Composable
fun CartListItem(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.product.imageUrl,
            contentDescription = item.product.name,
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$ ${item.product.price}", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cantidad:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                // Simple + / - buttons for quantity (could be a TextField)
                OutlinedButton(onClick = { onQuantityChange(item.quantity - 1) }, enabled = item.quantity > 0) { Text("-") }
                Text(text = "${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedButton(onClick = { onQuantityChange(item.quantity + 1) }) { Text("+") }
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                FontAwesomeIcons.Solid.Trash,
                modifier = Modifier
                    .size(24.dp),
                contentDescription = "Eliminar del carrito"
            )
        }
    }
}

@Composable
fun RemoveFromCartDialog(productName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Producto") },
        text = { Text("¿Estás seguro de que quieres eliminar '$productName' de tu carrito?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
