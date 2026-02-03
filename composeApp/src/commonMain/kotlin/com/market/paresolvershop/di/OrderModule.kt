package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.data.repository.implementations.OrderRepositoryImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val orderModule = module {
    singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }
}
