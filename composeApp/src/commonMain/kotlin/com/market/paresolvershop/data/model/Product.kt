package com.market.paresolvershop.data.model

import com.market.paresolvershop.domain.model.Product

/**
 * Clase que se mapea directamente a/desde Firestore.
 * */
data class ProductEntity(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val category: String = ""
)

/**
 * Función de extensión para convertir el objeto de Datos (Entity)
 * al objeto de Dominio (el que usa la app).
 * */
fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,
        imageUrl = this.imageUrl,
        category = this.category
    )
}

/**
 * Función de extensión para convertir el objeto de Dominio (el que usa la app)
 * al objeto de Datos (Entity).
 * */
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,
        imageUrl = this.imageUrl,
        category = this.category
    )
}
