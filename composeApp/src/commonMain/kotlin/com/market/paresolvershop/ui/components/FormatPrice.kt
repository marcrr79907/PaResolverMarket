package com.market.paresolvershop.ui.components

import kotlin.math.roundToInt

/**
 * Función de utilidad para formatear precios en Common Main (KMP)
 */
fun Double.formatPrice(): String {
    val rounded = (this * 100).roundToInt() / 100.0
    val parts = rounded.toString().split(".")
    val integerPart = parts[0]
    var decimalPart = if (parts.size > 1) parts[1] else "00"
    if (decimalPart.length == 1) decimalPart += "0"
    if (decimalPart.length > 2) decimalPart = decimalPart.substring(0, 2)
    return "$integerPart.$decimalPart"
}