package com.market.paresolvershop.config

actual object SupabaseConfig {
    // En iOS, necesitarás leer estos valores desde Info.plist o usar BuildKonfig
    // Por ahora, valores hardcodeados (cambiar antes de producción)
    actual val supabaseUrl: String = "https://jrsfnjljdkpecyhhaqch.supabase.co"
    actual val supabaseAnonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Impyc2ZuamxqZGtwZWN5aGhhcWNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg0ODU0NTQsImV4cCI6MjA4NDA2MTQ1NH0.mFXatXycMSPkrEmJfgAIrQGzb6owpog9Bcq-1PpTeTc"
    actual val webClientId: String = "1076965141728-esg2qu1gb315hb88mlucmohug5kmb0fg.apps.googleusercontent.com"
}
