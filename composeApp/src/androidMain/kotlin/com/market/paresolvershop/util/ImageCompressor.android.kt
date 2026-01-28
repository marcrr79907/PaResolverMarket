// /home/marc/Programation/Projects/PaResolverMarket/composeApp/src/androidMain/kotlin/com/market/paresolvershop/util/ImageCompressor.android.kt
package com.market.paresolvershop.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

actual object ImageCompressor {
    actual fun compress(bytes: ByteArray, quality: Int, maxWidth: Int): ByteArray {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = false }
        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return bytes

        // 1. Recortar a formato 1x1 (Cuadrado centrado)
        val width = originalBitmap.width
        val height = originalBitmap.height
        val newSize = if (width > height) height else width
        val xOffset = if (width > height) (width - height) / 2 else 0
        val yOffset = if (height > width) (height - width) / 2 else 0

        val croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, newSize, newSize)

        // 2. Redimensionar si es mayor al maxWidth
        val finalBitmap = if (newSize > maxWidth) {
            croppedBitmap.scale(maxWidth, maxWidth)
        } else {
            croppedBitmap
        }

        // 3. Comprimir (JPG para mejor peso/calidad)
        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        val result = outputStream.toByteArray()

        // Liberar memoria
        if (originalBitmap != finalBitmap) originalBitmap.recycle()
        if (croppedBitmap != finalBitmap && croppedBitmap != originalBitmap) croppedBitmap.recycle()

        return result
    }
}