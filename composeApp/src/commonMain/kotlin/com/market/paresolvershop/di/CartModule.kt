package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.implementations.CartRepositoryImpl
import com.market.paresolvershop.domain.cart.AddToCartUseCase
import com.market.paresolvershop.domain.cart.ClearCartUseCase
import com.market.paresolvershop.domain.cart.GetCartItemsUseCase
import com.market.paresolvershop.domain.cart.RemoveFromCartUseCase
import com.market.paresolvershop.domain.cart.UpdateCartQuantityUseCase
import com.market.paresolvershop.ui.cart.CartViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule = module {
    // Repository
    singleOf(::CartRepositoryImpl)

    // Use Cases
    factoryOf(::AddToCartUseCase)
    factoryOf(::ClearCartUseCase)
    factoryOf(::GetCartItemsUseCase)
    factoryOf(::RemoveFromCartUseCase)
    factoryOf(::UpdateCartQuantityUseCase)

    // ViewModel
    viewModelOf(::CartViewModel)
}
