package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.time.Clock
class StorageRepositoryImpl(
    private val supabase: SupabaseClient
) : StorageRepository {

    private val bucketName = "product_images" // Nombre del bucket en Supabase Storage

    override suspend fun uploadImage(bytes: ByteArray, baseNameHint: String): DataResult<String> {
        if (bytes.isEmpty()) {
            return DataResult.Success("")
        }

        return try {
            val sanitizedHint = baseNameHint.trim().replace(" ", "_")
            val uniqueFileName = "${Clock.System}_$sanitizedHint.jpg"

            // Subir el archivo al bucket de Supabase Storage
            supabase.storage[bucketName].upload(
                path = uniqueFileName,
                data = bytes
            )

            // Obtener la URL pública
            val publicUrl = supabase.storage[bucketName].publicUrl(uniqueFileName)

            DataResult.Success(publicUrl)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al subir la imagen")
        }
    }
}
