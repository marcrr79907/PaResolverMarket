package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAddress(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    @SerialName("address_line")
    val addressLine: String = "",
    @SerialName("company")
    val company: String? = null,
    @SerialName("phone")
    val phone: String = "",
    @SerialName("city")
    val city: String = "",
    @SerialName("is_default")
    val isDefault: Boolean = false
)
