package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.AddressRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.UserAddress
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddressRepositoryImpl(
    private val supabase: SupabaseClient
) : AddressRepository {

    private val _addresses = MutableStateFlow<List<UserAddress>>(emptyList())
    override val addresses: StateFlow<List<UserAddress>> = _addresses.asStateFlow()

    override suspend fun fetchAddresses(): DataResult<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return DataResult.Error("Usuario no autenticado")
            val result = supabase.from("user_addresses").select {
                filter {
                    eq("user_id", userId);
                    eq("is_visible", true)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<UserAddress>()
            
            _addresses.value = result
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al obtener direcciones")
        }
    }

    override suspend fun saveAddress(address: UserAddress): DataResult<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return DataResult.Error("Usuario no autenticado")
            
            // Inyectamos el userId en el objeto antes de guardarlo
            val addressWithUser = address.copy(userId = userId)
            
            if (address.isDefault) {
                clearDefaults(userId)
            }

            if (address.id == null) {
                // Insertar nueva (id es null, Supabase lo genera)
                supabase.from("user_addresses").insert(addressWithUser) {
                    select()
                }
            } else {
                // Actualizar existente (id no es null)
                supabase.from("user_addresses").update(addressWithUser) {
                    filter { eq("id", address.id) }
                }
            }
            
            fetchAddresses() // Recargar lista
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al guardar dirección")
        }
    }

    override suspend fun deleteAddress(addressId: String): DataResult<Unit> {
        return try {
            supabase.from("user_addresses").update(mapOf("is_visible" to false)) {
                filter { eq("id", addressId) }
            }
            fetchAddresses()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al eliminar dirección")
        }
    }

    override suspend fun setDefaultAddress(addressId: String): DataResult<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return DataResult.Error("Usuario no autenticado")
            clearDefaults(userId)
            
            supabase.from("user_addresses").update(mapOf("is_default" to true)) {
                filter { eq("id", addressId) }
            }
            
            fetchAddresses()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al establecer dirección predeterminada")
        }
    }

    private suspend fun clearDefaults(userId: String) {
        supabase.from("user_addresses").update(mapOf("is_default" to false)) {
            filter { eq("user_id", userId) }
        }
    }
}
