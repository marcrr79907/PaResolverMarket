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
import kotlinx.coroutines.withContext

class OrderRepositoryImpl(
    private val supabase: SupabaseClient
) : OrderRepository {

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

            DataResult.Success(orderId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al crear el pedido")
        }
    }

    override suspend fun getMyOrders(): DataResult<List<Order>> = withContext(Dispatchers.Default) {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@withContext DataResult.Error("Usuario no autenticado")
            
            val result = supabase.from("orders").select {
                filter {
                    eq("user_id", user.id)
                }
                order("created_at", SupabaseOrderDirection.DESCENDING)
            }.decodeList<Order>()
            
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener tus pedidos")
        }
    }
}
