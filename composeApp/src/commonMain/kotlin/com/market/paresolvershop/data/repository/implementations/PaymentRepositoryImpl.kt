package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.api.PaymentApiService
import com.market.paresolvershop.data.repository.PaymentRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StripeSessionResponse

class PaymentRepositoryImpl(
    private val apiService: PaymentApiService
) : PaymentRepository {
    override suspend fun createStripeSession(
        orderId: String, totalAmount: Double
    ): DataResult<StripeSessionResponse> = runCatching {
        val response = apiService.createStripeSession(orderId, totalAmount)
        DataResult.Success(response)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al conectar con la pasarela de pago")
    }
}
