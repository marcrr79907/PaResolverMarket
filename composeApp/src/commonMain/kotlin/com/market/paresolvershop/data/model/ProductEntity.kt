package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: Double,
    @SerialName("stock") val stock: Int,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category") val category: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("vendor_id") val vendorId: String? = null,
    @SerialName("status") val status: String = "approved"
)

fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id ?: "",
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,
        imageUrl = this.imageUrl,
        category = this.category,
        categoryId = this.categoryId,
        vendorId = this.vendorId,
        status = this.status
    )
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = if (this.id.isBlank()) null else this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,
        imageUrl = this.imageUrl,
        category = this.category,
        categoryId = this.categoryId,
        vendorId = this.vendorId,
        status = this.status
    )
}
