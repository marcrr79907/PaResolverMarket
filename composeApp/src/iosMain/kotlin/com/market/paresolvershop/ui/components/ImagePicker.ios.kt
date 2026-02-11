package com.market.paresolvershop.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun ImagePicker(
    show: Boolean,
    multiple: Boolean,
    onImagesSelected: (List<ByteArray>?) -> Unit
) {
}