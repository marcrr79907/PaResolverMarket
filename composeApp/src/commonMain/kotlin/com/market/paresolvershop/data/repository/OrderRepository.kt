package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem

interface OrderRepository {
    suspend fun createOrder(order: Order, items: List<OrderItem>): DataResult<String>
    suspend fun getMyOrders(): DataResult<List<Order>>
}
