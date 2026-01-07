package com.market.paresolvershop.domain.model

// Modelo de negocio principal para un producto.
data class Product(
    val id: String,          // ID del documento en Firestore
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String,
    val category: String
)
