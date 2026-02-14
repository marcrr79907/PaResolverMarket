package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository

class IsUserLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.getCurrentUser() != null
    }
}
