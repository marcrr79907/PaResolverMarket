package com.market.paresolvershop.domain.cart

import com.market.paresolvershop.data.repository.CartRepository

class ClearCartUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke() {
        cartRepository.clearCart()
    }
}
