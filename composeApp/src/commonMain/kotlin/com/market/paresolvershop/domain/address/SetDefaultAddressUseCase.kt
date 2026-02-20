package com.market.paresolvershop.domain.address

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.domain.model.DataResult

class SetDefaultAddressUseCase(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(addressId: String): DataResult<Unit> {
        return addressRepository.setDefaultAddress(addressId)
    }
}