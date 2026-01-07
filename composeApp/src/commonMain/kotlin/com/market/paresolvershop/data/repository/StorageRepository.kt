package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.DataResult

/**
 * Repositorio para manejar operaciones de almacenamiento de archivos, como la subida de imágenes.
 */
interface StorageRepository {
    /**
     * Sube una imagen a Firebase Storage.
     *
     * @param bytes Los datos de la imagen en formato ByteArray.
     * @param baseNameHint Un nombre base sugerido para el archivo (ej: el nombre del producto).
     * @return Un [DataResult] que contiene la URL de descarga de la imagen si tiene éxito,
     * o un error si falla.
     */
    suspend fun uploadImage(bytes: ByteArray, baseNameHint: String): DataResult<String>
}
