package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.data.repository.implementations.AuthRepositoryImpl
import com.market.paresolvershop.data.repository.implementations.CartRepositoryImpl
import com.market.paresolvershop.data.repository.implementations.ProductRepositoryImpl
import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.data.repository.implementations.StorageRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Dependencia de Autenticación
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

    // Dependencia de Productos
    singleOf(::ProductRepositoryImpl) { bind<ProductRepository>() }

    // Dependencia de Storage
    singleOf(::StorageRepositoryImpl) { bind<StorageRepository>() }

    // Dependencia de Carrito
    singleOf(::CartRepositoryImpl) { bind<CartRepository>() }
}