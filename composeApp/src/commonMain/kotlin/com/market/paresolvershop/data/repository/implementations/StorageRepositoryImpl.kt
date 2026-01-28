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

    private val bucketName = "product_images"

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

            val publicUrl = supabase.storage[bucketName].publicUrl(uniqueFileName)
            DataResult.Success(publicUrl)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al subir la imagen")
        }
    }

    override suspend fun deleteImage(imageUrl: String): DataResult<Unit> = runCatching {
        // Extraer el nombre del archivo de la URL pública de Supabase
        // Ejem: .../storage/v1/object/public/product_images/nombre_archivo.jpg
        val fileName = imageUrl.substringAfterLast("/")
        if (fileName.isNotEmpty() && imageUrl.contains(bucketName)) {
            supabase.storage[bucketName].delete(listOf(fileName))
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al eliminar la imagen antigua")
    }
}
