package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.UserAddress
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.StateFlow

interface AddressRepository {
    val addresses: StateFlow<List<UserAddress>>
    suspend fun fetchAddresses(): DataResult<Unit>
    suspend fun saveAddress(address: UserAddress): DataResult<Unit>
    suspend fun deleteAddress(addressId: String): DataResult<Unit>
    suspend fun setDefaultAddress(addressId: String): DataResult<Unit>
}
