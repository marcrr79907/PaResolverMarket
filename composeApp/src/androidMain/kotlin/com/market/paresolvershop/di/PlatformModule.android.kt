package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.AuthRepositoryAndroid
import com.market.paresolvershop.data.ProductRepositoryAndroid
import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.data.StorageRepositoryAndroid
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Dependencia de Autenticación
    singleOf(::AuthRepositoryAndroid) { bind<AuthRepository>() }

    // Dependencia de Productos
    singleOf(::ProductRepositoryAndroid) { bind<ProductRepository>() }

    // Dependencia de Storage
    singleOf(::StorageRepositoryAndroid) { bind<StorageRepository>() }
}