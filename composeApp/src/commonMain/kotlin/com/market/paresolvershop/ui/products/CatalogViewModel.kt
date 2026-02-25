package com.market.paresolvershop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.model.StoreConfig
import com.market.paresolvershop.domain.products.GetProducts
import com.market.paresolvershop.domain.store.GetStoreConfigUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(
        val products: List<Product>,
        val categories: List<Category>,
        val config: StoreConfig? = null,
        val selectedCategoryId: String? = null
    ) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class CatalogViewModel(
    private val getProducts: GetProducts,
    private val categoryRepository: CategoryRepository,
    private val getStoreConfigUseCase: GetStoreConfigUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CatalogUiState> = combine(
        _selectedCategory,
        categoryRepository.getActiveCategories(),
        getStoreConfigUseCase.storeConfig
    ) { categoryId, activeCategories, config ->
        Triple(categoryId, activeCategories, config)
    }.flatMapLatest { (categoryId, activeCategories, config) ->
        getProducts(categoryId)
            .map { products -> 
                CatalogUiState.Success(
                    products = products,
                    categories = activeCategories,
                    config = config,
                    selectedCategoryId = categoryId
                ) as CatalogUiState 
            }
            .onStart { emit(CatalogUiState.Loading) }
            .catch { emit(CatalogUiState.Error(it.message ?: "Error desconocido")) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState.Loading
    )

    init {
        viewModelScope.launch {
            getStoreConfigUseCase()
        }
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }
}
