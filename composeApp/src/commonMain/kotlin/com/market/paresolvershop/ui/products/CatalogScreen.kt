package com.market.paresolvershop.ui.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.navigation.SearchScreen
import com.market.paresolvershop.ui.profilemagnament.ProfileUiState
import com.market.paresolvershop.ui.profilemagnament.ProfileViewModel
import com.market.paresolvershop.ui.theme.Inter
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import com.market.paresolvershop.ui.theme.SurfaceVariant
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.Camera
import compose.icons.fontawesomeicons.solid.Gamepad
import compose.icons.fontawesomeicons.solid.Headphones
import compose.icons.fontawesomeicons.solid.Heart
import compose.icons.fontawesomeicons.solid.Laptop
import compose.icons.fontawesomeicons.solid.MobileAlt
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Star
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CatalogScreen : Screen {
    @Composable
    @OptIn(KoinExperimentalAPI::class)
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CatalogViewModel>()
        val profileViewModel = koinViewModel<ProfileViewModel>()
        
        val uiState by viewModel.uiState.collectAsState()
        val profileState by profileViewModel.uiState.collectAsState()

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
                    CatalogGridContent(
                        userName = userName,
                        products = (uiState as CatalogUiState.Success).products,
                        onProductClick = { productId ->
                            navigator.push(ProductDetailScreen(productId))
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
        }
    }
}

@Composable
fun CatalogGridContent(
    userName: String,
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { HeaderSection(userName, onSearchClick) }
        item { PromoBanner() }
        item { CategoriesSection() }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recomendados",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "Ver todo",
                    color = Primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { }
                )
            }
        }

        items(products.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pair.forEach { product ->
                    ProductGridItem(
                        product = product,
                        modifier = Modifier.weight(1f),
                        onClick = { onProductClick(product.id) }
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
            Icon(
                imageVector = FontAwesomeIcons.Solid.Laptop,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun CategoriesSection() {
    val categories = listOf(
        "PC" to FontAwesomeIcons.Solid.Laptop,
        "Móvil" to FontAwesomeIcons.Solid.MobileAlt,
        "Audio" to FontAwesomeIcons.Solid.Headphones,
        "Cámara" to FontAwesomeIcons.Solid.Camera,
        "Juegos" to FontAwesomeIcons.Solid.Gamepad
    )

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Categorías", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Ver todo", color = Primary, style = MaterialTheme.typography.labelLarge)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (category.first == "PC") Primary else MaterialTheme.colorScheme.surface,
                        border = if (category.first == "PC") null else BorderStroke(1.dp, SurfaceVariant),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = category.second,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (category.first == "PC") Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        category.first,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridItem(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(SurfaceVariant)) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.7f)
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Heart,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp).size(14.dp),
                        tint = Primary
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = FontAwesomeIcons.Solid.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Text(" 4.5", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        "$${product.price}",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
