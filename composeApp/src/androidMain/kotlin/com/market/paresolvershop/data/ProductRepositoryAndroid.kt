package com.market.paresolvershop.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.market.paresolvershop.data.model.ProductEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepositoryAndroid : ProductRepository {

    private val db = Firebase.firestore

    override fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { document ->
                    document.toObject<ProductEntity>()?.copy(id = document.id)
                }?.map { it.toDomain() } ?: emptyList()

                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getProductById(id: String): DataResult<Product> {
        return try {
            val document = db.collection("products").document(id).get().await()
            val product = document.toObject<ProductEntity>()?.copy(id = document.id)?.toDomain()
            if (product != null) {
                DataResult.Success(product)
            } else {
                DataResult.Error("Producto no encontrado: $id")
            }
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener el producto")
        }
    }

    override suspend fun createProduct(product: Product): DataResult<Unit> {
        return try {
            // 1. Convierte el modelo de dominio a la entidad de datos
            val productEntity = product.toEntity()
            // 2. Guarda la entidad en Firestore
            db.collection("products").add(productEntity).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al crear el producto")
        }
    }
}
