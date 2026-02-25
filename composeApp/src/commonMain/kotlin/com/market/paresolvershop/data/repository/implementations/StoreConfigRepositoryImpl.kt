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
            // Buscamos la configuración principal (id = 'main_config')
            val result = supabase.from("store_config").select {
                filter { eq("id", "main_config") }
            }.decodeSingleOrNull<StoreConfig>()

            if (result != null) {
                _storeConfig.value = result
                DataResult.Success(Unit)
            } else {
                DataResult.Error("No se encontró la configuración de la tienda")
            }
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener la configuración")
        }
    }

    override suspend fun updateStoreConfig(config: StoreConfig): DataResult<Unit> {
        return try {
            supabase.from("store_config").update(config) {
                filter { eq("id", "main_config") }
            }
            _storeConfig.value = config
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al actualizar la configuración")
        }
    }
}
