package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.rememberAsyncImagePainter
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.ui.components.ImagePicker
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
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
        var showImagePicker by remember { mutableStateOf(false) }

        ImagePicker(show = showImagePicker) {
            showImagePicker = false
            if (it != null) {
                viewModel.onImageSelected(it)
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

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Crear Nuevo Producto") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(FontAwesomeIcons.Solid.ArrowLeft, "Volver")
                    }
                }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Vista previa de imagen
                    formState.imageBytes?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Vista previa",
                            modifier = Modifier.size(150.dp),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) { Text("Sin imagen") }

                    Button(onClick = { showImagePicker = true }) {
                        Text("Seleccionar Imagen")
                    }

                    // Campos de texto
                    OutlinedTextField(value = formState.name, onValueChange = viewModel::onNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.description, onValueChange = viewModel::onDescriptionChange, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.price, onValueChange = viewModel::onPriceChange, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.stock, onValueChange = viewModel::onStockChange, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    
                    // Selector de Categoría (Dropdown)
                    CategorySelector(
                        selectedCategoryId = formState.categoryId,
                        categoriesState = categoriesState,
                        onCategorySelected = { viewModel.onCategoryChange(it.id) }
                    )

                    Button(
                        onClick = viewModel::createProduct,
                        enabled = screenState != CreateProductScreenState.Loading && formState.categoryId.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Guardar Producto")
                    }
                }

                if (screenState == CreateProductScreenState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                
                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
            }
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
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay categorías. Créalas en el panel Admin.") },
                    onClick = { expanded = false }
                )
            }
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
