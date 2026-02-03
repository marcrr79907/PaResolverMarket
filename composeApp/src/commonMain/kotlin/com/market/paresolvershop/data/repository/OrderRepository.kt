package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

interface OrderRepository {
    val orders: StateFlow<List<Order>>
    suspend fun createOrder(order: Order, items: List<OrderItem>): DataResult<String>
    suspend fun fetchOrders(): DataResult<Unit>
    suspend fun getOrderItems(orderId: String): DataResult<List<Pair<OrderItem, Product>>>
}
