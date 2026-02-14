package com.market.paresolvershop.domain.orders

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.OrderItem
import com.market.paresolvershop.domain.model.Product

class GetOrderItemsUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): DataResult<List<Pair<OrderItem, Product>>> {
        return orderRepository.getOrderItems(orderId)
    }
}
