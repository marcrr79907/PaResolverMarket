package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.categories.GetCategoriesUseCase
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.products.DeleteProductUseCase
import com.market.paresolvershop.domain.products.GetProducts
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface InventoryUiState {
    data object Loading : InventoryUiState
    data class Success(
        val products: List<Product>,
        val categories: List<Category>,
        val totalProducts: Int,
        val lowStockCount: Int
    ) : InventoryUiState

    data class Error(val message: String) : InventoryUiState
}

sealed interface DeleteProductState {
    data object Idle : DeleteProductState
    data object Loading : DeleteProductState
    data object Success : DeleteProductState
    data class Error(val message: String) : DeleteProductState
}

enum class InventorySortType {
    NAME, PRICE, STOCK
}

class InventoryViewModel(
    private val getProducts: GetProducts,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _deleteState = MutableStateFlow<DeleteProductState>(DeleteProductState.Idle)
    val deleteState: StateFlow<DeleteProductState> = _deleteState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortType = MutableStateFlow(InventorySortType.NAME)
    val sortType = _sortType.asStateFlow()

    private val _isAscending = MutableStateFlow(true)
    val isAscending = _isAscending.asStateFlow()

    val uiState: StateFlow<InventoryUiState> = combine(
        getProducts(),
        getCategoriesUseCase(),
        combine(_selectedCategoryId, _searchQuery) { id, q -> id to q },
        combine(_sortType, _isAscending) { t, a -> t to a }
    ) { products, categories, filters, sort ->
        val (selectedId, query) = filters
        val (type, asc) = sort

        // 1. Filtrar categorías activas
        val activeCategoryIds = products.map { it.categoryId }.toSet()
        val filteredCategories = categories.filter { it.id in activeCategoryIds }

        // 2. Filtrado de productos (Nombre y categoría)
        val filtered = products.filter { product ->
            val matchesCategory = selectedId == null || product.categoryId == selectedId
            val matchesQuery = query.isEmpty() || product.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        // 3. Ordenamiento alternante
        val sorted = when (type) {
            InventorySortType.NAME -> if (asc) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
            InventorySortType.PRICE -> if (asc) filtered.sortedBy { it.price } else filtered.sortedByDescending { it.price }
            InventorySortType.STOCK -> if (asc) filtered.sortedBy { it.stock } else filtered.sortedByDescending { it.stock }
        }

        val state: InventoryUiState = InventoryUiState.Success(
            products = sorted,
            categories = filteredCategories,
            totalProducts = products.size,
            lowStockCount = products.count { it.stock < 5 }
        )
        state
    }.onStart { emit(InventoryUiState.Loading) }
        .catch { emit(InventoryUiState.Error(it.message ?: "Error al cargar inventario")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState.Loading)


    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSort(type: InventorySortType) {
        if (_sortType.value == type) {
            _isAscending.value = !_isAscending.value
        } else {
            _sortType.value = type
            _isAscending.value = true
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            _deleteState.value = DeleteProductState.Loading
            when (val result = deleteProductUseCase(product)) {
                is DataResult.Success -> {
                    _deleteState.value = DeleteProductState.Success
                }

                is DataResult.Error -> {
                    _deleteState.value = DeleteProductState.Error(result.message)
                }
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteProductState.Idle
    }
}
