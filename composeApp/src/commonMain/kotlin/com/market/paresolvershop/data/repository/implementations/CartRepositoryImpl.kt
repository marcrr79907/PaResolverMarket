package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemEntity(
    @SerialName("user_id")
    val userId: String,
    @SerialName("product_id")
    val productId: String,
    val quantity: Int
)

class CartRepositoryImpl(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) : CartRepository {

    // Canal para notificar cambios y forzar recarga del Flow
    private val cartRefreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartRefreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            val user = authRepository.getCurrentUser()
            flow {
                if (user == null) {
                    emit(emptyList())
                } else {
                    emit(fetchCartItemsForUser(user.id))
                }
            }
        }
    }

    private suspend fun fetchCartItemsForUser(userId: String): List<CartItem> {
        return try {
            val cartEntities = supabase.from("cart_items")
                .select()
                .decodeList<CartItemEntity>()
                .filter { it.userId == userId }

            cartEntities.mapNotNull { cartEntity ->
                val product = supabase.from("products")
                    .select { filter { eq("id", cartEntity.productId) } }
                    .decodeSingleOrNull<Product>()

                product?.let { CartItem(product = it, quantity = cartEntity.quantity) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addToCart(product: Product): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id ?: return DataResult.Error("Inicia sesión")
        return try {
            val existingItem = supabase.from("cart_items").select {
                filter { eq("user_id", userId); eq("product_id", product.id) }
            }.decodeSingleOrNull<CartItemEntity>()

            if (existingItem != null) {
                if (product.stock > existingItem.quantity) {
                    supabase.from("cart_items").update({ CartItemEntity::quantity setTo existingItem.quantity + 1 }) {
                        filter { eq("user_id", userId); eq("product_id", product.id) }
                    }
                } else return DataResult.Error("Sin stock suficiente")
            } else {
                supabase.from("cart_items").insert(CartItemEntity(userId, product.id, 1))
            }
            cartRefreshTrigger.emit(Unit) // Notificar cambio
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error")
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id ?: return DataResult.Error("Inicia sesión")
        return try {
            if (quantity > 0) {
                supabase.from("cart_items").update({ CartItemEntity::quantity setTo quantity }) {
                    filter { eq("user_id", userId); eq("product_id", productId) }
                }
            } else {
                removeFromCart(productId)
            }
            cartRefreshTrigger.emit(Unit) // Notificar cambio inmediato
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error")
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val userId = authRepository.getCurrentUser()?.id ?: return
        try {
            supabase.from("cart_items").delete {
                filter { eq("user_id", userId); eq("product_id", productId) }
            }
            cartRefreshTrigger.emit(Unit) // Notificar cambio
        } catch (e: Exception) {}
    }

    override suspend fun clearCart() {
        val userId = authRepository.getCurrentUser()?.id ?: return
        try {
            supabase.from("cart_items").delete {
                filter { eq("user_id", userId) }
            }
            cartRefreshTrigger.emit(Unit) // Notificar cambio
        } catch (e: Exception) {}
    }
}
