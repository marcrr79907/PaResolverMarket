package com.market.paresolvershop.ui.admin

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.admin.components.AdminScaffold
import com.market.paresolvershop.ui.components.formatPrice
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object InventoryScreen : Screen {
    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<InventoryViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val deleteState by viewModel.deleteState.collectAsState()

        var query by remember { mutableStateOf("") }
        var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(deleteState) {
            when (val state = deleteState) {
                is DeleteProductState.Success -> {
                    snackbarHostState.showSnackbar("Producto eliminado.")
                    viewModel.resetDeleteState()
                }
                is DeleteProductState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetDeleteState()
                }
                else -> {}
            }
        }

        if (showDeleteDialog != null) {
            DeleteConfirmationDialog(
                productName = showDeleteDialog!!.name,
                onConfirm = { 
                    viewModel.deleteProduct(showDeleteDialog!!)
                    showDeleteDialog = null
                },
                onDismiss = { showDeleteDialog = null }
            )
        }

        AdminScaffold(
            title = "Gestión de Inventario",
            currentScreen = InventoryScreen,
            actions = {
                IconButton(onClick = { navigator.push(CreateProductScreen) }) {
                    Icon(FontAwesomeIcons.Solid.Plus, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            },
            extraHeader = {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar por nombre o ID...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SurfaceVariant,
                        focusedBorderColor = Primary
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
                when (val state = uiState) {
                    is InventoryUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is InventoryUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is InventoryUiState.Success -> {
                        val filteredProducts = if (query.isEmpty()) state.products 
                                              else state.products.filter { it.name.contains(query, ignoreCase = true) || it.id.contains(query) }

                        if (filteredProducts.isEmpty()) {
                            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(FontAwesomeIcons.Solid.BoxOpen, null, Modifier.size(64.dp), tint = SoftGray)
                                Text("No se encontraron productos", color = OnSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredProducts) { product ->
                                    InventoryProductCard(
                                        product = product,
                                        onEdit = { navigator.push(EditProductScreen(product)) },
                                        onDelete = { showDeleteDialog = product }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(product.categoryName ?: "Sin categoría", fontSize = 12.sp, color = OnSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    val stockColor = if (product.stock < 5) Error else Color(0xFF4CAF50)
                    Box(Modifier.size(8.dp).background(stockColor, CircleShape))
                    Text(" Stock: ${product.stock}", fontSize = 12.sp, color = stockColor, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Text("$${product.price.formatPrice()}", fontSize = 14.sp, color = Primary, fontWeight = FontWeight.ExtraBold)
                }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(FontAwesomeIcons.Solid.Edit, null, tint = Primary, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(FontAwesomeIcons.Solid.Trash, null, tint = Error, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar producto?", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
        text = { Text("¿Estás seguro de que quieres eliminar '$productName'? El producto se archivará pero seguirá visible en los pedidos antiguos.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("Eliminar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}
