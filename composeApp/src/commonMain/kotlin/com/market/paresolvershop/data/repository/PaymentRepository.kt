package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StripeSessionResponse

interface PaymentRepository {
    suspend fun createStripeSession(
        orderId: String, 
        totalAmount: Double,
        customerEmail: String,
        customerName: String
    ): DataResult<StripeSessionResponse>
}
