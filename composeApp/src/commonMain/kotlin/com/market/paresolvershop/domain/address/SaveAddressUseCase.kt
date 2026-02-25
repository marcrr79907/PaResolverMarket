package com.market.paresolvershop.domain.address

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.UserAddress

class SaveAddressUseCase(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(address: UserAddress): DataResult<Unit> {
        return addressRepository.saveAddress(address)
    }
}
