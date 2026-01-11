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
            // Convierte el modelo de dominio a la entidad de datos
            val productEntity = product.toEntity()

            // Guarda la entidad en Firestore
            val documentReference = db.collection("products").add(productEntity).await()

            // Obtiene el ID generado por Firestore.
            val generatedId = documentReference.id

            // Actualizar el documento para añadirle su propio ID.
            db.collection("products").document(generatedId).update("id", generatedId).await()

            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al crear el producto")
        }
    }

    override suspend fun updateProduct(product: Product): DataResult<Unit> {
        return try {
            val productEntity = product.toEntity()
            db.collection("products").document(product.id).set(productEntity).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al actualizar el producto")
        }
    }

    override suspend fun deleteProduct(productId: String): DataResult<Unit> {
        return try {
            db.collection("products").document(productId).delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al eliminar el producto")
        }
    }
}
