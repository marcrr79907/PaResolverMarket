package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.OrderEntity
import com.market.paresolvershop.data.model.OrderItemEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class OrderRepositoryImpl(
    private val supabase: SupabaseClient
) : OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    override suspend fun createOrder(order: Order, items: List<OrderItem>): DataResult<String> = withContext(Dispatchers.Default) {
        try {
            // Convertimos el dominio a entidad pura de DB (sin campos de join)
            val dbOrder = order.toEntity()

            val insertedOrder = supabase.from("orders").insert(dbOrder) {
                select()
            }.decodeSingle<OrderEntity>()

            val orderId = insertedOrder.id ?: throw Exception("No order ID returned")

            // Convertimos items a entidades
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
            val user = supabase.auth.currentUserOrNull() ?: return@withContext DataResult.Error("Inicia sesión")
            
            // SOLUCIÓN AL ERROR DE TIPO: Usamos Columns.raw para el Join
            val result = supabase.from("orders").select(
                columns = Columns.raw("*, user_addresses(first_name, last_name)")
            ) {
                filter { eq("user_id", user.id) }
                order("created_at", SupabaseOrderDirection.DESCENDING)
            }.decodeList<OrderEntity>()
            
            // Mapeamos a Dominio usando la función de extensión
            _orders.value = result.map { it.toDomain() }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener pedidos")
        }
    }

    override suspend fun getOrderItems(orderId: String): DataResult<List<Pair<OrderItem, Product>>> = withContext(Dispatchers.Default) {
        try {
            val items = supabase.from("order_items").select {
                filter { eq("order_id", orderId) }
            }.decodeList<OrderItemEntity>()

            val result = items.mapNotNull { entity ->
                val product = supabase.from("products").select {
                    filter { eq("id", entity.productId) }
                }.decodeSingleOrNull<Product>()
                
                // Mapeamos de vuelta a dominio para la UI
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
}
