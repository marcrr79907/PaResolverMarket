package com.market.paresolvershop.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.market.paresolvershop.domain.model.Product
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

data class ProductDetailScreen(val productId: String) : Screen {

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        // Inyectamos el ViewModel, pasándole el productId como parámetro
        val viewModel = koinViewModel<ProductDetailViewModel>(
            parameters = { parametersOf(productId) }
        )
        val uiState by viewModel.uiState.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is ProductDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductDetailUiState.Success -> {
                    ProductDetailContent(product = state.product)
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

@Composable
fun ProductDetailContent(product: Product) {
    // El contenido que ya tenías, pero ahora recibe el objeto Product
    Column(
        // ... tu diseño ...
    ) {
        Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
        Text(text = "$ ${product.price}")
        Text(text = product.description)
        // ... etc
        Button(onClick = { /* Añadir al carrito */ }) {
            Text("Añadir al Carrito")
        }
    }
}
