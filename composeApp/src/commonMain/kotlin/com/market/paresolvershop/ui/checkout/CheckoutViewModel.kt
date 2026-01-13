package com.market.paresolvershop.ui.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Estado para el formulario de envío
data class ShippingFormState(
    val fullName: String = "",
    val address: String = "",
    val additionalInfo: String = "",
    val city: String = "",
    val province: String = "",
    val phone: String = ""
)

class CheckoutViewModel : ViewModel() {

    var formState by mutableStateOf(ShippingFormState())
        private set

    fun onFullNameChange(name: String) {
        formState = formState.copy(fullName = name)
    }

    fun onAddressChange(address: String) {
        formState = formState.copy(address = address)
    }

    fun onAdditionalInfoChange(info: String) {
        formState = formState.copy(additionalInfo = info)
    }

    fun onCityChange(city: String) {
        formState = formState.copy(city = city)
    }

    fun onProvinceChange(province: String) {
        formState = formState.copy(province = province)
    }

    fun onPhoneChange(phone: String) {
        formState = formState.copy(phone = phone)
    }
    
    fun canProceedToPayment(): Boolean {
        // Aquí se puede añadir una lógica de validación más compleja
        return formState.fullName.isNotBlank() &&
               formState.address.isNotBlank() &&
               formState.city.isNotBlank() &&
               formState.province.isNotBlank() &&
               formState.phone.isNotBlank()
    }
}
