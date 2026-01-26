package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.data.repository.implementations.ProductRepositoryImpl
import com.market.paresolvershop.data.repository.implementations.StorageRepositoryImpl
import com.market.paresolvershop.domain.products.CreateProductUseCase
import com.market.paresolvershop.domain.products.DeleteProductUseCase
import com.market.paresolvershop.domain.products.GetProductById
import com.market.paresolvershop.domain.products.GetProducts
import com.market.paresolvershop.domain.products.UpdateProductUseCase
import com.market.paresolvershop.ui.admin.CreateProductViewModel
import com.market.paresolvershop.ui.admin.EditProductViewModel
import com.market.paresolvershop.ui.admin.InventoryViewModel
import com.market.paresolvershop.ui.products.CatalogViewModel
import com.market.paresolvershop.ui.products.ProductDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val productModule = module {
    // Repositories
    singleOf(::ProductRepositoryImpl)
    singleOf(::StorageRepositoryImpl)

    // Use Cases
    factoryOf(::GetProductById)
    factoryOf(::GetProducts)
    factoryOf(::CreateProductUseCase)
    factoryOf(::UpdateProductUseCase)
    factoryOf(::DeleteProductUseCase)

    // ViewModels
    viewModelOf(::CatalogViewModel)
    viewModelOf(::ProductDetailViewModel)
    viewModelOf(::CreateProductViewModel)
    viewModelOf(::InventoryViewModel)
    viewModelOf(::EditProductViewModel)
}
