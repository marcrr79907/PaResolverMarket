package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.StoreConfig
import kotlinx.coroutines.flow.StateFlow

interface StoreConfigRepository {
    val storeConfig: StateFlow<StoreConfig?>
    suspend fun fetchStoreConfig(): DataResult<Unit>
    suspend fun updateStoreConfig(config: StoreConfig): DataResult<Unit>
}
