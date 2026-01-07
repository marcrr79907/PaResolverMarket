package com.market.paresolvershop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.Product
import com.market.paresolvershop.domain.products.GetProducts
import kotlinx.coroutines.flow.*

// Estado para la UI del catálogo
sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val products: List<Product>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class CatalogViewModel(
    getProducts: GetProducts
) : ViewModel() {

    // El StateFlow que expondrá el estado a la UI.
    val uiState: StateFlow<CatalogUiState> = getProducts()
        .map<List<Product>, CatalogUiState>(CatalogUiState::Success) // Si hay éxito, envuelve en Success
        .onStart { emit(CatalogUiState.Loading) } // Al empezar, emite Loading
        .catch { emit(CatalogUiState.Error(it.message ?: "Error desconocido")) } // Si hay error, emite Error
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CatalogUiState.Loading // Valor inicial mientras se conecta
        )
}
