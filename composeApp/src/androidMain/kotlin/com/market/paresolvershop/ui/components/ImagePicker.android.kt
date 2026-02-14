package com.market.paresolvershop.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ImagePicker(
    show: Boolean,
    multiple: Boolean,
    onImagesSelected: (List<ByteArray>?) -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = if (multiple) ActivityResultContracts.PickMultipleVisualMedia()
        else ActivityResultContracts.PickVisualMedia()
    ) { uris ->
        // Convertir el URI único a lista si no es múltiple
        val uriList = when (uris) {
            is List<*> -> uris as List<Uri>
            is Uri -> listOf(uris)
            else -> emptyList()
        }

        val bytesList = uriList.mapNotNull { uri ->
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
        onImagesSelected(bytesList)
    }

    LaunchedEffect(show) {
        if (show) {
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            launcher.launch(request)
        }
    }
}
