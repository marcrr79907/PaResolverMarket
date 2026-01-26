package com.market.paresolvershop.di

import io.github.jan_tennert.supabase.SupabaseClient
import io.github.jan_tennert.supabase.auth.Auth
import io.github.jan_tennert.supabase.createSupabaseClient
import io.github.jan_tennert.supabase.postgrest.Postgrest
import io.github.jan_tennert.supabase.realtime.Realtime
import io.github.jan_tennert.supabase.serializer.KotlinXSerializer
import io.github.jan_tennert.supabase.storage.Storage
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val supabaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = "https://your-project.supabase.co", // TODO: Reemplazar con tu URL
            supabaseKey = "your-anon-key" // TODO: Reemplazar con tu Anon Key
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
            
            // Configuración de serialización recomendada por Supabase
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
                isLenient = true
            })
        }
    }
}
