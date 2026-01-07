package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository

class SignInWithEmail(private val repository: AuthRepository) {
    suspend operator fun invoke(user: String, password: String) = repository.signInWithEmailAndPassword(user, password)
}