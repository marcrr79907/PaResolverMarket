package com.market.paresolvershop.di

import com.market.paresolvershop.ui.checkout.CheckoutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val checkoutModule = module {
    viewModelOf(::CheckoutViewModel)
}
