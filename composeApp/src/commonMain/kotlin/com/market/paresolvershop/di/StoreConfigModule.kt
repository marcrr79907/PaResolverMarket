package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.StoreConfigRepository
import com.market.paresolvershop.data.repository.implementations.StoreConfigRepositoryImpl
import com.market.paresolvershop.domain.store.GetStoreConfigUseCase
import com.market.paresolvershop.domain.store.UpdateStoreConfigUseCase
import com.market.paresolvershop.ui.admin.StoreManagementViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val storeConfigModule = module {
    // Repository: Inyectamos la implementación y la vinculamos a la interfaz
    singleOf(::StoreConfigRepositoryImpl) { bind<StoreConfigRepository>() }

    // Use Cases: Se registran como factory para que se cree una instancia nueva cada vez
    factoryOf(::GetStoreConfigUseCase)
    factoryOf(::UpdateStoreConfigUseCase)

    // ViewModels: Koin se encarga de inyectar los casos de uso automáticamente
    viewModelOf(::StoreManagementViewModel)
}
