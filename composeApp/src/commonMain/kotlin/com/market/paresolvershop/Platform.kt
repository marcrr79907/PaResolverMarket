package com.market.paresolvershop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform