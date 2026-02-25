package com.market.paresolvershop.domain.address

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.domain.model.DataResult

class DeleteAddressUseCase(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(addressId: String): DataResult<Unit> {
        return addressRepository.deleteAddress(addressId)
    }
}
