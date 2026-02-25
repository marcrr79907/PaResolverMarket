package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.StoreConfigRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StoreConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreConfigRepositoryImpl(
    private val supabase: SupabaseClient
) : StoreConfigRepository {

    private val _storeConfig = MutableStateFlow<StoreConfig?>(null)
    override val storeConfig: StateFlow<StoreConfig?> = _storeConfig.asStateFlow()

    override suspend fun fetchStoreConfig(): DataResult<Unit> {
        return try {
            val result = supabase.from("store_config").select {
                filter { eq("id", "main_config") }
            }.decodeSingleOrNull<StoreConfig>()

            if (result != null) {
                _storeConfig.value = result
                DataResult.Success(Unit)
            } else {
                DataResult.Error("No se encontró la configuración inicial.")
            }
        } catch (e: Exception) {
            DataResult.Error("Error de red: ${e.message}")
        }
    }

    override suspend fun updateStoreConfig(config: StoreConfig): DataResult<Unit> {
        return try {
            // Importante: Al usar update, Supabase devuelve las filas afectadas.
            // Si no devuelve nada, es porque la política RLS bloqueó la operación.
            val result = supabase.from("store_config").update(config) {
                filter { eq("id", "main_config") }
                select() 
            }.decodeSingleOrNull<StoreConfig>()

            if (result != null) {
                _storeConfig.value = result
                DataResult.Success(Unit)
            } else {
                DataResult.Error("No se pudo guardar. Verifica tus permisos de administrador en Supabase (RLS).")
            }
        } catch (e: Exception) {
            DataResult.Error("Fallo al persistir en servidor: ${e.message}")
        }
    }
}
