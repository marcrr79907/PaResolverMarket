package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository

class SignOut(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}