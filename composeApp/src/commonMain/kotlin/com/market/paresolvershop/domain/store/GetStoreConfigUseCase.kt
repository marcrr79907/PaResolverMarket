package com.market.paresolvershop.domain.store

import com.market.paresolvershop.data.repository.StoreConfigRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StoreConfig
import kotlinx.coroutines.flow.StateFlow

class GetStoreConfigUseCase(
    private val storeConfigRepository: StoreConfigRepository
) {
    /**
     * Proporciona un flujo reactivo con la configuración de la tienda.
     */
    val storeConfig: StateFlow<StoreConfig?> = storeConfigRepository.storeConfig

    /**
     * Dispara la actualización de la configuración desde la base de datos.
     */
    suspend operator fun invoke(): DataResult<Unit> {
        return storeConfigRepository.fetchStoreConfig()
    }
}
