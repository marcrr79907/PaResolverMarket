package com.market.paresolvershop.domain.cart

import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product

class AddToCartUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(product: Product, quantity: Int = 1): DataResult<Unit> {
        return cartRepository.addToCart(product, quantity)
    }
}
