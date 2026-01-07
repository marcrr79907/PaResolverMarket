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
        category: String,
        imageBytes: ByteArray
    ): DataResult<Unit> {
        // 1. Validar campos básicos
        if (name.isBlank() || description.isBlank() || category.isBlank() || price <= 0) {
            return DataResult.Error("Por favor, completa todos los campos correctamente.")
        }

        // 2. Subir la imagen usando el nombre del producto como pista
        val imageUrlResult = storageRepository.uploadImage(imageBytes, baseNameHint = name)

        val imageUrl = when (imageUrlResult) {
            is DataResult.Success -> imageUrlResult.data
            is DataResult.Error -> return imageUrlResult // Si la subida falla, detenemos el proceso
        }

        // 3. Crear el objeto Product
        val product = Product(
            id = "", // Firestore generará el ID automáticamente
            name = name,
            description = description,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            category = category
        )

        // 4. Guardar el producto en Firestore
        return productRepository.createProduct(product)
    }
}
