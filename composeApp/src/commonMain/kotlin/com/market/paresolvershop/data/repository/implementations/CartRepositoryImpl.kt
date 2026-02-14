package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.CartItemEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class CartRepositoryImpl(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) : CartRepository {

    private val cartRefreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartRefreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            val userId = authRepository.getCurrentUser()?.id
            flow {
                if (userId == null) {
                    emit(emptyList())
                } else {
                    emit(fetchCartItemsForUser(userId))
                }
            }
        }
    }

    private suspend fun fetchCartItemsForUser(userId: String): List<CartItem> {
        return try {
            val entities = supabase.from("cart_items")
                .select(
                    columns = Columns.raw("*, products(*)")
                ) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<CartItemEntity>()

            entities.mapNotNull { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addToCart(product: Product, quantity: Int): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id ?: return DataResult.Error("Inicia sesión")
        return try {
            val existingItem = supabase.from("cart_items").select {
                filter { eq("user_id", userId); eq("product_id", product.id) }
            }.decodeSingleOrNull<CartItemEntity>()

            if (existingItem != null) {
                val newQuantity = existingItem.quantity + quantity
                if (product.stock >= newQuantity) {
                    supabase.from("cart_items").update({ CartItemEntity::quantity setTo newQuantity }) {
                        filter { eq("user_id", userId); eq("product_id", product.id) }
                    }
                } else return DataResult.Error("Sin stock suficiente (Disponible: ${product.stock})")
            } else {
                if (product.stock >= quantity) {
                    val cartItem = CartItem(product, quantity)
                    supabase.from("cart_items").insert(cartItem.toEntity(userId))
                } else return DataResult.Error("Sin stock suficiente (Disponible: ${product.stock})")
            }
            cartRefreshTrigger.emit(Unit)
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al añadir al carrito")
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id ?: return DataResult.Error("Inicia sesión")
        return try {
            if (quantity > 0) {
                // Verificar stock antes de actualizar
                val product = supabase.from("products").select {
                    filter { eq("id", productId) }
                }.decodeSingle<Product>()

                if (quantity > product.stock) {
                    return DataResult.Error("Límite de stock alcanzado (${product.stock})")
                }

                supabase.from("cart_items").update({ CartItemEntity::quantity setTo quantity }) {
                    filter { eq("user_id", userId); eq("product_id", productId) }
                }
            } else {
                removeFromCart(productId)
            }
            cartRefreshTrigger.emit(Unit)
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al actualizar cantidad")
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val userId = authRepository.getCurrentUser()?.id ?: return
        try {
            supabase.from("cart_items").delete {
                filter { eq("user_id", userId); eq("product_id", productId) }
            }
            cartRefreshTrigger.emit(Unit)
        } catch (e: Exception) {}
    }

    override suspend fun clearCart() {
        val userId = authRepository.getCurrentUser()?.id ?: return
        try {
            supabase.from("cart_items").delete {
                filter { eq("user_id", userId) }
            }
            cartRefreshTrigger.emit(Unit)
        } catch (e: Exception) {}
    }
}
