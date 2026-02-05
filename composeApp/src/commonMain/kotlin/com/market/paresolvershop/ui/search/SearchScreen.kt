package com.market.paresolvershop.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.ui.authentication.LoginScreen
import com.market.paresolvershop.ui.authentication.RegisterScreen
import com.market.paresolvershop.ui.cart.CartEvent
import com.market.paresolvershop.ui.cart.CartViewModel
import com.market.paresolvershop.ui.components.CategoryChip
import com.market.paresolvershop.ui.components.LoginPromptDialog
import com.market.paresolvershop.ui.products.CatalogUiState
import com.market.paresolvershop.ui.products.CatalogViewModel
import com.market.paresolvershop.ui.products.ProductGridItem
import com.market.paresolvershop.ui.products.ProductDetailScreen
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object SearchScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val catalogViewModel = koinViewModel<CatalogViewModel>()
        val cartViewModel = koinViewModel<CartViewModel>()
        
        val uiState by catalogViewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        
        var showLoginPrompt by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }

        // Obtenemos categorías y selección del estado del catálogo
        val successState = uiState as? CatalogUiState.Success
        val categories = successState?.categories ?: emptyList()
        val selectedCategoryId = successState?.selectedCategoryId

        val filteredProducts = remember(query, uiState) {
            val products = successState?.products ?: emptyList()
            if (query.isEmpty()) products
            else products.filter { it.name.contains(query, ignoreCase = true) }
        }

        LaunchedEffect(Unit) {
            cartViewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is CartEvent.Success -> snackbarHostState.showSnackbar(event.message)
                    is CartEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        if (showLoginPrompt) {
            LoginPromptDialog(
                onDismiss = { showLoginPrompt = false },
                onLoginClick = {
                    showLoginPrompt = false
                    navigator.push(LoginScreen)
                },
                onRegisterClick = {
                    showLoginPrompt = false
                    navigator.push(RegisterScreen)
                }
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Background,
                    shadowElevation = 2.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
                                            Icon(FontAwesomeIcons.Solid.TimesCircle, null, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                singleLine = true
                            )
                        }

                        // Fila de categorías dinámica
                        if (categories.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    CategoryChip(
                                        name = "Todo",
                                        isSelected = selectedCategoryId == null,
                                        onClick = { catalogViewModel.selectCategory(null) }
                                    )
                                }
                                items(categories) { category ->
                                    CategoryChip(
                                        name = category.name,
                                        isSelected = selectedCategoryId == category.id,
                                        onClick = { catalogViewModel.selectCategory(category.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)) {
                
                if (uiState is CatalogUiState.Loading && query.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
                } else if (filteredProducts.isEmpty()) {
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
                                onClick = { navigator.push(ProductDetailScreen(product.id)) },
                                onAddToCart = {
                                    if (cartViewModel.isUserLoggedIn()) {
                                        cartViewModel.addToCart(product)
                                    } else {
                                        showLoginPrompt = true
                                    }
                                }
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
            text = if (query.isEmpty()) "Empieza a buscar..." else "Sin resultados para \"$query\"",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = OnSurface
        )
        Text(
            text = "Prueba con otra palabra o selecciona una categoría diferente.",
            textAlign = TextAlign.Center,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
