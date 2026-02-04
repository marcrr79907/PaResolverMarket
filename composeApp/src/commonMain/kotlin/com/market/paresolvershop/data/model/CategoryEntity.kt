package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("icon_name") val iconName: String? = null
)

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = this.id ?: "",
        name = this.name,
        iconName = this.iconName
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = if (this.id.isBlank()) null else this.id,
        name = this.name,
        iconName = this.iconName
    )
}
