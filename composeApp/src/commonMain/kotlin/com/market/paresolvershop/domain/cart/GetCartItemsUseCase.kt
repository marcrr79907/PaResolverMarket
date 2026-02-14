package com.market.paresolvershop.domain.cart

import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

class GetCartItemsUseCase (
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return cartRepository.getCartItems()
    }
}