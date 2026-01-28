package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.Product
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Plus
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

        var showDeleteDialog by remember { mutableStateOf<Product?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(deleteState) {
            when (val state = deleteState) {
                is DeleteProductState.Success -> {
                    snackbarHostState.showSnackbar("Producto eliminado con éxito.")
                    viewModel.resetDeleteState()
                }
                is DeleteProductState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetDeleteState()
                }
                else -> {}
            }
        }

        showDeleteDialog?.let {
            DeleteConfirmationDialog(
                productName = it.name,
                onConfirm = { viewModel.deleteProduct(it) },
                onDismiss = { showDeleteDialog = null }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inventario de Productos") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Volver")
                        }
                    }
                )
            },
            floatingActionButton = {
                 FloatingActionButton(onClick = { navigator.push(CreateProductScreen) }) {
                     Icon(
                        FontAwesomeIcons.Solid.Plus,
                        contentDescription = "Añadir Producto",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (val state = uiState) {
                    is InventoryUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is InventoryUiState.Success -> {
                        if (state.products.isEmpty()) {
                            Text(
                                "No hay productos disponibles.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                items(state.products) { product ->
                                    AdminProductCard(
                                        product = product,
                                        onEditClick = { navigator.push(EditProductScreen(product)) },
                                        onDeleteClick = { showDeleteDialog = product }
                                    )
                                }
                            }
                        }
                    }
                    is InventoryUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                if (deleteState is DeleteProductState.Loading) {
                     CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun AdminProductCard(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Imagen de ${product.name}",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 16.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text("Precio: $${product.price}", style = MaterialTheme.typography.bodyMedium)
                    Text("Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDeleteClick, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(productName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar el producto '$productName'? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(onClick = { 
                onConfirm()
                onDismiss()
             }) {
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
