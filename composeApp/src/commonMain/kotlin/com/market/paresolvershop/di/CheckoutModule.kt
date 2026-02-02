package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.data.repository.implementations.AddressRepositoryImpl
import com.market.paresolvershop.ui.checkout.CheckoutViewModel
import com.market.paresolvershop.ui.profilemagnament.AddressViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val checkoutModule = module {
    singleOf(::AddressRepositoryImpl) { bind<AddressRepository>() }

    // ViewModels
    viewModelOf(::CheckoutViewModel)
    viewModelOf(::AddressViewModel)
}
