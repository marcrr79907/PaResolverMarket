package com.market.paresolvershop.ui.authentication

import androidx.compose.runtime.Composable

/**
 * 'expect' composable que define la interfaz para lanzar la UI de autenticación de Google.
 * La UI compartida (LoginScreen) llamará a esto, y cada plataforma (Android)
 * proporcionará su implementación 'actual'.
 *
 * @param onGoogleSignInResult Lambda que se invoca con el 'idToken' si el login es exitoso, o 'null' si falla.
 * @param content El Composable (ej. un botón) que el usuario verá y que al hacer clic, lanzará el flujo.
 */
@Composable
expect fun GoogleAuthUiProvider(
    onGoogleSignInResult: (idToken: String?) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
)
