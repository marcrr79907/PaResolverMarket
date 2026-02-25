package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.domain.model.DataResult

class DeleteAccountUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): DataResult<Unit> {
        return authRepository.deleteAccount()
    }
}
