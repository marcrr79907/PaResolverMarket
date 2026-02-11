package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.ProductVariant
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
    @SerialName("category") val category: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("vendor_id") val vendorId: String? = null,
    @SerialName("status") val status: String = "approved",
    
    @SerialName("product_images") val images: List<ProductImageEntity>? = null,
    @SerialName("product_variants") val variants: List<ProductVariantEntity>? = null
)

@Serializable
data class ProductUpdateEntity(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: Double,
    @SerialName("stock") val stock: Int,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("status") val status: String
)

@Serializable
data class ProductImageEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("product_id") val product_id: String,
    @SerialName("image_url") val image_url: String,
    @SerialName("is_main") val is_main: Boolean = false
)

@Serializable
data class ProductVariantEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("product_id") val product_id: String,
    @SerialName("name") val name: String,
    @SerialName("price_override") val price_override: Double? = null,
    @SerialName("stock") val stock: Int = 0,
    @SerialName("sku") val sku: String? = null
)

fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id ?: "",
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,
        imageUrl = this.imageUrl,
        category = this.category ?: "",
        categoryId = this.categoryId,
        vendorId = this.vendorId,
        status = this.status,
        images = this.images?.map { it.image_url } ?: emptyList(),
        variants = this.variants?.map { it.toDomain() } ?: emptyList()
    )
}

fun ProductVariantEntity.toDomain(): ProductVariant {
    return ProductVariant(
        id = this.id ?: "",
        productId = this.product_id,
        name = this.name,
        price = this.price_override,
        stock = this.stock,
        sku = this.sku
    )
}

fun Product.toUpdateEntity(): ProductUpdateEntity {
    return ProductUpdateEntity(
        name = name,
        description = description,
        price = price,
        stock = stock,
        imageUrl = imageUrl,
        categoryId = categoryId,
        status = status
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
