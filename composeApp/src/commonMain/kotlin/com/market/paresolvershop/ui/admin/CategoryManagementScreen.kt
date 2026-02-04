package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.ui.theme.Background
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Edit
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

        var showDialog by remember { mutableStateOf<CategoryAction?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(actionState) {
            when (val state = actionState) {
                is DataResult.Success -> {
                    snackbarHostState.showSnackbar("Operación realizada con éxito")
                    viewModel.resetActionState()
                }
                is DataResult.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetActionState()
                }
                else -> {}
            }
        }

        // Manejo de Diálogos (Crear/Editar/Eliminar)
        showDialog?.let { action ->
            when (action) {
                is CategoryAction.Create, is CategoryAction.Edit -> {
                    val isEdit = action is CategoryAction.Edit
                    var name by remember { mutableStateOf(if (action is CategoryAction.Edit) action.category.name else "") }
                    
                    AlertDialog(
                        onDismissRequest = { showDialog = null },
                        title = { Text(if (isEdit) "Editar Categoría" else "Nueva Categoría", fontFamily = SpaceGrotesk) },
                        text = {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre de la categoría") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (isEdit) {
                                        viewModel.updateCategory((action as CategoryAction.Edit).category.copy(name = name))
                                    } else {
                                        viewModel.createCategory(name)
                                    }
                                    showDialog = null
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Guardar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = null }) { Text("Cancelar") }
                        }
                    )
                }
                is CategoryAction.Delete -> {
                    AlertDialog(
                        onDismissRequest = { showDialog = null },
                        title = { Text("¿Eliminar categoría?", fontFamily = SpaceGrotesk) },
                        text = { Text("¿Estás seguro de que quieres eliminar '${action.category.name}'? Esto podría afectar a los productos asociados.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteCategory(action.category.id)
                                    showDialog = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Eliminar", color = Color.White) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = null }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gestionar Categorías", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Volver")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = CategoryAction.Create },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Plus, 
                        contentDescription = "Añadir",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
                when (val state = uiState) {
                    is CategoryUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
                    is CategoryUiState.Success -> {
                        if (state.categories.isEmpty()) {
                            EmptyState(Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.categories) { category ->
                                    CategoryCard(
                                        category = category,
                                        onEdit = { showDialog = CategoryAction.Edit(category) },
                                        onDelete = { showDialog = CategoryAction.Delete(category) }
                                    )
                                }
                            }
                        }
                    }
                    is CategoryUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${category.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(FontAwesomeIcons.Solid.Edit, contentDescription = "Editar", tint = Primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(FontAwesomeIcons.Solid.Trash, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No hay categorías", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text("Pulsa + para añadir la primera", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

sealed class CategoryAction {
    data object Create : CategoryAction()
    data class Edit(val category: Category) : CategoryAction()
    data class Delete(val category: Category) : CategoryAction()
}
