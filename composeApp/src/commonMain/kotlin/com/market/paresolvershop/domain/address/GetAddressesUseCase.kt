package com.market.paresolvershop.domain.address

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.UserAddress
import kotlinx.coroutines.flow.StateFlow

class GetAddressesUseCase(
    private val addressRepository: AddressRepository
) {
    val addresses: StateFlow<List<UserAddress>> = addressRepository.addresses

    suspend operator fun invoke(): DataResult<Unit> {
        return addressRepository.fetchAddresses()
    }
}
