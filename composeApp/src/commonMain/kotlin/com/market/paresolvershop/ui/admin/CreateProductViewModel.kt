package com.market.paresolvershop.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.ProductVariant
import com.market.paresolvershop.domain.products.CreateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado ampliado para e-commerce profesional
data class CreateProductFormState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val categoryId: String = "",
    val mainImageBytes: ByteArray? = null,
    val additionalImages: List<ByteArray> = emptyList(),
    val variants: List<ProductVariantFormState> = emptyList()
)

data class ProductVariantFormState(
    val name: String = "",
    val price: String = "",
    val stock: String = "",
    val sku: String = ""
)

sealed interface CreateProductScreenState {
    data object Idle : CreateProductScreenState
    data object Loading : CreateProductScreenState
    data object Success : CreateProductScreenState
    data class Error(val message: String) : CreateProductScreenState
}

class CreateProductViewModel(
    private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    var formState by mutableStateOf(CreateProductFormState())
        private set

    private val _screenState = MutableStateFlow<CreateProductScreenState>(CreateProductScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    fun onNameChange(name: String) { formState = formState.copy(name = name) }
    fun onDescriptionChange(description: String) { formState = formState.copy(description = description) }
    fun onPriceChange(price: String) { formState = formState.copy(price = price) }
    fun onStockChange(stock: String) { formState = formState.copy(stock = stock) }
    fun onCategoryChange(categoryId: String) { formState = formState.copy(categoryId = categoryId) }
    
    // Ajustado para tomar la primera imagen de la lista
    fun onMainImageSelected(images: List<ByteArray>?) { 
        formState = formState.copy(mainImageBytes = images?.firstOrNull()) 
    }
    
    // Ajustado para añadir múltiples imágenes de una vez
    fun addAdditionalImages(images: List<ByteArray>) {
        formState = formState.copy(additionalImages = formState.additionalImages + images)
    }

    fun removeAdditionalImage(index: Int) {
        val newList = formState.additionalImages.toMutableList().apply { removeAt(index) }
        formState = formState.copy(additionalImages = newList)
    }

    fun addVariant() {
        formState = formState.copy(variants = formState.variants + ProductVariantFormState())
    }

    fun updateVariant(index: Int, variant: ProductVariantFormState) {
        val newList = formState.variants.toMutableList().apply { set(index, variant) }
        formState = formState.copy(variants = newList)
    }

    fun removeVariant(index: Int) {
        val newList = formState.variants.toMutableList().apply { removeAt(index) }
        formState = formState.copy(variants = newList)
    }

    fun createProduct() {
        viewModelScope.launch {
            _screenState.value = CreateProductScreenState.Loading

            val priceDouble = formState.price.toDoubleOrNull() ?: 0.0
            val stockInt = formState.stock.toIntOrNull() ?: 0
            val mainImage = formState.mainImageBytes ?: byteArrayOf()

            val variants = formState.variants.map { 
                ProductVariant(
                    id = "",
                    productId = "",
                    name = it.name,
                    price = it.price.toDoubleOrNull(),
                    stock = it.stock.toIntOrNull() ?: 0,
                    sku = it.sku
                )
            }

            val result = createProductUseCase(
                name = formState.name,
                description = formState.description,
                price = priceDouble,
                stock = stockInt,
                categoryId = formState.categoryId,
                mainImageBytes = mainImage,
                additionalImages = formState.additionalImages,
                variants = variants
            )

            _screenState.value = when (result) {
                is DataResult.Success -> CreateProductScreenState.Success
                is DataResult.Error -> CreateProductScreenState.Error(result.message)
            }
        }
    }
    
    fun resetScreenState() {
        _screenState.value = CreateProductScreenState.Idle
    }
}
