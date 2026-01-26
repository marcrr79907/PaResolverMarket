package com.market.paresolvershop.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val role: String = "client"
)
