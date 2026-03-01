package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.data.repository.implementations.OrderRepositoryImpl
import com.market.paresolvershop.domain.orders.GetAllOrdersAdminUseCase
import com.market.paresolvershop.domain.orders.GetOrderDetailsUseCase
import com.market.paresolvershop.domain.orders.GetOrderHistoryUseCase
import com.market.paresolvershop.domain.orders.GetOrderItemsUseCase
import com.market.paresolvershop.domain.orders.PlaceOrderUseCase
import com.market.paresolvershop.domain.orders.UpdateOrderStatusUseCase
import com.market.paresolvershop.ui.admin.OrderManagementViewModel
import com.market.paresolvershop.ui.orders.OrderDetailViewModel
import com.market.paresolvershop.ui.orders.OrderHistoryViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val orderModule = module {
    // Repository
    singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }

    // Uses Case
    factoryOf(::GetOrderDetailsUseCase)
    factoryOf(::GetOrderItemsUseCase)
    factoryOf(::GetOrderHistoryUseCase)
    factoryOf(::GetAllOrdersAdminUseCase)
    factoryOf(::PlaceOrderUseCase)
    factoryOf(::UpdateOrderStatusUseCase)

    // ViewModels
    viewModelOf(::OrderHistoryViewModel)
    viewModelOf(::OrderDetailViewModel)
    viewModelOf(::OrderManagementViewModel)
}
