package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.data.repository.implementations.OrderRepositoryImpl
import com.market.paresolvershop.ui.admin.OrderManagementViewModel
import com.market.paresolvershop.ui.orders.OrderDetailViewModel
import com.market.paresolvershop.ui.orders.OrderHistoryViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val orderModule = module {
    singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }

    viewModelOf(::OrderHistoryViewModel)
    viewModelOf(::OrderDetailViewModel)
    viewModelOf(::OrderManagementViewModel)
}
