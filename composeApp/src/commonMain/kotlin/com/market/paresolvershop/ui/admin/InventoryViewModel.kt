package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.products.DeleteProductUseCase
import com.market.paresolvershop.domain.products.GetProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed interface InventoryUiState {
    data object Loading : InventoryUiState
    data class Success(val products: List<Product>) : InventoryUiState
    data class Error(val message: String) : InventoryUiState
}

sealed interface DeleteProductState {
    data object Idle : DeleteProductState
    data object Loading : DeleteProductState
    data object Success : DeleteProductState
    data class Error(val message: String) : DeleteProductState
}

class InventoryViewModel(
    private val getProducts: GetProducts,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteProductState>(DeleteProductState.Idle)
    val deleteState: StateFlow<DeleteProductState> = _deleteState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        getProducts()
            .onStart { _uiState.value = InventoryUiState.Loading }
            .onEach { products ->
                _uiState.value = InventoryUiState.Success(products)
            }
            .catch { error ->
                _uiState.value = InventoryUiState.Error(error.message ?: "Error al cargar productos")
            }
            .launchIn(viewModelScope)
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            _deleteState.value = DeleteProductState.Loading
            when (val result = deleteProductUseCase(product)) {
                is DataResult.Success -> {
                    _deleteState.value = DeleteProductState.Success
                    // No es necesario recargar la lista, el Flow la actualizará automáticamente.
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
