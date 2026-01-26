package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelo de negocio principal para un producto.
@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    @SerialName("image_url") // Mapea el nombre de la columna en Supabase
    val imageUrl: String,
    val category: String
)
