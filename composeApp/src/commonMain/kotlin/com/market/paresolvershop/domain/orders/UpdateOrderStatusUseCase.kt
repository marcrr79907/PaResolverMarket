package com.market.paresolvershop.domain.orders

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult

class UpdateOrderStatusUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, newStatus: String): DataResult<Unit> {
        // Validación de negocio básica
        val validStatuses = listOf("pending", "shipped", "delivered", "cancelled")
        if (newStatus !in validStatuses) {
            return DataResult.Error("Estado de pedido no válido")
        }

        return orderRepository.updateOrderStatus(orderId, newStatus)
    }
}
