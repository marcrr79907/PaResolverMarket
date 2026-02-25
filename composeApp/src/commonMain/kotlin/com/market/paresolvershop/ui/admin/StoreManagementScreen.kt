package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.Save
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object StoreManagementScreen : Screen {

    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<StoreManagementViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // Estados locales para los campos
        var storeName by remember { mutableStateOf("") }
        var shippingFee by remember { mutableStateOf("") }
        var taxFee by remember { mutableStateOf("") }
        var currency by remember { mutableStateOf("$") }

        // Sincronizar con el estado del servidor al cargar
        LaunchedEffect(uiState) {
            if (uiState is StoreManagementUiState.Success) {
                val config = (uiState as StoreManagementUiState.Success).config
                storeName = config.storeName
                shippingFee = config.shippingFee.toString()
                taxFee = config.taxFee.toString()
                currency = config.currencySymbol
            }
        }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is StoreManagementEvent.Success -> snackbarHostState.showSnackbar(event.message)
                    is StoreManagementEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Ajustes de Tienda", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier.padding(start = 12.dp).background(SurfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
                when (val state = uiState) {
                    is StoreManagementUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                    is StoreManagementUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is StoreManagementUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text("Configuración General", style = Typography.titleMedium, color = Primary)
                            
                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                label = { Text("Nombre de la Tienda") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = shippingFee,
                                    onValueChange = { shippingFee = it },
                                    label = { Text("Costo Envío") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = taxFee,
                                    onValueChange = { taxFee = it },
                                    label = { Text("Impuestos") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }

                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Símbolo Moneda") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(32.dp))

                            Button(
                                onClick = { viewModel.updateConfig(storeName, shippingFee, taxFee, currency) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
                            ) {
                                Icon(FontAwesomeIcons.Solid.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
