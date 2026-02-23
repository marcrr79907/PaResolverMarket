package com.market.paresolvershop.ui.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.ProductVariant
import com.market.paresolvershop.ui.authentication.LoginScreen
import com.market.paresolvershop.ui.authentication.RegisterScreen
import com.market.paresolvershop.ui.cart.CartEvent
import com.market.paresolvershop.ui.cart.CartViewModel
import com.market.paresolvershop.ui.components.LoginPromptDialog
import com.market.paresolvershop.ui.components.formatPrice
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Heart
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
                onDismiss = { showLoginDialog = false },
                onLoginClick = {
                    showLoginDialog = false
                    navigator.push(LoginScreen)
                },
                onRegisterClick = {
                    showLoginDialog = false
                    navigator.push(RegisterScreen)
                }
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
                    is ProductDetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is ProductDetailUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is ProductDetailUiState.Success -> {
                        ProductDetailContent(
                            product = state.product,
                            onAddToCart = { finalProduct ->
                                if (cartViewModel.isUserLoggedIn()) {
                                    cartViewModel.addToCart(finalProduct)
                                } else {
                                    showLoginDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailContent(product: Product, onAddToCart: (Product) -> Unit) {
    val allImages = remember(product) { (listOfNotNull(product.imageUrl) + product.images).distinct() }
    val pagerState = rememberPagerState(pageCount = { allImages.size })
    val scrollState = rememberScrollState()
    
    // Gestión de variantes
    var selectedVariant by remember { mutableStateOf<ProductVariant?>(product.variants.firstOrNull()) }
    
    // Precio y stock dinámicos
    val currentPrice = selectedVariant?.price ?: product.price
    val currentStock = selectedVariant?.stock ?: product.stock

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
    ) {
        // 1. Carrusel de Imágenes Profesional
        Box(
            modifier = Modifier.fillMaxWidth().height(380.dp).padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(32.dp)).background(SurfaceVariant)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = allImages[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Indicador de páginas (Dots)
            if (allImages.size > 1) {
                Row(
                    Modifier.height(50.dp).fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(allImages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Primary else Color.LightGray
                        Box(
                            modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // 2. Nombre y Precio Dinámico
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text(text = product.categoryName ?: "Sin categoría", color = OnSurfaceVariant, fontSize = 14.sp)
                }
                Text(
                    text = "$${currentPrice.formatPrice()}",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Primary
                )
            }

            // 3. Rating y Stock
            Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(FontAwesomeIcons.Solid.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Text(" 4.8", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(16.dp))
                val stockColor = if (currentStock > 0) Color(0xFF4CAF50) else Error
                Surface(color = stockColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (currentStock > 0) "En Stock ($currentStock)" else "Agotado",
                        color = stockColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // 4. Selector de Variantes (Solo si existen)
            if (product.variants.isNotEmpty()) {
                Text("Selecciona una opción", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                LazyRow(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(product.variants) { variant ->
                        val isSelected = selectedVariant?.id == variant.id
                        Surface(
                            modifier = Modifier.clickable { selectedVariant = variant },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) Primary else SoftGray)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(variant.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Primary else OnSurface)
                                if (variant.price != null) {
                                    Text("$${variant.price.formatPrice()}", fontSize = 11.sp, color = if (isSelected) Primary else OnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // 5. Descripción
            Text("Descripción", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(Modifier.height(120.dp))
        }
    }

    // 6. Botón de Acción Fijo
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomCenter) {
        Button(
            onClick = { 
                // Creamos un objeto producto temporal con los datos de la variante elegida
                val finalProduct = product.copy(
                    price = currentPrice,
                    stock = currentStock,
                    name = if (selectedVariant != null) "${product.name} (${selectedVariant!!.name})" else product.name
                )
                onAddToCart(finalProduct)
            },
            enabled = currentStock > 0,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
        ) {
            Text("Añadir al Carrito", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = SpaceGrotesk)
        }
    }
}
