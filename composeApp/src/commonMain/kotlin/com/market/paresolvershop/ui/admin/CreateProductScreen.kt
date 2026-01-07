package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.rememberAsyncImagePainter
import com.market.paresolver.ui.components.ImagePicker
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
                is CreateProductScreenState.Error -> snackbarHostState.showSnackbar(state.message)
                is CreateProductScreenState.Success -> {
                    snackbarHostState.showSnackbar("¡Producto creado con éxito!")
                    navigator.pop()
                }
                else -> {}
            }
        }

        // Usamos una Columna como raíz en lugar de un Scaffold anidado
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
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado consistente
                ) {
                    formState.imageBytes?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Vista previa del producto",
                            modifier = Modifier.size(150.dp),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) { Text("Sin imagen") }

                    Button(onClick = { showImagePicker = true }) {
                        Text("Seleccionar Imagen")
                    }

                    OutlinedTextField(value = formState.name, onValueChange = viewModel::onNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.description, onValueChange = viewModel::onDescriptionChange, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.price, onValueChange = viewModel::onPriceChange, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.stock, onValueChange = viewModel::onStockChange, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = formState.category, onValueChange = viewModel::onCategoryChange, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())

                    Button(onClick = viewModel::createProduct, enabled = screenState != CreateProductScreenState.Loading) {
                        Text("Guardar Producto")
                    }
                }

                if (screenState == CreateProductScreenState.Loading) {
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
