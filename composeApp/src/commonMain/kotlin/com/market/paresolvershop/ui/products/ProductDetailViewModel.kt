package com.market.paresolvershop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.products.GetProductById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 2. Define un estado de UI específico para esta pantalla
sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Success(val product: Product) : ProductDetailUiState // Contiene UN solo producto
    data class Error(val message: String) : ProductDetailUiState
}

class ProductDetailViewModel(
    private val productId: String,
    private val getProductById: GetProductById
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading

            when(val result = getProductById(productId)) {
                is DataResult.Success -> {
                    _uiState.value = ProductDetailUiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = ProductDetailUiState.Error(result.message)
                }
            }

        }
    }
}
