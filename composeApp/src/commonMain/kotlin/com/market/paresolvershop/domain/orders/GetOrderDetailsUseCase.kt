package com.market.paresolvershop.domain.orders

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order

class GetOrderDetailsUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): DataResult<Order> {
        return orderRepository.getOrderById(orderId)
    }
}
