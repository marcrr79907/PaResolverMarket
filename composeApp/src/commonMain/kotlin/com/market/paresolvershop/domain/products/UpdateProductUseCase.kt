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
        newMainImageBytes: ByteArray?,
        newAdditionalImages: List<ByteArray> = emptyList()
    ): DataResult<Unit> {
        return try {
            var finalMainImageUrl = product.imageUrl
            val currentAdditionalImages = product.images.toMutableList()

            // 1. Actualizar imagen principal si hay una nueva
            if (newMainImageBytes != null && newMainImageBytes.isNotEmpty()) {
                val uploadResult = storageRepository.uploadImage(newMainImageBytes, "${product.name}-main-update")
                if (uploadResult is DataResult.Success) {
                    finalMainImageUrl = uploadResult.data
                }
            }

            // 2. Subir nuevas imágenes adicionales
            newAdditionalImages.forEachIndexed { index, bytes ->
                val res = storageRepository.uploadImage(bytes, "${product.name}-extra-update-$index")
                if (res is DataResult.Success) {
                    currentAdditionalImages.add(res.data)
                }
            }

            val updatedProduct = product.copy(
                imageUrl = finalMainImageUrl,
                images = currentAdditionalImages
            )
            
            productRepository.updateProduct(updatedProduct)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al actualizar producto")
        }
    }
}
