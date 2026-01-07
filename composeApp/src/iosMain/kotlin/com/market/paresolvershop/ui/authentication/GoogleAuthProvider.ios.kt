package com.market.paresolvershop.ui.authentication

import androidx.compose.runtime.Composable

@Composable
actual fun GoogleAuthUiProvider(
    onGoogleSignInResult: (idToken: String?) -> Unit,
    content: @Composable ((onClick: () -> Unit) -> Unit)
) {
}