package com.market.paresolvershop.di

import com.market.paresolvershop.config.SupabaseConfig
import com.market.paresolvershop.data.api.PaymentApiService
import com.market.paresolvershop.data.repository.PaymentRepository
import com.market.paresolvershop.data.repository.implementations.PaymentRepositoryImpl
import com.market.paresolvershop.domain.payments.CreateStripeSessionUseCase
import com.market.paresolvershop.network.NetworkUtils
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val networkModule = module {
    // Proveemos el cliente HTTP único
    single { NetworkUtils.httpClient }

    // Servicio de API de Pagos configurado desde SupabaseConfig
    single { 
        PaymentApiService(
            baseUrl = SupabaseConfig.supabaseUrl,
            anonKey = SupabaseConfig.supabaseAnonKey
        ) 
    }

    // Repositorio de Pagos
    single<PaymentRepository> { PaymentRepositoryImpl(get()) }

    // Casos de Uso de Pagos
    factoryOf(::CreateStripeSessionUseCase)
}
