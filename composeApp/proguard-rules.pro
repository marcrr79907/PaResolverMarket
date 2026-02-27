# Reglas base para Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# Evitar que R8 borre las entidades de tu base de datos
-keep class com.market.paresolvershop.data.model.** { *; }
-keep class com.market.paresolvershop.domain.model.** { *; }

# Supabase y Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Ktor tries to use java.lang.management which is not available on Android
-dontwarn java.lang.management.**
