package com.market.paresolvershop.config

/**
 * Configuración de Supabase multiplataforma.
 * Cada plataforma provee su propia implementación.
 */
expect object SupabaseConfig {
    val supabaseUrl: String
    val supabaseAnonKey: String
    val webClientId: String
}
