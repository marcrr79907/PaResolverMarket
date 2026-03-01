package com.market.paresolvershop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.orders.GetAllOrdersAdminUseCase
import com.market.paresolvershop.domain.orders.UpdateOrderStatusUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface OrderManagementUiState {
    data object Loading : OrderManagementUiState
    data class Success(
        val orders: List<Order>,
        val filteredOrders: List<Order>,
        val selectedStatus: String?
    ) : OrderManagementUiState
    data class Error(val message: String) : OrderManagementUiState
}

sealed interface OrderManagementEvent {
    data class Success(val message: String) : OrderManagementEvent
    data class Error(val message: String) : OrderManagementEvent
}

class OrderManagementViewModel(
    private val getAllOrdersAdminUseCase: GetAllOrdersAdminUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _selectedStatus = MutableStateFlow<String?>(null) // null significa "Todas"
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OrderManagementUiState> = combine(
        _allOrders, _selectedStatus, _searchQuery, _isLoading, _errorMessage
    ) { orders, status, query, loading, error ->
        if (error != null) return@combine OrderManagementUiState.Error(error)
        if (loading) return@combine OrderManagementUiState.Loading

        val filtered = orders.filter { order ->
            val matchesStatus = status == null || order.status == status
            val matchesQuery = query.isEmpty() || 
                              order.id?.contains(query, ignoreCase = true) == true ||
                              order.customerName?.contains(query, ignoreCase = true) == true
            matchesStatus && matchesQuery
        }.sortedByDescending { it.createdAt }

        OrderManagementUiState.Success(
            orders = orders,
            filteredOrders = filtered,
            selectedStatus = status
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderManagementUiState.Loading)

    private val _events = MutableSharedFlow<OrderManagementEvent>()
    val events = _events.asSharedFlow()

    init {
        fetchAllOrders()
    }

    fun fetchAllOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = getAllOrdersAdminUseCase()) {
                is DataResult.Success -> {
                    _allOrders.value = result.data
                    _isLoading.value = false
                }
                is DataResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }
            }
        }
    }

    fun filterByStatus(status: String?) {
        _selectedStatus.value = status
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            when (val result = updateOrderStatusUseCase(orderId, newStatus)) {
                is DataResult.Success -> {
                    _events.emit(OrderManagementEvent.Success("Pedido actualizado correctamente"))
                    // Actualizamos localmente para evitar recargar toda la red
                    _allOrders.value = _allOrders.value.map {
                        if (it.id == orderId) it.copy(status = newStatus) else it
                    }
                }
                is DataResult.Error -> {
                    _events.emit(OrderManagementEvent.Error(result.message))
                }
            }
        }
    }
}
