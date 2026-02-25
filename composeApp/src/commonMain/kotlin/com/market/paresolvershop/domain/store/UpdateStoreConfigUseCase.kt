package com.market.paresolvershop.domain.store

import com.market.paresolvershop.data.repository.StoreConfigRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StoreConfig

class UpdateStoreConfigUseCase(
    private val storeConfigRepository: StoreConfigRepository
) {
    suspend operator fun invoke(config: StoreConfig): DataResult<Unit> {
        // Validaciones de negocio básicas
        if (config.storeName.isBlank()) return DataResult.Error("El nombre de la tienda no puede estar vacío")
        if (config.shippingFee < 0) return DataResult.Error("El costo de envío no puede ser negativo")
        
        return storeConfigRepository.updateStoreConfig(config)
    }
}
