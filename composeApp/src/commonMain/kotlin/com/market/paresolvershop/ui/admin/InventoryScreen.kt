package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.market.paresolvershop.domain.model.Category
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
        val searchQuery by viewModel.searchQuery.collectAsState()
        val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
        val sortType by viewModel.sortType.collectAsState()
        val isAscending by viewModel.isAscending.collectAsState()

        var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
        var showSortMenu by remember { mutableStateOf(false) }
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
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = if (isAscending) FontAwesomeIcons.Solid.SortAmountUp else FontAwesomeIcons.Solid.SortAmountDown,
                            contentDescription = "Ordenar",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        SortMenuItem(
                            label = "Nombre",
                            isSelected = sortType == InventorySortType.NAME,
                            isAscending = isAscending,
                            onClick = { viewModel.toggleSort(InventorySortType.NAME); showSortMenu = false }
                        )
                        SortMenuItem(
                            label = "Precio",
                            isSelected = sortType == InventorySortType.PRICE,
                            isAscending = isAscending,
                            onClick = { viewModel.toggleSort(InventorySortType.PRICE); showSortMenu = false }
                        )
                        SortMenuItem(
                            label = "Stock",
                            isSelected = sortType == InventorySortType.STOCK,
                            isAscending = isAscending,
                            onClick = { viewModel.toggleSort(InventorySortType.STOCK); showSortMenu = false }
                        )
                    }
                }
                IconButton(onClick = { navigator.push(CreateProductScreen) }) {
                    Icon(FontAwesomeIcons.Solid.Plus, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            },
            extraHeader = {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Buscar producto...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(FontAwesomeIcons.Solid.Times, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SurfaceVariant,
                            focusedBorderColor = Primary
                        )
                    )
                    
                    if (uiState is InventoryUiState.Success) {
                        val state = uiState as InventoryUiState.Success
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryId == null,
                                    onClick = { viewModel.selectCategory(null) },
                                    label = { Text("Todos", fontSize = 12.sp) }
                                )
                            }
                            items(state.categories) { category ->
                                FilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { viewModel.selectCategory(category.id) },
                                    label = { Text(category.name, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
                when (val state = uiState) {
                    is InventoryUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is InventoryUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is InventoryUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Mini Dashboard de Inventario - Más sutil
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InventoryStatBadge("Total: ${state.totalProducts}", Primary)
                                if (state.lowStockCount > 0) {
                                    InventoryStatBadge("Bajo Stock: ${state.lowStockCount}", Error)
                                }
                            }

                            if (state.products.isEmpty()) {
                                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(FontAwesomeIcons.Solid.BoxOpen, null, Modifier.size(64.dp), tint = SoftGray)
                                    Text("No se encontraron productos", color = OnSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.products) { product ->
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
}

@Composable
fun SortMenuItem(label: String, isSelected: Boolean, isAscending: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        imageVector = if (isAscending) FontAwesomeIcons.Solid.ArrowUp else FontAwesomeIcons.Solid.ArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Primary
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
fun InventoryStatBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
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
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant),
                    contentScale = ContentScale.Crop
                )
                if (product.stock < 5) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Error, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Low", color = OnPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(product.categoryName ?: "Sin categoría", fontSize = 12.sp, color = OnSurfaceVariant)
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$${product.price.formatPrice()}", 
                        fontSize = 16.sp, 
                        color = Primary, 
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = SpaceGrotesk
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Stock: ${product.stock}", 
                        fontSize = 13.sp, 
                        color = if (product.stock < 5) Error else OnSurface, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.Center) {
                IconButton(onClick = onEdit) { 
                    Icon(FontAwesomeIcons.Solid.Edit, null, tint = Primary, modifier = Modifier.size(20.dp)) 
                }
                IconButton(onClick = onDelete) { 
                    Icon(FontAwesomeIcons.Solid.Trash, null, tint = Error, modifier = Modifier.size(20.dp)) 
                }
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
