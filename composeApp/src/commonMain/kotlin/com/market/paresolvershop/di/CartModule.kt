package com.market.paresolvershop.di

import com.market.paresolvershop.ui.products.CartViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule = module {
    // El ViewModel es común y no necesita saber la implementación del repositorio.
    viewModelOf(::CartViewModel)
}
