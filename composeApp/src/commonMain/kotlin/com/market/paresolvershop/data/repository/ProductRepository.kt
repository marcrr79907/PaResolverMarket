package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(categoryId: String? = null): Flow<List<Product>>
    suspend fun getProductById(id: String): DataResult<Product>
    suspend fun fetchProducts(categoryId: String? = null): DataResult<Unit>
    
    // Para el Admin y Vendedores
    suspend fun getAllProductsAdmin(): DataResult<List<Product>>
    suspend fun updateProductStatus(productId: String, status: String): DataResult<Unit>
}
