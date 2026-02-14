package com.market.paresolvershop.domain.cart

import com.market.paresolvershop.data.repository.CartRepository

class RemoveFromCartUseCase (
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(productId: String) {
        return cartRepository.removeFromCart(productId)
    }
}