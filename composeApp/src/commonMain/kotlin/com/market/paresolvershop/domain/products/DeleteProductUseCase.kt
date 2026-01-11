package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult

class DeleteProductUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(productId: String): DataResult<Unit> {
        return productRepository.deleteProduct(productId)
    }
}
