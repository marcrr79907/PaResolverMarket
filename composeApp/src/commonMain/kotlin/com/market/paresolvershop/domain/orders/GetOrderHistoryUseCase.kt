package com.market.paresolvershop.domain.orders

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import kotlinx.coroutines.flow.StateFlow

class GetOrderHistoryUseCase(
    private val orderRepository: OrderRepository
) {
    // Exponemos el StateFlow del repositorio para observar cambios en tiempo real
    val orders: StateFlow<List<Order>> = orderRepository.orders

    suspend operator fun invoke(): DataResult<Unit> {
        return orderRepository.fetchOrders()
    }
}
