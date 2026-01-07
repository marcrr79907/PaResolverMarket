package com.market.paresolvershop.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ImagePicker(
    show: Boolean,
    onImageSelected: (ByteArray?) -> Unit
) {
    val context = LocalContext.current

    // 1. Crear el lanzador para la actividad de selección de contenido
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri == null) {
                onImageSelected(null)
                return@rememberLauncherForActivityResult
            }
            // Leer los bytes de la imagen seleccionada
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            onImageSelected(bytes)
        }
    )

    // 2. Lanzar el selector de imágenes cuando `show` sea verdadero
    LaunchedEffect(show) {
        if (show) {
            launcher.launch("image/*") // Especificamos que queremos seleccionar imágenes
        }
    }
}
