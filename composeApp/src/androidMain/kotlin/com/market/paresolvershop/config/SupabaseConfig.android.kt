package com.market.paresolvershop.config

import com.market.paresolvershop.BuildConfig

actual object SupabaseConfig {
    actual val supabaseUrl: String = BuildConfig.SUPABASE_URL
    actual val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
    actual val webClientId: String = BuildConfig.WEB_CLIENT_ID
}
