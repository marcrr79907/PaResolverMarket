package com.market.paresolvershop.util

expect object ImageCompressor {
    fun compress(bytes: ByteArray, quality: Int = 80, maxWidth: Int = 1024): ByteArray
}