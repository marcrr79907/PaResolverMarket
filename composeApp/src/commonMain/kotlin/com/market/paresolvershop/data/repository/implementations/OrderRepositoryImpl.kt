package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.OrderEntity
import com.market.paresolvershop.data.model.OrderItemEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrderDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderRepositoryImpl(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) : OrderRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        repositoryScope.launch {
            authRepository.authState.collect { user ->
                if (user != null) {
                    fetchOrders()
                } else {
                    _orders.value = emptyList()
                }
            }
        }
    }

    override suspend fun createOrder(order: Order, items: List<OrderItem>): DataResult<String> = withContext(Dispatchers.Default) {
        try {
            val dbOrder = order.toEntity()
            val insertedOrder = supabase.from("orders").insert(dbOrder) {
                select()
            }.decodeSingle<OrderEntity>()

            val orderId = insertedOrder.id ?: throw Exception("No order ID returned")

            val dbItems = items.map { it.copy(orderId = orderId).toEntity() }
            supabase.from("order_items").insert(dbItems)

            fetchOrders()
            DataResult.Success(orderId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al crear el pedido")
        }
    }

    override suspend fun fetchOrders(): DataResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val user = supabase.auth.currentUserOrNull() ?:
            return@withContext DataResult.Error("Inicia sesión")
            
            // Eliminamos users(name) de aquí porque el usuario no necesita ver su propio nombre en cada card
            val result = supabase.from("orders").select(
                columns = Columns.raw("*, user_addresses(*)")
            ) {
                filter { eq("user_id", user.id) }
                order("created_at", SupabaseOrderDirection.DESCENDING)
            }.decodeList<OrderEntity>()
            
            _orders.value = result.map { it.toDomain() }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener pedidos")
        }
    }

    override suspend fun fetchAllOrdersAdmin(): DataResult<List<Order>> = withContext(Dispatchers.Default) {
        try {
            // El admin sí necesita el join con users(name)
            val result = supabase.from("orders").select(
                columns = Columns.raw("*, user_addresses(*), users(name)")
            ) {
                order("created_at", SupabaseOrderDirection.DESCENDING)
            }.decodeList<OrderEntity>()
            
            DataResult.Success(result.map { it.toDomain() })
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al cargar todas las órdenes")
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String): DataResult<Unit> = withContext(Dispatchers.Default) {
        try {
            supabase.from("orders").update(mapOf("status" to newStatus)) {
                filter { eq("id", orderId) }
            }
            fetchOrders() 
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al actualizar estado")
        }
    }

    override suspend fun getOrderItems(orderId: String): DataResult<List<Pair<OrderItem, Product>>> = withContext(Dispatchers.Default) {
        try {
            val entities = supabase.from("order_items")
                .select(columns = Columns.raw("*, products(*)")) {
                    filter { eq("order_id", orderId) }
                }.decodeList<OrderItemEntity>()

            val result = entities.mapNotNull { entity ->
                val product = supabase.from("products").select {
                    filter { eq("id", entity.productId) }
                }.decodeSingleOrNull<Product>()

                product?.let {
                    OrderItem(
                        id = entity.id,
                        orderId = entity.orderId,
                        productId = entity.productId,
                        quantity = entity.quantity,
                        priceAtPurchase = entity.priceAtPurchase
                    ) to it
                }
            }

            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener detalles")
        }
    }

    override suspend fun getOrderById(orderId: String): DataResult<Order> = withContext(Dispatchers.Default) {
        try {
            val entity = supabase.from("orders").select(
                columns = Columns.raw("*, user_addresses(*), users(name)")
            ) {
                filter { eq("id", orderId) }
            }.decodeSingle<OrderEntity>()
            
            DataResult.Success(entity.toDomain())
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener la orden")
        }
    }
}
