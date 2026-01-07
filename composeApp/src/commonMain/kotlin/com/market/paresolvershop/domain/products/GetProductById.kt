package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
/**
 * Una función suspendida que recibe el ID de un producto, y devuelve el producto o un Error.
 * */
class GetProductById(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String): DataResult<Product> {
        return repository.getProductById(id)
    }
}