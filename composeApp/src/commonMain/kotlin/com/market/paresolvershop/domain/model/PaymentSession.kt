package com.market.paresolvershop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StripeSessionResponse(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String
)
