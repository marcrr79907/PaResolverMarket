package com.market.paresolvershop.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.products.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Reusing the same form state

// Estado para la pantalla en general
sealed interface EditProductScreenState {
    data object Idle : EditProductScreenState
    data object Loading : EditProductScreenState
    data object Success : EditProductScreenState
    data class Error(val message: String) : EditProductScreenState
}

class EditProductViewModel(
    private val productToEdit: Product,
    private val updateProductUseCase: UpdateProductUseCase
) : ViewModel() {

    var formState by mutableStateOf(CreateProductFormState())
        private set

    private val _screenState = MutableStateFlow<EditProductScreenState>(EditProductScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    init {
        // Initialize form with the product to edit
        formState = formState.copy(
            name = productToEdit.name,
            description = productToEdit.description,
            price = productToEdit.price.toString(),
            stock = productToEdit.stock.toString(),
            category = productToEdit.category,
            // Image is handled separately, starting with the existing URL
        )
    }

    fun onNameChange(name: String) {
        formState = formState.copy(name = name)
    }

    fun onDescriptionChange(description: String) {
        formState = formState.copy(description = description)
    }

    fun onPriceChange(price: String) {
        formState = formState.copy(price = price)
    }

    fun onStockChange(stock: String) {
        formState = formState.copy(stock = stock)
    }

    fun onCategoryChange(category: String) {
        formState = formState.copy(category = category)
    }

    fun onImageSelected(bytes: ByteArray?) {
        formState = formState.copy(imageBytes = bytes)
    }

    fun updateProduct() {
        viewModelScope.launch {
            _screenState.value = EditProductScreenState.Loading

            val priceDouble = formState.price.toDoubleOrNull() ?: 0.0
            val stockInt = formState.stock.toIntOrNull() ?: 0

            val updatedProduct = productToEdit.copy(
                name = formState.name,
                description = formState.description,
                price = priceDouble,
                stock = stockInt,
                category = formState.category
            )

            val result = updateProductUseCase(
                product = updatedProduct,
                newImageBytes = formState.imageBytes
            )

            _screenState.value = when (result) {
                is DataResult.Success -> EditProductScreenState.Success
                is DataResult.Error -> EditProductScreenState.Error(result.message)
            }
        }
    }
    
    fun resetScreenState() {
        _screenState.value = EditProductScreenState.Idle
    }
}
