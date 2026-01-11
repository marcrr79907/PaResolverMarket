package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * El contrato que el dominio necesita.
 * No sabe si los datos vienen de Firestore, una API REST o una BD local.
 * */
interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): DataResult<Product>
    suspend fun createProduct(product: Product): DataResult<Unit>
    suspend fun updateProduct(product: Product): DataResult<Unit>
    suspend fun deleteProduct(productId: String): DataResult<Unit>
}
