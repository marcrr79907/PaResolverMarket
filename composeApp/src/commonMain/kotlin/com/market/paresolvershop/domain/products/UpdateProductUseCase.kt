package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product

class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        product: Product,
        newImageBytes: ByteArray? // Null if the image is not being changed
    ): DataResult<Unit> {
        return try {
            // 1. If there's a new image, upload it first
            val imageUrl = if (newImageBytes != null && newImageBytes.isNotEmpty()) {
                when (val uploadResult = storageRepository.uploadImage(newImageBytes, product.name)) {
                    is DataResult.Success -> uploadResult.data
                    is DataResult.Error -> return uploadResult // Propagate the error
                }
            } else {
                product.imageUrl // Keep the old image URL
            }

            // 2. Create the updated product object
            val updatedProduct = product.copy(imageUrl = imageUrl)

            // 3. Update the product in the repository
            productRepository.updateProduct(updatedProduct)

        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error desconocido al actualizar el producto")
        }
    }
}
