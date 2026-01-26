package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemEntity(
    @SerialName("user_id")
    val userId: String,
    @SerialName("product_id")
    val productId: String,
    val quantity: Int,
    // Datos del producto embebidos (si usas una view o join)
    @SerialName("product_name")
    val productName: String? = null,
    @SerialName("product_price")
    val productPrice: Double? = null,
    @SerialName("product_description")
    val productDescription: String? = null,
    @SerialName("product_stock")
    val productStock: Int? = null,
    @SerialName("product_image_url")
    val productImageUrl: String? = null,
    @SerialName("product_category")
    val productCategory: String? = null
)

class CartRepositoryImpl(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) : CartRepository {

    /**
     * Obtiene los items del carrito.
     * Nota: Si necesitas actualización en tiempo real, tendrás que implementar
     * un mecanismo de polling o usar Realtime correctamente cuando la API lo permita.
     */
    override fun getCartItems(): Flow<List<CartItem>> {
        return authRepository.authState.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                flow {
                    try {
                        val items = fetchCartItemsForUser(user.id)
                        emit(items)
                    } catch (e: Exception) {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    private suspend fun fetchCartItemsForUser(userId: String): List<CartItem> {
        return try {
            // Opción 1: Si tienes una view que hace join entre cart_items y products
            // val entities = supabase.from("cart_items_with_products")
            //     .select { filter { eq("user_id", userId) } }
            //     .decodeList<CartItemEntity>()

            // Opción 2: Consultar cart_items y luego productos (2 queries)
            val cartEntities = supabase.from("cart_items")
                .select()
                .decodeList<CartItemEntity>()
                .filter { it.userId == userId }

            cartEntities.mapNotNull { cartEntity ->
                // Obtener el producto completo
                val product = supabase.from("products")
                    .select {
                        filter { eq("id", cartEntity.productId) }
                    }
                    .decodeSingleOrNull<Product>()

                product?.let {
                    CartItem(
                        product = it,
                        quantity = cartEntity.quantity
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addToCart(product: Product): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id
            ?: return DataResult.Error("Usuario no autenticado")

        return try {
            // Verificar si ya existe en el carrito
            val existingItem = supabase.from("cart_items")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("product_id", product.id)
                    }
                }
                .decodeSingleOrNull<CartItemEntity>()

            if (existingItem != null) {
                // Verificar stock antes de incrementar
                if (product.stock > existingItem.quantity) {
                    // Actualizar cantidad
                    supabase.from("cart_items")
                        .update({
                            CartItemEntity::quantity setTo existingItem.quantity + 1
                        }) {
                            filter {
                                eq("user_id", userId)
                                eq("product_id", product.id)
                            }
                        }
                } else {
                    return DataResult.Error("Máximo stock para \"${product.name}\" en su carrito")
                }
            } else {
                // Crear nuevo item
                val newItem = CartItemEntity(
                    userId = userId,
                    productId = product.id,
                    quantity = 1
                )
                supabase.from("cart_items").insert(newItem)
            }

            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al añadir al carrito")
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): DataResult<Unit> {
        val userId = authRepository.getCurrentUser()?.id
            ?: return DataResult.Error("Usuario no autenticado")

        return try {
            if (quantity > 0) {
                // Verificar stock disponible
                val product = supabase.from("products")
                    .select {
                        filter { eq("id", productId) }
                    }
                    .decodeSingleOrNull<Product>()

                if (product != null && quantity > product.stock) {
                    return DataResult.Error("La cantidad solicitada supera el stock disponible.")
                }

                // Actualizar cantidad
                supabase.from("cart_items")
                    .update({
                        CartItemEntity::quantity setTo quantity
                    }) {
                        filter {
                            eq("user_id", userId)
                            eq("product_id", productId)
                        }
                    }
            } else {
                // Si la cantidad es 0, eliminar del carrito
                removeFromCart(productId)
            }

            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error actualizando la cantidad")
        }
    }

    override suspend fun removeFromCart(productId: String) {
        val userId = authRepository.getCurrentUser()?.id ?: return

        try {
            supabase.from("cart_items").delete {
                filter {
                    eq("user_id", userId)
                    eq("product_id", productId)
                }
            }
        } catch (e: Exception) {
            // Ignorar errores en delete
        }
    }

    override suspend fun clearCart() {
        val userId = authRepository.getCurrentUser()?.id ?: return

        try {
            supabase.from("cart_items").delete {
                filter { eq("user_id", userId) }
            }
        } catch (e: Exception) {
            // Ignorar errores
        }
    }
}
