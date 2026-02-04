package com.market.paresolvershop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.products.GetProducts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val products: List<Product>, val selectedCategoryId: String? = null) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class CatalogViewModel(
    private val getProducts: GetProducts
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CatalogUiState> = _selectedCategory
        .flatMapLatest { categoryId ->
            getProducts(categoryId)
                .map { products -> CatalogUiState.Success(products, categoryId) as CatalogUiState }
                .onStart { emit(CatalogUiState.Loading) }
                .catch { emit(CatalogUiState.Error(it.message ?: "Error desconocido")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CatalogUiState.Loading
        )

    fun selectCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }
}
