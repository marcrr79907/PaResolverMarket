package com.market.paresolvershop.data.api

import com.market.paresolvershop.domain.model.StripeSessionResponse
import com.market.paresolvershop.network.NetworkUtils
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class StripeSessionRequest(
    val orderId: String,
    val amount: Double,
    val customerEmail: String,
    val customerName: String
)

class PaymentApiService(private val baseUrl: String, private val anonKey: String) {
    
    suspend fun createStripeSession(
        orderId: String, 
        totalAmount: Double,
        email: String,
        name: String
    ): StripeSessionResponse {
        val endpoint = "$baseUrl/functions/v1/create-stripe-session"
        
        val response = NetworkUtils.httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $anonKey")
            setBody(StripeSessionRequest(orderId, totalAmount, email, name))
        }
        return response.body()
    }
}
