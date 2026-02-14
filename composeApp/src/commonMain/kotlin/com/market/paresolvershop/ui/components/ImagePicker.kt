package com.market.paresolvershop.ui.components

import androidx.compose.runtime.Composable

/**
 * Un componente multiplataforma para seleccionar una imagen de la galería del dispositivo.
 *
 * @param show Controla si el selector de imágenes debe mostrarse o no.
 * @param onImageSelected Una función callback que se invoca cuando una imagen es seleccionada.
 *                        Devuelve los bytes (ByteArray) de la imagen o null si la selección se cancela.
 */
@Composable
expect fun ImagePicker(
    show: Boolean,
    multiple: Boolean = false,
    onImagesSelected: (List<ByteArray>?) -> Unit
)
