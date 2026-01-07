package com.market.paresolvershop.di

import com.market.paresolvershop.domain.auth.ObserveAuthState
import com.market.paresolvershop.domain.auth.SignInWithEmail
import com.market.paresolvershop.domain.auth.SignInWithGoogle
import com.market.paresolvershop.domain.auth.SignOut
import com.market.paresolvershop.domain.auth.SignUpWithEmail
import com.market.paresolvershop.ui.authentication.LoginViewModel
import com.market.paresolver.ui.authentication.RegisterViewModel
import com.market.paresolver.ui.profilemagnament.ProfileViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {

    factoryOf(::SignInWithEmail)
    factoryOf(::SignInWithGoogle)
    factoryOf(::SignUpWithEmail)
    factoryOf(::SignOut)

    factoryOf(::ObserveAuthState)

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ProfileViewModel)
}