package com.market.paresolvershop.domain.auth

import com.market.paresolvershop.data.repository.AuthRepository

class ObserveAuthState(private val repository: AuthRepository) {

    operator fun invoke() = repository.authState

}
