package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("address_id")
    val addressId: String,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("status")
    val status: String = "pending",
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class OrderItem(
    @SerialName("id")
    val id: String? = null,
    @SerialName("order_id")
    val orderId: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("price_at_purchase")
    val priceAtPurchase: Double
)
