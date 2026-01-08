package com.market.paresolvershop.ui.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Google
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object LoginScreen : Screen {

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<LoginViewModel>()
        val state by viewModel.uiState.collectAsState()

        var email by remember { mutableStateOf("marc@gmail.com") }
        var password by remember { mutableStateOf("123456") }

        LaunchedEffect(state) {
            if (state is LoginUiState.Success) {
                navigator.pop()
            }
        }

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))

            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            when (val currentState = state) {
                is LoginUiState.Loading -> CircularProgressIndicator()
                is LoginUiState.Error -> Text(currentState.message, color = MaterialTheme.colorScheme.error)
                else -> { /* Idle or Success */ }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onLoginClick(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is LoginUiState.Loading
            ) {
                Text("Login")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Sign-in with Google
            GoogleAuthUiProvider(
                // Cuando el proveedor de la plataforma (Android) nos devuelva el token...
                onGoogleSignInResult = { idToken ->
                    if (idToken != null) {
                        // ...se lo pasamos al ViewModel para que inicie sesión en Firebase.
                        viewModel.onGoogleLoginSuccess(idToken)
                    } else {
                        // Si el token es nulo, el usuario canceló o hubo un error.
                        viewModel.onError("El inicio de sesión con Google fue cancelado.")
                    }
                }
            ) { onClickLambda ->
                // Este es el Composable que se mostrará en la pantalla.
                // La lambda 'onClick' que recibimos es la que debemos llamar para lanzar el flujo de Google.
                Button(
                    onClick = onClickLambda,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is LoginUiState.Loading
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
        }
    }
}