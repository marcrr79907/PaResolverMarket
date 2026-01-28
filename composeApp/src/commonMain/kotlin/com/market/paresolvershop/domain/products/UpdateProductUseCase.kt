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
            val oldImageUrl = product.imageUrl
            var finalImageUrl = oldImageUrl
            var hasNewImage = false

            // If there's a new image, upload it first
            if (newImageBytes != null && newImageBytes.isNotEmpty()) {
                val uploadResult = storageRepository.uploadImage(newImageBytes, product.name)
                if (uploadResult is DataResult.Success) {
                    finalImageUrl = uploadResult.data
                    hasNewImage = true
                } else {
                    return uploadResult as DataResult.Error
                }
            }

            val updatedProduct = product.copy(imageUrl = finalImageUrl)
            val dbResult = productRepository.updateProduct(updatedProduct)

            if (dbResult is DataResult.Success && hasNewImage && !oldImageUrl.isNullOrEmpty()) {
                // Delete old image for clean
                storageRepository.deleteImage(oldImageUrl)
            }

            dbResult

        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al actualizar producto")
        }
    }
}
