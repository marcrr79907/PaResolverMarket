package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.implementations.CartRepositoryImpl
import com.market.paresolvershop.ui.cart.CartViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule = module {
    // Repository
    singleOf(::CartRepositoryImpl)

    // ViewModel
    viewModelOf(::CartViewModel)
}
