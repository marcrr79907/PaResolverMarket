package com.market.paresolvershop.domain.orders

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order

class GetAllOrdersAdminUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(): DataResult<List<Order>> = repository.fetchAllOrdersAdmin()
}
