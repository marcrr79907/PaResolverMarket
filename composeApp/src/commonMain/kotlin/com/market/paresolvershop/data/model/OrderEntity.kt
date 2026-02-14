package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("address_id") val addressId: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("status") val status: String = "pending",
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("created_at") val createdAt: String? = null,

    @SerialName("user_addresses") val address: AddressJoinEntity? = null,
    @SerialName("users") val user: UserJoinEntity? = null
)

@Serializable
data class AddressJoinEntity(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("address_line") val addressLine: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("city") val city: String? = null
)

@Serializable
data class UserJoinEntity(
    @SerialName("name") val name: String
)

@Serializable
data class OrderItemEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price_at_purchase") val priceAtPurchase: Double
)

@Serializable
data class OrderItemWithProductEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price_at_purchase") val priceAtPurchase: Double,
    @SerialName("products") val product: Product
)

// Mapeadores
fun OrderEntity.toDomain(): Order = Order(
    id = id,
    userId = userId,
    addressId = addressId,
    totalAmount = totalAmount,
    status = status,
    paymentMethod = paymentMethod,
    createdAt = createdAt,
    recipientFirstName = address?.firstName,
    recipientLastName = address?.lastName,
    recipientAddress = address?.addressLine,
    recipientPhone = address?.phone,
    recipientCity = address?.city,
    customerName = user?.name
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    userId = userId,
    addressId = addressId,
    totalAmount = totalAmount,
    status = status,
    paymentMethod = paymentMethod,
    createdAt = createdAt
)

fun OrderItem.toEntity(): OrderItemEntity = OrderItemEntity(
    id = id,
    orderId = orderId,
    productId = productId,
    quantity = quantity,
    priceAtPurchase = priceAtPurchase
)

fun OrderItemWithProductEntity.toDomainPair(): Pair<OrderItem, Product> =
    OrderItem(
        id = id,
        orderId = orderId,
        productId = productId,
        quantity = quantity,
        priceAtPurchase = priceAtPurchase
    ) to product
