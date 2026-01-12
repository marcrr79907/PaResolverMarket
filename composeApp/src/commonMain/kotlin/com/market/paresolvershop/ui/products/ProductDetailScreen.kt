package com.market.paresolvershop.ui.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.navigation.bottombar.ProfileTab
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

data class ProductDetailScreen(val productId: String) : Screen {

    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val detailViewModel = koinViewModel<ProductDetailViewModel> { parametersOf(productId) }
        val cartViewModel = koinViewModel<CartViewModel>()
        val uiState by detailViewModel.uiState.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        var showLoginDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            cartViewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is CartEvent.Success -> snackbarHostState.showSnackbar(event.message)
                    is CartEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        if (showLoginDialog) {
            LoginPromptDialog(
                onConfirm = {
                    showLoginDialog = false
                    tabNavigator.current = ProfileTab
                },
                onDismiss = { showLoginDialog = false }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle del Producto") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Volver")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (val state = uiState) {
                    is ProductDetailUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ProductDetailUiState.Success -> {
                        ProductDetailContent(
                            product = state.product,
                            onAddToCart = {
                                if (cartViewModel.isUserLoggedIn()) {
                                    cartViewModel.addToCart(state.product)
                                } else {
                                    showLoginDialog = true
                                }
                            }
                        )
                    }
                    is ProductDetailUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailContent(product: Product, onAddToCart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier.size(250.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$ ${product.price}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = product.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
            Text("Añadir al Carrito")
        }
    }
}

@Composable
fun LoginPromptDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Iniciar Sesión Requerido") },
        text = { Text("Para añadir productos a tu carrito, necesitas iniciar sesión.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Iniciar Sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
