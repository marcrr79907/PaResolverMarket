package com.market.paresolvershop.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

class AddressManagementScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<AddressViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        
        var isAddingNew by remember { mutableStateOf(false) }
        var addressToEdit by remember { mutableStateOf<UserAddress?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is AddressEvent.Success -> {
                        snackbarHostState.showSnackbar(event.message)
                        isAddingNew = false
                        addressToEdit = null
                    }
                    is AddressEvent.Error -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            if (isAddingNew || addressToEdit != null) "Shipping address" else "My Addresses", 
                            fontFamily = SpaceGrotesk, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { 
                                if (isAddingNew || addressToEdit != null) {
                                    isAddingNew = false
                                    addressToEdit = null
                                } else {
                                    navigator.pop()
                                }
                            },
                            modifier = Modifier.padding(start = 12.dp).background(SurfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                val hasAddresses = (uiState as? AddressUiState.Success)?.addresses?.isNotEmpty() == true
                if (!isAddingNew && addressToEdit == null && hasAddresses) {
                    FloatingActionButton(
                        onClick = { isAddingNew = true },
                        containerColor = Primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Plus, 
                            contentDescription = "Add Address",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isAddingNew || addressToEdit != null) {
                    AddressForm(
                        address = addressToEdit,
                        onSave = { viewModel.saveAddress(it) },
                        onCancel = { isAddingNew = false; addressToEdit = null }
                    )
                } else {
                    when (val state = uiState) {
                        is AddressUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                        is AddressUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = Error)
                        }
                        is AddressUiState.Success -> {
                            if (state.addresses.isEmpty()) {
                                EmptyAddressesView { isAddingNew = true }
                            } else {
                                AddressList(
                                    addresses = state.addresses,
                                    onEdit = { addressToEdit = it },
                                    onDelete = { viewModel.deleteAddress(it) },
                                    onSetDefault = { viewModel.setDefault(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressList(
    addresses: List<UserAddress>,
    onEdit: (UserAddress) -> Unit,
    onDelete: (String) -> Unit,
    onSetDefault: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(addresses) { address ->
            AddressCard(
                address = address,
                onEdit = { onEdit(address) },
                onDelete = { onDelete(address.id ?: "") },
                onSetDefault = { onSetDefault(address.id ?: "") }
            )
        }
    }
}

@Composable
fun AddressCard(
    address: UserAddress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (address.isDefault) Primary else SoftGray),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${address.firstName} ${address.lastName}",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (address.isDefault) {
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Default",
                            color = Primary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(address.addressLine, color = OnSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Text(address.phone, color = OnSurfaceVariant, fontSize = 12.sp)
            
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Edit",
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onEdit() }
                )
                Text(
                    "Delete",
                    color = Error,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onDelete() }
                )
                if (!address.isDefault) {
                    Text(
                        "Set Default",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onSetDefault() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddressForm(
    address: UserAddress? = null,
    onSave: (UserAddress) -> Unit,
    onCancel: () -> Unit
) {
    var firstName by remember { mutableStateOf(address?.firstName ?: "") }
    var lastName by remember { mutableStateOf(address?.lastName ?: "") }
    var addressLine by remember { mutableStateOf(address?.addressLine ?: "") }
    var company by remember { mutableStateOf(address?.company ?: "") }
    var phone by remember { mutableStateOf(address?.phone ?: "") }
    var city by remember { mutableStateOf(address?.city ?: "") }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        AddressTextField("First name", firstName, { firstName = it }, "First name")
        AddressTextField("Last name", lastName, { lastName = it }, "Last name")
        AddressTextField("Address", addressLine, { addressLine = it }, "1226 University Drive")
        AddressTextField("Company", company, { company = it }, "(optional)")
        AddressTextField("Phone number", phone, { phone = it }, "+65 Phone (optional)", KeyboardType.Phone)
        AddressTextField("City", city, { city = it }, "City name")

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isDefault, onCheckedChange = { isDefault = it }, colors = CheckboxDefaults.colors(checkedColor = Primary))
            Text("Set as default address", fontSize = 14.sp, fontFamily = Inter)
        }

        Button(
            onClick = { 
                onSave(UserAddress(
                    id = address?.id,
                    firstName = firstName,
                    lastName = lastName,
                    addressLine = addressLine,
                    company = company,
                    phone = phone,
                    city = city,
                    isDefault = isDefault
                )) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
            enabled = firstName.isNotBlank() && lastName.isNotBlank() && addressLine.isNotBlank() && phone.isNotBlank() && city.isNotBlank()
        ) {
            Text("Save Address", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SoftGray)
        ) {
            Text("Cancel", color = OnSurface)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AddressTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                unfocusedBorderColor = SoftGray,
                focusedBorderColor = Primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

@Composable
fun EmptyAddressesView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = FontAwesomeIcons.Solid.MapMarkedAlt,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = SoftGray
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No addresses found",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            "Add a shipping address to complete your orders faster.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Plus, 
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Add your first address", fontWeight = FontWeight.Bold)
        }
    }
}
