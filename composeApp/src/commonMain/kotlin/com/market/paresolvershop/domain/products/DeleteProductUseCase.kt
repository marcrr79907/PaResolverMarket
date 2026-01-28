package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product

class DeleteProductUseCase(
    private val productRepository: ProductRepository,
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(product: Product): DataResult<Unit> {
        val result = productRepository.deleteProduct(product.id)
        if (result is DataResult.Success) {
            // Delete the image associate to the product deleted
            product.imageUrl?.let { storageRepository.deleteImage(it) }
        }
        return result
    }
}
