package com.market.paresolvershop.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.products.CreateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado para los campos del formulario
data class CreateProductFormState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val category: String = "",
    val imageBytes: ByteArray? = null
)

// Estado para la pantalla en general
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

    fun createProduct() {
        viewModelScope.launch {
            _screenState.value = CreateProductScreenState.Loading

            val priceDouble = formState.price.toDoubleOrNull() ?: 0.0
            val stockInt = formState.stock.toIntOrNull() ?: 0

            if (formState.imageBytes == null) {
                _screenState.value = CreateProductScreenState.Error("Debes seleccionar una imagen.")
                return@launch
            }

            val result = createProductUseCase(
                name = formState.name,
                description = formState.description,
                price = priceDouble,
                stock = stockInt,
                category = formState.category,
                imageBytes = formState.imageBytes!!
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
