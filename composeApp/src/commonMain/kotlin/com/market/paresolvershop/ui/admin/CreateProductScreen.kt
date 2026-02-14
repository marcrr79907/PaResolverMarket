package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.rememberAsyncImagePainter
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.ui.components.ImagePicker
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Trash
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CreateProductScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CreateProductViewModel>()
        val categoryViewModel = koinViewModel<CategoryManagementViewModel>()
        
        val formState = viewModel.formState
        val screenState by viewModel.screenState.collectAsState()
        val categoriesState by categoryViewModel.uiState.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        var showMainImagePicker by remember { mutableStateOf(false) }
        var showAdditionalImagePicker by remember { mutableStateOf(false) }

        // Picker para imagen principal
        ImagePicker(show = showMainImagePicker) {
            showMainImagePicker = false
            if (it != null) {
                viewModel.onMainImageSelected(it)
            }
        }

        // Picker para imágenes adicionales
        ImagePicker(show = showAdditionalImagePicker, multiple = true) {
            showAdditionalImagePicker = false
            if (it != null) {
                viewModel.addAdditionalImages(it)
            }
        }

        LaunchedEffect(screenState) {
            when (val state = screenState) {
                is CreateProductScreenState.Error -> snackbarHostState.showSnackbar(state.message)
                is CreateProductScreenState.Success -> {
                    snackbarHostState.showSnackbar("¡Producto creado con éxito!")
                    navigator.pop()
                }
                else -> {}
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Crear Nuevo Producto", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // SECCIÓN: IMÁGENES
                    Text("Imágenes del Producto", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Imagen Principal
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceVariant)
                                    .clickable { showMainImagePicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (formState.mainImageBytes != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(formState.mainImageBytes),
                                        contentDescription = "Principal",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(FontAwesomeIcons.Solid.Plus, null, tint = OnSurfaceVariant)
                                }
                            }
                            Text("Principal", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }

                        // Lista de Imágenes Adicionales
                        Column(modifier = Modifier.weight(2f)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(formState.additionalImages) { index, bytes ->
                                    Box(modifier = Modifier.size(100.dp)) {
                                        Image(
                                            painter = rememberAsyncImagePainter(bytes),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeAdditionalImage(index) },
                                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Error, CircleShape)
                                        ) {
                                            Icon(FontAwesomeIcons.Solid.Trash, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceVariant.copy(alpha = 0.5f))
                                            .clickable { showAdditionalImagePicker = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(FontAwesomeIcons.Solid.Plus, null, tint = Primary)
                                    }
                                }
                            }
                            Text("Adicionales", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    HorizontalDivider(color = SoftGray)

                    // SECCIÓN: DATOS BÁSICOS
                    Text("Información General", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    
                    OutlinedTextField(value = formState.name, onValueChange = viewModel::onNameChange, label = { Text("Nombre del Producto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = formState.description, onValueChange = viewModel::onDescriptionChange, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 3)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formState.price, 
                            onValueChange = viewModel::onPriceChange, 
                            label = { Text("Precio Base") }, 
                            modifier = Modifier.weight(1f), 
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = formState.stock, 
                            onValueChange = viewModel::onStockChange, 
                            label = { Text("Stock Total") }, 
                            modifier = Modifier.weight(1f), 
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    
                    CategorySelector(
                        selectedCategoryId = formState.categoryId,
                        categoriesState = categoriesState,
                        onCategorySelected = { viewModel.onCategoryChange(it.id) }
                    )

                    HorizontalDivider(color = SoftGray)

                    // SECCIÓN: VARIANTES
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Variantes / SKUs", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TextButton(onClick = { viewModel.addVariant() }) {
                            Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Añadir")
                        }
                    }

                    formState.variants.forEachIndexed { index, variant ->
                        VariantItemForm(
                            state = variant,
                            onUpdate = { viewModel.updateVariant(index, it) },
                            onRemove = { viewModel.removeVariant(index) }
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::createProduct,
                        enabled = screenState != CreateProductScreenState.Loading && formState.name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (screenState == CreateProductScreenState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Guardar Producto", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun VariantItemForm(
    state: ProductVariantFormState,
    onUpdate: (ProductVariantFormState) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SoftGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Variante", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(FontAwesomeIcons.Solid.Trash, null, tint = Error, modifier = Modifier.size(16.dp))
                }
            }
            
            OutlinedTextField(
                value = state.name, 
                onValueChange = { onUpdate(state.copy(name = it)) }, 
                label = { Text("Nombre (ej: Lomo, Pierna)") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.price, 
                    onValueChange = { onUpdate(state.copy(price = it)) }, 
                    label = { Text("Precio Extra") }, 
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = state.stock, 
                    onValueChange = { onUpdate(state.copy(stock = it)) }, 
                    label = { Text("Stock") }, 
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            OutlinedTextField(
                value = state.sku, 
                onValueChange = { onUpdate(state.copy(sku = it)) }, 
                label = { Text("SKU (Opcional)") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategoryId: String,
    categoriesState: CategoryUiState,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = (categoriesState as? CategoryUiState.Success)?.categories ?: emptyList()
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Selecciona una categoría",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
