package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.ProductVariant

class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        categoryId: String,
        mainImageBytes: ByteArray,
        additionalImages: List<ByteArray> = emptyList(),
        variants: List<ProductVariant> = emptyList()
    ): DataResult<Unit> {
        if (name.isBlank() || description.isBlank() || categoryId.isBlank() || price <= 0) {
            return DataResult.Error("Por favor, completa todos los campos correctamente.")
        }

        // Creamos un identificador único para la carpeta del producto (usando el nombre limpio)
        val productFolder = name.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
        val timestamp = (clockNow() / 1000).toString() // Para evitar cacheos
        val uniquePathPrefix = "$productFolder-$timestamp"

        // 1. Subir imagen principal a su propia carpeta
        val mainImageUrl = when (val res = storageRepository.uploadImage(mainImageBytes, "$uniquePathPrefix/main.jpg")) {
            is DataResult.Success -> res.data
            is DataResult.Error -> return res
        }

        // 2. Subir imágenes adicionales a la subcarpeta 'gallery'
        val imageUrls = mutableListOf<String>()
        additionalImages.forEachIndexed { index, bytes ->
            val res = storageRepository.uploadImage(bytes, "$uniquePathPrefix/gallery/img_$index.jpg")
            if (res is DataResult.Success) imageUrls.add(res.data)
        }

        val product = Product(
            id = "",
            name = name,
            description = description,
            price = price,
            stock = stock,
            imageUrl = mainImageUrl,
            category = "",
            categoryId = categoryId,
            images = imageUrls,
            variants = variants
        )

        return productRepository.createProduct(product)
    }

    private fun clockNow(): Long = 1715856000000 // Placeholder para System.currentTimeMillis() en KMP
}
