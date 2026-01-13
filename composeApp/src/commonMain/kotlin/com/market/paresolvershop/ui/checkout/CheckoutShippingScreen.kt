package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CheckoutShippingScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<CheckoutViewModel>()
        val formState by viewModel::formState

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dirección de Envío") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(FontAwesomeIcons.Solid.ArrowLeft, "Volver")
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = { Text("Dirección (Calle y Número)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.additionalInfo,
                    onValueChange = viewModel::onAdditionalInfoChange,
                    label = { Text("Información Adicional (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.city,
                    onValueChange = viewModel::onCityChange,
                    label = { Text("Ciudad") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.province,
                    onValueChange = viewModel::onProvinceChange,
                    label = { Text("Provincia") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Teléfono de Contacto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { navigator.push(CheckoutPaymentScreen) },
                    enabled = viewModel.canProceedToPayment(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar al Pago")
                }
            }
        }
    }
}
