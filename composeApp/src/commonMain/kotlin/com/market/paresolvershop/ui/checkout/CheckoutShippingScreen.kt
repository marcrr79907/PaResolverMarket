package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.ui.profile.AddressManagementScreen
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.Plus
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CheckoutShippingScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CheckoutViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        
        var selectedAddress by remember { mutableStateOf<UserAddress?>(null) }

        // Escuchamos eventos de éxito o error (ej. al refrescar)
        LaunchedEffect(Unit) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is CheckoutEvent.Success -> snackbarHostState.showSnackbar(event.message)
                    is CheckoutEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Dirección de envío", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() },
                            modifier = Modifier.padding(start = 12.dp).background(SurfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                // Indicador de progreso del Checkout
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Paso 2 de 3: Selección de dirección", style = MaterialTheme.typography.labelSmall, color = Primary)
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (val state = uiState) {
                        is CheckoutUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
                        }
                        is CheckoutUiState.Error -> {
                            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = Error)
                                Button(onClick = { viewModel.loadCheckoutData(true) }, modifier = Modifier.padding(top = 8.dp)) {
                                    Text("Reintentar")
                                }
                            }
                        }
                        is CheckoutUiState.Success -> {
                            if (state.addresses.isEmpty()) {
                                NoAddressesCheckoutView { navigator.push(AddressManagementScreen()) }
                            } else {
                                // Si hay una dirección por defecto y nada seleccionado, la marcamos
                                LaunchedEffect(state.addresses) {
                                    if (selectedAddress == null) {
                                        selectedAddress = state.addresses.find { it.isDefault } ?: state.addresses.firstOrNull()
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Text("Selecciona dónde recibirás tu pedido:", style = MaterialTheme.typography.titleSmall)
                                    }
                                    items(state.addresses) { address ->
                                        AddressSelectionCard(
                                            address = address,
                                            isSelected = selectedAddress?.id == address.id,
                                            onSelect = { selectedAddress = address }
                                        )
                                    }
                                    item {
                                        TextButton(
                                            onClick = { navigator.push(AddressManagementScreen()) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(FontAwesomeIcons.Solid.Plus, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Añadir o editar direcciones")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botón de acción basado en el estado Success
                val currentState = uiState as? CheckoutUiState.Success
                Button(
                    onClick = { 
                        selectedAddress?.let { address ->
                            currentState?.let { success ->
                                navigator.push(
                                    CheckoutPaymentScreen(
                                        selectedAddress = address,
                                        cartItems = success.cartItems
                                    )
                                )
                            }
                        }
                    },
                    enabled = selectedAddress != null && (currentState?.cartItems?.isNotEmpty() == true),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 16.dp, top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
                ) {
                    Text("Continuar al Pago", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddressSelectionCard(address: UserAddress, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Primary.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) Primary else SoftGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = Primary)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "${address.firstName} ${address.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Text(
                    text = address.addressLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = address.phone,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NoAddressesCheckoutView(onAddClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
    ) {
        Icon(FontAwesomeIcons.Solid.MapMarkerAlt, null, modifier = Modifier.size(48.dp), tint = SoftGray)
        Spacer(Modifier.height(16.dp))
        Text("No tienes direcciones guardadas", color = OnSurfaceVariant)
        Button(
            onClick = onAddClick,
            modifier = Modifier.padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Añadir mi primera dirección")
        }
    }
}
