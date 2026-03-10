package com.market.paresolvershop.domain.payments

import com.market.paresolvershop.data.repository.PaymentRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StripeSessionResponse

class CreateStripeSessionUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke(
        orderId: String, 
        totalAmount: Double,
        customerEmail: String,
        customerName: String
    ): DataResult<StripeSessionResponse> {
        return repository.createStripeSession(orderId, totalAmount, customerEmail, customerName)
    }
}
