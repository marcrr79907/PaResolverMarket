package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.data.repository.implementations.AddressRepositoryImpl
import com.market.paresolvershop.domain.address.*
import com.market.paresolvershop.ui.profile.AddressViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val addressModule = module {
    // Repository
    singleOf(::AddressRepositoryImpl) { bind<AddressRepository>() }

    // Use Cases
    factoryOf(::GetAddressesUseCase)
    factoryOf(::SaveAddressUseCase)
    factoryOf(::DeleteAddressUseCase)
    factoryOf(::SetDefaultAddressUseCase)

    // ViewModels
    viewModelOf(::AddressViewModel)
}
