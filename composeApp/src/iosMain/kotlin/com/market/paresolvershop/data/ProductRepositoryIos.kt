package com.market.paresolvershop.data

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepositoryIos: ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getProductById(id: String): DataResult<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun createProduct(product: Product): DataResult<Unit> {
        TODO("Not yet implemented")
    }
}