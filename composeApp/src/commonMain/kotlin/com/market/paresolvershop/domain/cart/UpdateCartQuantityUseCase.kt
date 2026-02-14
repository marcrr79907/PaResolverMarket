package com.market.paresolvershop.domain.cart

import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.DataResult

class UpdateCartQuantityUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(productId: String, quantity: Int): DataResult<Unit> {
        return cartRepository.updateQuantity(productId, quantity)
    }
}
