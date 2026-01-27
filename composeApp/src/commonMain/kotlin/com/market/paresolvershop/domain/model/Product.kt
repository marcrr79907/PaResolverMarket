package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelo de negocio principal para un producto.
@Serializable
data class Product(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("price")
    val price: Double,
    @SerialName("stock")
    val stock: Int,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("category")
    val category: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
