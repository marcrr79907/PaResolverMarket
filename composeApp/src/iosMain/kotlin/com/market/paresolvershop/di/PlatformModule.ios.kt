package com.market.paresolvershop.di

import com.market.paresolvershop.data.AuthRepositoryIos
import com.market.paresolvershop.data.ProductRepositoryIos
import com.market.paresolvershop.data.StorageRepositoryIos
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::AuthRepositoryIos) { bind<AuthRepository>() }

    singleOf(::ProductRepositoryIos) { bind<ProductRepository>() }

    singleOf(::StorageRepositoryIos) { bind<StorageRepository>() }
}