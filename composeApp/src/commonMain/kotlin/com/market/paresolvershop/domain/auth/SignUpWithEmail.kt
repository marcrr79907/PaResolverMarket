package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository

class SignUpWithEmail(private val repository: AuthRepository) {

    /**
     * Ejecuta el registro de usuario.
     *
     * @param name El nombre completo del usuario (para guardar en el perfil).
     * @param email El correo electrónico.
     * @param password La contraseña.
     * @return AuthResult (Success o Error).
     */
    suspend operator fun invoke(name: String, email: String, password: String) = repository.signUpWithEmail(name, email, password)
}
