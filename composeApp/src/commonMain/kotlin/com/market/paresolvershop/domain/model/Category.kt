package com.market.paresolvershop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String = "",
    val name: String,
    val iconName: String? = null
)
