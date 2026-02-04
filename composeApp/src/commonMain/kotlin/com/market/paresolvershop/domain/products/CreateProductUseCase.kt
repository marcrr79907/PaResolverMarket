package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product

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
        imageBytes: ByteArray
    ): DataResult<Unit> {
        // Validar campos básicos
        if (name.isBlank() || description.isBlank() || categoryId.isBlank() || price <= 0) {
            return DataResult.Error("Por favor, completa todos los campos correctamente.")
        }

        // Subir la imagen usando el nombre del producto como pista
        val imageUrlResult = storageRepository.uploadImage(imageBytes, baseNameHint = name)

        val imageUrl = when (imageUrlResult) {
            is DataResult.Success -> imageUrlResult.data
            is DataResult.Error -> return imageUrlResult
        }

        val product = Product(
            id = "",
            name = name,
            description = description,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            category = "",
            categoryId = categoryId
        )

        return productRepository.createProduct(product)
    }
}
