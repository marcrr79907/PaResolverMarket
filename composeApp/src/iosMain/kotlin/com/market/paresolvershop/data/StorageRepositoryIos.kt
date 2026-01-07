package com.market.paresolvershop.data

import com.market.paresolvershop.data.repository.StorageRepository
import com.market.paresolvershop.domain.model.DataResult

class StorageRepositoryIos : StorageRepository {
    override suspend fun uploadImage(
        bytes: ByteArray,
        fileName: String
    ): DataResult<String> {
        TODO("Not yet implemented")
    }

}