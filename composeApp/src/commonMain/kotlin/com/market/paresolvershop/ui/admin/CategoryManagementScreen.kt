package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Trash
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CategoryManagementScreen : Screen {
    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CategoryManagementViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val actionState by viewModel.actionState.collectAsState()

        var showAddDialog by remember { mutableStateOf(false) }
        var newCategoryName by remember { mutableStateOf("") }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(actionState) {
            when (val state = actionState) {
                is DataResult.Success -> {
                    snackbarHostState.showSnackbar("Operación exitosa")
                    viewModel.resetActionState()
                }
                is DataResult.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetActionState()
                }
                else -> {}
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nueva Categoría") },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.createCategory(newCategoryName)
                        newCategoryName = ""
                        showAddDialog = false
                    }) { Text("Crear") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestionar Categorías") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Volver")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(FontAwesomeIcons.Solid.Plus, contentDescription = "Añadir")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val state = uiState) {
                    is CategoryUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is CategoryUiState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                            items(state.categories) { category ->
                                CategoryItem(category, onDelete = { viewModel.deleteCategory(category.id) })
                            }
                        }
                    }
                    is CategoryUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onDelete) {
                Icon(FontAwesomeIcons.Solid.Trash, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
