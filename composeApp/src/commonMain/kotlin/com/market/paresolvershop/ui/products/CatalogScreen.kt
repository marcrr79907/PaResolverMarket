package com.market.paresolvershop.ui.products

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.market.paresolvershop.ui.authentication.LoginScreen
import com.market.paresolvershop.ui.authentication.RegisterScreen
import com.market.paresolvershop.ui.cart.CartEvent
import com.market.paresolvershop.ui.cart.CartViewModel
import com.market.paresolvershop.ui.components.CategoryChip
import com.market.paresolvershop.ui.components.LoginPromptDialog
import com.market.paresolvershop.ui.profile.ProfileUiState
import com.market.paresolvershop.ui.profile.ProfileViewModel
import com.market.paresolvershop.ui.search.SearchScreen
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import com.market.paresolvershop.ui.theme.SurfaceVariant
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.Heart
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.ShoppingCart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CatalogScreen : Screen {
    @Composable
    @OptIn(KoinExperimentalAPI::class)
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CatalogViewModel>()
        val profileViewModel = koinViewModel<ProfileViewModel>()
        val cartViewModel = koinViewModel<CartViewModel>()
        
        val uiState by viewModel.uiState.collectAsState()
        val profileState by profileViewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        
        var showLoginPrompt by remember { mutableStateOf(false) }

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

        val userName = when (val state = profileState) {
            is ProfileUiState.Authenticated -> state.user.name
            else -> "Invitado"
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (uiState) {
                is CatalogUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
                }
                is CatalogUiState.Success -> {
                    val state = uiState as CatalogUiState.Success
                    CatalogGridContent(
                        userName = userName,
                        products = state.products,
                        categories = state.categories,
                        selectedCategoryId = state.selectedCategoryId,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onProductClick = { productId ->
                            navigator.push(ProductDetailScreen(productId))
                        },
                        onAddToCart = { product ->
                            if (cartViewModel.isUserLoggedIn()) {
                                cartViewModel.addToCart(product)
                            } else {
                                showLoginPrompt = true
                            }
                        },
                        onSearchClick = {
                            navigator.push(SearchScreen)
                        }
                    )
                }
                is CatalogUiState.Error -> {
                    Text(
                        text = (uiState as CatalogUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun CatalogGridContent(
    userName: String,
    products: List<Product>,
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelect: (String?) -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onSearchClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { HeaderSection(userName, onSearchClick) }
        item { PromoBanner() }
        item { 
            CategoriesSection(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelect = onCategorySelect
            ) 
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (selectedCategoryId == null) "Recomendados" else "Resultados",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        if (products.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(products.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pair.forEach { product ->
                        ProductGridItem(
                            product = product,
                            modifier = Modifier.weight(1f),
                            onClick = { onProductClick(product.id) },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelect: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            "Categorías", 
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = SpaceGrotesk, 
            fontWeight = FontWeight.Bold, 
            fontSize = 18.sp
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CategoryChip(
                    name = "Todo",
                    isSelected = selectedCategoryId == null,
                    onClick = { onCategorySelect(null) }
                )
            }
            
            items(categories) { category ->
                CategoryChip(
                    name = category.name,
                    isSelected = selectedCategoryId == category.id,
                    onClick = { onCategorySelect(category.id) }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(name: String, onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Bienvenido de nuevo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                name,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeaderIcon(FontAwesomeIcons.Solid.Search, onClick = onSearchClick)
            HeaderIcon(FontAwesomeIcons.Solid.Bell, onClick = { /* Notificaciones */ })
        }
    }
}

@Composable
fun HeaderIcon(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.size(45.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun PromoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Primary)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Nueva Colección",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "Descuento 50% para\nrecomendados",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Comprar ahora", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProductGridItem(
    product: Product, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SurfaceVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(SurfaceVariant)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Heart,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp).size(14.dp),
                        tint = Color.Gray
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$${product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Surface(
                        onClick = onAddToCart,
                        shape = CircleShape,
                        color = Primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShoppingCart,
                                contentDescription = "Añadir al carrito",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

