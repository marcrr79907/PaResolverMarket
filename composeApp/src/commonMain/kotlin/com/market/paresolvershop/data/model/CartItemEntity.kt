package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.CartItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemEntity(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("product_id") val productId: String,
    @SerialName("quantity") val quantity: Int,
    // Supabase devuelve el objeto unido bajo el nombre de la tabla
    @SerialName("products") val product: ProductEntity? = null
)

fun CartItemEntity.toDomain(): CartItem? {
    // Si no tenemos el producto (por ejemplo en una inserción), no podemos crear el dominio completo
    val domainProduct = product?.toDomain() ?: return null
    return CartItem(
        product = domainProduct,
        quantity = this.quantity
    )
}

// Para guardar en la DB, solo necesitamos los IDs y la cantidad
fun CartItem.toEntity(userId: String): CartItemEntity = CartItemEntity(
    userId = userId,
    productId = this.product.id,
    quantity = this.quantity
)
