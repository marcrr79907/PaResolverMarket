package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String,
    @SerialName("icon_name") val iconName: String? = null // Para guardar el nombre del icono de FontAwesome
)
