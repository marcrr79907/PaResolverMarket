package com.market.paresolvershop.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.market.paresolvershop.data.model.CartItemEntity
import com.market.paresolvershop.data.model.ProductEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class CartRepositoryAndroid(private val authRepository: AuthRepository) : CartRepository {

    private val db = Firebase.firestore

    private fun cartCollection(userId: String) = db.collection("users").document(userId).collection("cart")
    private fun productDocument(productId: String) = db.collection("products").document(productId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCartItems(): Flow<List<CartItem>> {
        return authRepository.authState.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val listener = cartCollection(user.id).addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val items = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject<CartItemEntity>()?.toDomain()
                        } ?: emptyList()
                        trySend(items)
                    }
                    awaitClose { listener.remove() }
                }
            }
        }
    }

    override suspend fun addToCart(product: Product): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id ?: return DataResult.Error("Usuario no autenticado")
        val cartDocRef = cartCollection(userId).document(product.id)
        val productDocRef = productDocument(product.id)

        return try {
            db.runTransaction { transaction ->
                val productSnapshot = transaction.get(productDocRef)
                val productInDb = productSnapshot.toObject<ProductEntity>()
                val availableStock = productInDb?.stock ?: 0

                val cartSnapshot = transaction.get(cartDocRef)
                val cartItem = cartSnapshot.toObject<CartItemEntity>()
                val quantityInCart = cartItem?.quantity ?: 0

                if (availableStock > quantityInCart) {
                    if (cartItem != null) {
                        transaction.update(cartDocRef, "quantity", quantityInCart + 1)
                    } else {
                        val newItem = CartItem(product, 1).toEntity()
                        transaction.set(cartDocRef, newItem)
                    }
                    null // La transacción fue exitosa
                } else {
                    // Si no hay stock, la transacción falla y devolvemos el error.
                    throw Exception("No hay suficiente stock para \"${product.name}\"")
                }
            }.await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al añadir al carrito")
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        val userId = authRepository.getCurrentUser()?.id ?: return
        val cartDocRef = cartCollection(userId).document(productId)
        val productDocRef = productDocument(productId)

        try {
             db.runTransaction { transaction ->
                val productSnapshot = transaction.get(productDocRef)
                val productInDb = productSnapshot.toObject<ProductEntity>()
                val availableStock = productInDb?.stock ?: 0

                if(quantity > availableStock) {
                     throw Exception("La cantidad solicitada supera el stock disponible.")
                }

                if (quantity > 0) {
                    transaction.update(cartDocRef, "quantity", quantity)
                } else {
                    transaction.delete(cartDocRef)
                }
                null
            }.await()
        }  catch (e: Exception) {
            DataResult.Error(e.message ?: "Error actualizando el producto")
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val userId = authRepository.getCurrentUser()?.id ?: return
        cartCollection(userId).document(productId).delete().await()
    }

    override suspend fun clearCart() {
        val userId = authRepository.getCurrentUser()?.id ?: return
        val batch = db.batch()
        val allItems = cartCollection(userId).get().await()
        for (document in allItems) {
            batch.delete(document.reference)
        }
        batch.commit().await()
    }
}
