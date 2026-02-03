package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
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
            // 1. Insertamos la cabecera del pedido
            val insertedOrder = supabase.from("orders").insert(order) {
                select()
            }.decodeSingle<Order>()

            val orderId = insertedOrder.id ?: throw Exception("No se pudo obtener el ID del pedido generado.")

            // 2. Insertamos los items del pedido vinculados al ID obtenido
            val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
            supabase.from("order_items").insert(itemsWithOrderId)

            // 3. Notificamos la creación recargando la lista
            fetchOrders()

            DataResult.Success(orderId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al crear el pedido")
        }
    }

    override suspend fun fetchOrders(): DataResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@withContext DataResult.Error("Usuario no autenticado")
            
            val result = supabase.from("orders").select {
                filter {
                    eq("user_id", user.id)
                }
                order("created_at", SupabaseOrderDirection.DESCENDING)
            }.decodeList<Order>()
            
            _orders.value = result
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener tus pedidos")
        }
    }
}
