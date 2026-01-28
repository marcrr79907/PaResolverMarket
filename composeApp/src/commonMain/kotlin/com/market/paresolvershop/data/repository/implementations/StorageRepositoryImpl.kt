package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.util.ImageCompressor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.time.Clock
class StorageRepositoryImpl(
    private val supabase: SupabaseClient
) : StorageRepository {

    private val bucketName = "product_images" // Nombre del bucket en Supabase Storage

    override suspend fun uploadImage(bytes: ByteArray, baseNameHint: String): DataResult<String> {
        if (bytes.isEmpty()) return DataResult.Success("")

        return try {
            val optimizedBytes = ImageCompressor.compress(bytes, quality = 50, maxWidth = 800)
            val sanitizedHint = baseNameHint.trim()
                .lowercase()
                .replace(Regex("[áàäâ]"), "a")
                .replace(Regex("[éèëê]"), "e")
                .replace(Regex("[íìïî]"), "i")
                .replace(Regex("[óòöô]"), "o")
                .replace(Regex("[úùüû]"), "u")
                .replace("ñ", "n")
                .replace(Regex("[^a-z0-9]"), "_")

            val timestamp = Clock.System.now().toEpochMilliseconds()
            val uniqueFileName = "${timestamp}_$sanitizedHint.jpg"

            // Subir el archivo al bucket de Supabase Storage
            supabase.storage[bucketName].upload(
                path = uniqueFileName,
                data = optimizedBytes
            )

            // Obtener la URL pública
            val publicUrl = supabase.storage[bucketName].publicUrl(uniqueFileName)

            DataResult.Success(publicUrl)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al subir la imagen")
        }
    }
}
