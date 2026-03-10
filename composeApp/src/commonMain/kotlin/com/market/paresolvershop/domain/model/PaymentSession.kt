package com.market.paresolvershop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StripeSessionResponse(
    val paymentIntent: String,
    val publishableKey: String,
    val ephemeralKey: String? = null,
    val customer: String? = null
)
