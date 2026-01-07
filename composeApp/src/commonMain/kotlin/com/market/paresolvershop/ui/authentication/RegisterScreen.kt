package com.market.paresolvershop.ui.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Google
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object RegisterScreen : Screen {

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // 1. Solo necesitamos el RegisterViewModel
        val viewModel = koinViewModel<RegisterViewModel>()
        val state by viewModel.uiState.collectAsState()

        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        // 2. El LaunchedEffect ahora solo depende de 'state'
        LaunchedEffect(state) {
            if (state is RegisterUiState.Success) {
                navigator.pop()
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crea tu Cuenta", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // --- Formulario de Registro con Email ---
            TextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Manejo de estado simplificado
            when(val currentState = state) {
                is RegisterUiState.Loading -> CircularProgressIndicator()
                is RegisterUiState.Error -> Text(currentState.message, color = MaterialTheme.colorScheme.error)
                else -> { /* Idle o Success, no mostramos nada extra */ }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botones de Acción ---
            Button(
                onClick = { viewModel.onRegisterClick(name, email, password, confirmPassword) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is RegisterUiState.Loading
            ) {
                Text("Registrarse con Email")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(modifier = Modifier.height(16.dp))

            // 4. El botón de Google ahora llama al RegisterViewModel
            GoogleAuthUiProvider(
                onGoogleSignInResult = { idToken ->
                    if (idToken != null) {
                        // Llamamos a la nueva función en nuestro ViewModel
                        viewModel.onGoogleRegisterSuccess(idToken)
                    } else {
                        viewModel.onError("El registro con Google fue cancelado.")
                    }
                }
            ) { onClickLambda ->
                Button(
                    onClick = onClickLambda,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is RegisterUiState.Loading
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Brands.Google,
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Continuar con Google")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    // Navega a la pantalla de Login en lugar de simplemente hacer pop
                    navigator.pop() // Cierra la pantalla actual
                    navigator.push(LoginScreen) // Abre la de Login
                },
                enabled = state !is RegisterUiState.Loading
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}