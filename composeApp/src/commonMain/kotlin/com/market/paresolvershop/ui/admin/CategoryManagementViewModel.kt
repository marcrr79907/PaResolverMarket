package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.data.repository.CategoryRepository
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
    private val repository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<CategoryUiState> = repository.getCategories()
        .map<List<Category>, CategoryUiState> { CategoryUiState.Success(it) }
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
        if (name.isBlank()) return
        viewModelScope.launch {
            _actionState.value = null
            val result = repository.createCategory(Category(name = name))
            _actionState.value = result
        }
    }

    fun updateCategory(category: Category) {
        if (category.name.isBlank()) return
        viewModelScope.launch {
            _actionState.value = null
            val result = repository.updateCategory(category)
            _actionState.value = result
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _actionState.value = null
            val result = repository.deleteCategory(id)
            _actionState.value = result
        }
    }
    
    fun resetActionState() {
        _actionState.value = null
    }
}
