package com.market.paresolvershop.di

import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.data.repository.implementations.CategoryRepositoryImpl
import com.market.paresolvershop.data.repository.implementations.ProductRepositoryImpl
import com.market.paresolvershop.data.repository.implementations.StorageRepositoryImpl
import com.market.paresolvershop.domain.categories.CreateCategoryUseCase
import com.market.paresolvershop.domain.categories.DeleteCategoryUseCase
import com.market.paresolvershop.domain.categories.GetCategoriesUseCase
import com.market.paresolvershop.domain.categories.UpdateCategoryUseCase
import com.market.paresolvershop.domain.products.CreateProductUseCase
import com.market.paresolvershop.domain.products.DeleteProductUseCase
import com.market.paresolvershop.domain.products.GetProductById
import com.market.paresolvershop.domain.products.GetProducts
import com.market.paresolvershop.domain.products.UpdateProductUseCase
import com.market.paresolvershop.ui.admin.CategoryManagementViewModel
import com.market.paresolvershop.ui.admin.CreateProductViewModel
import com.market.paresolvershop.ui.admin.EditProductViewModel
import com.market.paresolvershop.ui.admin.InventoryViewModel
import com.market.paresolvershop.ui.products.CatalogViewModel
import com.market.paresolvershop.ui.products.ProductDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val productModule = module {
    // Repositories
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::StorageRepositoryImpl) bind StorageRepository::class

    // Use Cases - Products
    factoryOf(::GetProductById)
    factoryOf(::GetProducts)
    factoryOf(::CreateProductUseCase)
    factoryOf(::UpdateProductUseCase)
    factoryOf(::DeleteProductUseCase)

    // Use Cases - Categories
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::CreateCategoryUseCase)
    factoryOf(::UpdateCategoryUseCase)
    factoryOf(::DeleteCategoryUseCase)

    // ViewModels
    viewModelOf(::CatalogViewModel)
    viewModelOf(::ProductDetailViewModel)
    viewModelOf(::CreateProductViewModel)
    viewModelOf(::InventoryViewModel)
    viewModelOf(::EditProductViewModel)
    viewModelOf(::CategoryManagementViewModel)
}
