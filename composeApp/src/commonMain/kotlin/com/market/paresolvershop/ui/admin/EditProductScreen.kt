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
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.ui.components.ImagePicker
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

data class EditProductScreen(val product: Product) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<EditProductViewModel> { parametersOf(product) }
        val formState = viewModel.formState
        val screenState by viewModel.screenState.collectAsState()

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
                is EditProductScreenState.Error -> snackbarHostState.showSnackbar(state.message)
                is EditProductScreenState.Success -> {
                    snackbarHostState.showSnackbar("¡Producto actualizado con éxito!")
                    navigator.pop()
                }
                else -> {}
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Editar Producto") },
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
                    // Show new image preview if selected, otherwise show original image
                    val imageModifier = Modifier.size(150.dp)
                    formState.imageBytes?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Vista previa del producto",
                            modifier = imageModifier,
                            contentScale = ContentScale.Crop
                        )
                    } ?: AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Imagen actual del producto",
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop
                    )

                    Button(onClick = { showImagePicker = true }) {
                        Text("Cambiar Imagen")
                    }

                    OutlinedTextField(value = formState.name, onValueChange = viewModel::onNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.description, onValueChange = viewModel::onDescriptionChange, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.price, onValueChange = viewModel::onPriceChange, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.stock, onValueChange = viewModel::onStockChange, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.category, onValueChange = viewModel::onCategoryChange, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())

                    Button(onClick = viewModel::updateProduct, enabled = screenState != EditProductScreenState.Loading) {
                        Text("Guardar Cambios")
                    }
                }

                if (screenState == EditProductScreenState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                )
            }
        }
    }
}
