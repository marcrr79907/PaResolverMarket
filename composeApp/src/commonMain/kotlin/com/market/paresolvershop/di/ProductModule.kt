package com.market.paresolvershop.di

import com.market.paresolvershop.domain.products.CreateProductUseCase
import com.market.paresolvershop.domain.products.GetProductById
import com.market.paresolvershop.domain.products.GetProducts
import com.market.paresolvershop.ui.admin.CreateProductViewModel
import com.market.paresolvershop.ui.products.CatalogViewModel
import com.market.paresolvershop.ui.products.ProductDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val productModule = module {

    // Casos de Uso
    factoryOf(::GetProductById)
    factoryOf(::GetProducts)
    factoryOf(::CreateProductUseCase)

    // ViewModel
    viewModelOf(::CatalogViewModel)
    viewModelOf(::ProductDetailViewModel)
    viewModelOf(::CreateProductViewModel)
}
