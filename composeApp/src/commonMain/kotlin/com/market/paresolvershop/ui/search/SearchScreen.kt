package com.market.paresolvershop.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.products.CatalogUiState
import com.market.paresolvershop.ui.products.CatalogViewModel
import com.market.paresolvershop.ui.products.ProductGridItem
import com.market.paresolvershop.ui.products.ProductDetailScreen
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object SearchScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CatalogViewModel>() // Reutilizamos el VM para obtener productos
        val uiState by viewModel.uiState.collectAsState()
        
        var query by remember { mutableStateOf("") }
        val filteredProducts = remember(query, uiState) {
            val products = (uiState as? CatalogUiState.Success)?.products ?: emptyList()
            if (query.isEmpty()) products
            else products.filter { it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
        }

        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Background,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier.background(SurfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Atras", modifier = Modifier.size(18.dp))
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search products...", color = Color.Gray.copy(alpha = 0.6f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                                focusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Primary
                            ),
                            leadingIcon = {
                                Icon(FontAwesomeIcons.Solid.Search, null, modifier = Modifier.size(18.dp), tint = Primary)
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            FontAwesomeIcons.Solid.TimesCircle,
                                            null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)) {
                if (filteredProducts.isEmpty()) {
                    EmptySearchView(query)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductGridItem(
                                product = product,
                                onClick = { navigator.push(ProductDetailScreen(product.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchView(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = FontAwesomeIcons.Solid.SearchMinus,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = SoftGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (query.isEmpty()) "Start searching..." else "No hay resultados para \"$query\"",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = OnSurface
        )
        Text(
            text = "Intente una combinacion diferente de palabras o vea Categorias.",
            textAlign = TextAlign.Center,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
