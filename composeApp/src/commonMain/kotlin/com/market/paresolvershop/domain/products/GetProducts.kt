package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.Flow

class GetProducts(private val repository: ProductRepository) {
    operator fun invoke(categoryId: String? = null): Flow<List<Product>> {
        return repository.getProducts(categoryId)
    }
}
