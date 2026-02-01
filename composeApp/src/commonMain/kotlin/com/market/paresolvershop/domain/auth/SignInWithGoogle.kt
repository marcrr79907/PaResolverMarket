package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository

/**
 * Caso de uso para iniciar sesión con Google.
 */
class SignInWithGoogle(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String, nonce: String?) = repository.signInWithGoogle(idToken, nonce)
}
