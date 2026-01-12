package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    fun getCartItems(): Flow<List<CartItem>>

    suspend fun addToCart(product: Product): DataResult<Unit>

    suspend fun updateQuantity(productId: String, quantity: Int)

    suspend fun removeFromCart(productId: String)

    suspend fun clearCart()
}
