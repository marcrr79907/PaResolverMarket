package com.market.paresolvershop.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreConfig(
    @SerialName("store_name") val storeName: String = "Shop",
    @SerialName("shipping_fee") val shippingFee: Double = 0.0,
    @SerialName("tax_fee") val taxFee: Double = 0.0,
    @SerialName("currency_symbol") val currencySymbol: String = "$"
)
