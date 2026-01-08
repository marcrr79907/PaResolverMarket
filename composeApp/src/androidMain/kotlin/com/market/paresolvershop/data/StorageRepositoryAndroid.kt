package com.market.paresolvershop.data

import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepositoryAndroid : StorageRepository {

    private val storage = Firebase.storage

    override suspend fun uploadImage(bytes: ByteArray, baseNameHint: String): DataResult<String> {
        // Si no se proporciona ninguna imagen (array de bytes vacío),
        // devolver Success con una URL vacía sin intentar subir nada.
        if (bytes.isEmpty()) {
            return DataResult.Success("")
        }

        return try {
            val sanitizedHint = baseNameHint.trim().replace(" ", "_")
            val uniqueFileName = "${UUID.randomUUID()}_$sanitizedHint.jpg"

            // Definir la ruta en el storage
            val storageRef = storage.reference.child("product_images/$uniqueFileName")

            // Subir el archivo
            storageRef.putBytes(bytes).await()

            // Obtener la URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()

            DataResult.Success(downloadUrl.toString())

        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al subir la imagen")
        }
    }
}
