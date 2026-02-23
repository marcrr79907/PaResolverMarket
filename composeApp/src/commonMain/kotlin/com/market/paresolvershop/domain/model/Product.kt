package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: Double,
    @SerialName("stock") val stock: Int,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("vendor_id") val vendorId: String? = null,
    @SerialName("status") val status: String = "approved",
    @SerialName("created_at") val createdAt: String? = null,
    
    // Campos para E-commerce profesional
    val images: List<String> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val categoryName: String? = null // El nombre vendrá de un JOIN en la base de datos
)

@Serializable
data class ProductVariant(
    val id: String,
    val productId: String,
    val name: String,
    val price: Double?,
    val stock: Int,
    val sku: String? = null
)
