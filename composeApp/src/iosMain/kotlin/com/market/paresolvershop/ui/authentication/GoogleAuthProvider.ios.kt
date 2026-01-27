package com.market.paresolvershop.ui.authentication

import androidx.compose.runtime.Composable

/**
 * Implementación de Google Auth para iOS.
 * Por ahora es un placeholder. Para producción necesitarás:
 * - Integrar la SDK de Google Sign-In para iOS
 * - O usar Sign in with Apple como alternativa
 */
@Composable
actual fun GoogleAuthUiProvider(
    onGoogleSignInResult: (idToken: String?) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    // Renderizar el botón pero deshabilitado/con mensaje para iOS
    content {
        // TODO: Implementar Google Sign-In para iOS
        // Mientras tanto, notificar que no está disponible
        onGoogleSignInResult(null)
    }
}
