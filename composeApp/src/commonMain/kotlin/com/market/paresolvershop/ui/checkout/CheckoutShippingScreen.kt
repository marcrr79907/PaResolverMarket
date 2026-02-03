package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.ui.cart.CartViewModel
import com.market.paresolvershop.ui.profile.AddressManagementScreen
import com.market.paresolvershop.ui.profile.AddressUiState
import com.market.paresolvershop.ui.profile.AddressViewModel
import com.market.paresolvershop.ui.theme.Inter
import com.market.paresolvershop.ui.theme.OnSurfaceVariant
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.SoftGray
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import com.market.paresolvershop.ui.theme.SurfaceVariant
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.Plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CheckoutShippingScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val addressViewModel = koinViewModel<AddressViewModel>()
        val cartViewModel = koinViewModel<CartViewModel>()
        
        val uiState by addressViewModel.uiState.collectAsState()
        val cartState by cartViewModel.uiState.collectAsState()
        
        var selectedAddress by remember { mutableStateOf<UserAddress?>(null) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Shipping address", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
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
                // Stepper Visual (Opcional según diseño)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Paso 2 de 3: Selección de dirección", style = MaterialTheme.typography.labelSmall, color = Primary)
                }

                when (val state = uiState) {
                    is AddressUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    is AddressUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is AddressUiState.Success -> {
                        if (state.addresses.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                NoAddressesCheckoutView { navigator.push(AddressManagementScreen()) }
                            }
                        } else {
                            Text(
                                "Selecciona una dirección de entrega:",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                                        Text("Gestionar direcciones")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { 
                        selectedAddress?.let { address ->
                            navigator.push(
                                CheckoutPaymentScreen(
                                selectedAddress = address,
                                cartItems = cartState.items
                                )
                            )
                        }
                    },
                    enabled = selectedAddress != null && cartState.items.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
                ) {
                    Text("Continuar a detalles de pago", color = Color.White, fontWeight = FontWeight.Bold)
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
            Column(modifier = Modifier.padding(start = 8.dp)) {
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
