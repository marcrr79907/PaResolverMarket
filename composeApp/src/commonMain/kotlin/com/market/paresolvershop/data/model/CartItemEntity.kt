package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.CartItem

data class CartItemEntity(
    val product: ProductEntity = ProductEntity(),
    val quantity: Int = 0
)

fun CartItemEntity.toDomain(): CartItem = CartItem(
    product = this.product.toDomain(),
    quantity = this.quantity
)

fun CartItem.toEntity(): CartItemEntity = CartItemEntity(
    product = this.product.toEntity(),
    quantity = this.quantity
)
