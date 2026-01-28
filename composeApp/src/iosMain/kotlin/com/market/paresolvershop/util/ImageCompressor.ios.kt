package com.market.paresolvershop.util

import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import kotlinx.cinterop.*
import platform.posix.memcpy

actual object ImageCompressor {
    @OptIn(ExperimentalForeignApi::class)
    actual fun compress(bytes: ByteArray, quality: Int, maxWidth: Int): ByteArray {
        val data = bytes.toNSData()
        val image = UIImage.imageWithData(data) ?: return bytes

        val cgImage = image.CGImage ?: return bytes
        val width = CGImageGetWidth(cgImage).toDouble()
        val height = CGImageGetHeight(cgImage).toDouble()

        // 1. Calcular rect para recorte 1x1
        val newSize = if (width > height) height else width
        val x = if (width > height) (width - height) / 2.0 else 0.0
        val y = if (height > width) (height - width) / 2.0 else 0.0

        val cropRect = CGRectMake(x, y, newSize, newSize)
        val croppedCgImage = CGImageCreateWithImageInRect(cgImage, cropRect) ?: return bytes

        // 2. Redimensionar y Comprimir
        val scaledImage = UIImage.imageWithCGImage(croppedCgImage)
        val compression = quality / 100.0
        val compressedData = UIImageJPEGRepresentation(scaledImage, compression)

        return compressedData?.toByteArray() ?: bytes
    }
}

// Extension utilitaria para convertir NSData a ByteArray
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned { memcpy(it.addressOf(0), bytes, length) }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.dataWithBytes(it.addressOf(0), size.toULong())
}