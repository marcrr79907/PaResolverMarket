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
        val selectedStatus: String?,
        val totalAmountInView: Double
    ) : OrderManagementUiState
    data class Error(val message: String) : OrderManagementUiState
}

sealed interface OrderManagementEvent {
    data class Success(val message: String) : OrderManagementEvent
    data class Error(val message: String) : OrderManagementEvent
}

enum class OrderSortType {
    DATE, AMOUNT, CUSTOMER
}

class OrderManagementViewModel(
    private val getAllOrdersAdminUseCase: GetAllOrdersAdminUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _selectedStatus = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    private val _sortType = MutableStateFlow(OrderSortType.DATE)
    val sortType = _sortType.asStateFlow()
    
    private val _isAscending = MutableStateFlow(false)
    val isAscending = _isAscending.asStateFlow()

    private val _startDate = MutableStateFlow<String?>(null) // YYYY-MM-DD
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<String?>(null) // YYYY-MM-DD
    val endDate = _endDate.asStateFlow()

    val uiState: StateFlow<OrderManagementUiState> = combine(
        _allOrders, _selectedStatus, _searchQuery, _isLoading, _errorMessage, _sortType, _isAscending, _startDate, _endDate
    ) { array ->
        val orders = array[0] as List<Order>
        val status = array[1] as String?
        val query = array[2] as String
        val loading = array[3] as Boolean
        val error = array[4] as String?
        val sort = array[5] as OrderSortType
        val asc = array[6] as Boolean
        val start = array[7] as String?
        val end = array[8] as String?

        if (error != null) return@combine OrderManagementUiState.Error(error)
        if (loading) return@combine OrderManagementUiState.Loading

        val filtered = orders.filter { order ->
            val matchesStatus = status == null || order.status == status
            val matchesQuery = query.isEmpty() || 
                              order.id?.contains(query, ignoreCase = true) == true ||
                              order.customerName?.contains(query, ignoreCase = true) == true
            
            val orderDate = order.createdAt?.take(10) // Get YYYY-MM-DD
            val matchesDate = when {
                start != null && end != null -> orderDate != null && orderDate >= start && orderDate <= end
                start != null -> orderDate != null && orderDate >= start
                else -> true
            }
            
            matchesStatus && matchesQuery && matchesDate
        }

        val sorted = when (sort) {
            OrderSortType.DATE -> if (asc) filtered.sortedBy { it.createdAt } else filtered.sortedByDescending { it.createdAt }
            OrderSortType.AMOUNT -> if (asc) filtered.sortedBy { it.totalAmount } else filtered.sortedByDescending { it.totalAmount }
            OrderSortType.CUSTOMER -> if (asc) filtered.sortedBy { it.customerName } else filtered.sortedByDescending { it.customerName }
        }

        OrderManagementUiState.Success(
            orders = orders,
            filteredOrders = sorted,
            selectedStatus = status,
            totalAmountInView = sorted.sumOf { it.totalAmount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderManagementUiState.Loading)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun toggleSort(type: OrderSortType) {
        if (_sortType.value == type) {
            _isAscending.value = !_isAscending.value
        } else {
            _sortType.value = type
            _isAscending.value = type != OrderSortType.DATE
        }
    }

    fun setDateRange(start: String?, end: String?) {
        _startDate.value = start
        _endDate.value = end
    }

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            when (val result = updateOrderStatusUseCase(orderId, newStatus)) {
                is DataResult.Success -> {
                    _events.emit(OrderManagementEvent.Success("Pedido actualizado correctamente"))
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
