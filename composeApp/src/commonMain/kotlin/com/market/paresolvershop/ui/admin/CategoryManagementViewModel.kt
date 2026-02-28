package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.categories.CreateCategoryUseCase
import com.market.paresolvershop.domain.categories.DeleteCategoryUseCase
import com.market.paresolvershop.domain.categories.GetCategoriesUseCase
import com.market.paresolvershop.domain.categories.UpdateCategoryUseCase
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CategoryUiState {
    data object Loading : CategoryUiState
    data class Success(val categories: List<Category>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

class CategoryManagementViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    val uiState: StateFlow<CategoryUiState> = getCategoriesUseCase()
        .map { categories -> 
            val state: CategoryUiState = CategoryUiState.Success(categories)
            state 
        }
        .onStart { emit(CategoryUiState.Loading) }
        .catch { emit(CategoryUiState.Error(it.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryUiState.Loading
        )

    private val _actionState = MutableStateFlow<DataResult<Unit>?>(null)
    val actionState = _actionState.asStateFlow()

    fun createCategory(name: String) {
        viewModelScope.launch {
            _actionState.value = null
            val result = createCategoryUseCase(name)
            _actionState.value = result
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _actionState.value = null
            val result = updateCategoryUseCase(category)
            _actionState.value = result
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _actionState.value = null
            val result = deleteCategoryUseCase(id)
            _actionState.value = result
        }
    }
    
    fun resetActionState() {
        _actionState.value = null
    }
}
