package com.market.paresolvershop.data.model

data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val role: String = "client" // Valor por defecto para seguridad
)
