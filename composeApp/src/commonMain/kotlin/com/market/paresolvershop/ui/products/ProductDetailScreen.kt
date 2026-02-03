package com.market.paresolvershop.ui.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.cart.CartEvent
import com.market.paresolvershop.ui.cart.CartViewModel
import com.market.paresolvershop.ui.navigation.bottombar.ProfileTab
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Heart
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.Star
import kotlinx.coroutines.flow.collectLatest
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Volver", modifier = Modifier.size(20.dp))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.Heart, contentDescription = "Favorito", modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
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
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 1. Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(SurfaceVariant)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(24.dp))

        // 2. Thumbnails (Mocked for UI design)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(List(5) { it }) { index ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariant)
                        .border(
                            width = if (index == 2) 2.dp else 0.dp,
                            color = if (index == 2) Primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            // 3. Name and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${product.price}",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = OnSurface
                )
            }

            // 4. Rating
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(FontAwesomeIcons.Solid.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Text(
                    " 4.6",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 5. Select Model
            Text(
                "Select Model",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            val variants = listOf("Core i3", "Core i5", "Core i7", "Core i9")
            var selectedVariant by remember { mutableStateOf("Core i5") }
            
            LazyRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(variants) { variant ->
                    FilterChip(
                        selected = selectedVariant == variant,
                        onClick = { selectedVariant = variant },
                        label = { Text(variant) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.1f),
                            selectedLabelColor = Primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (selectedVariant == variant) BorderStroke(1.dp, Primary) else null
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 6. Description
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(100.dp)) // Space for bottom button
        }
    }

    // 7. Bottom Action Bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onAddToCart() },
                shape = RoundedCornerShape(16.dp),
                color = SurfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, SoftGray)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ShoppingCart,
                        contentDescription = "Add to Cart",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
