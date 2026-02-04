package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class ProductRepositoryImpl(
    private val supabase: SupabaseClient
) : ProductRepository {

    // Gatillo para forzar la recarga de los flujos de productos
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    override fun getProducts(categoryId: String?): Flow<List<Product>> {
        return refreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            flow {
                try {
                    val result = supabase.from("products").select {
                        filter {
                            // En el marketplace, el cliente solo ve productos aprobados
                            eq("status", "approved") 
                            if (categoryId != null) {
                                eq("category_id", categoryId)
                            }
                        }
                    }.decodeList<Product>()
                    emit(result)
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
    }

    override suspend fun fetchProducts(categoryId: String?): DataResult<Unit> {
        refreshTrigger.emit(Unit)
        return DataResult.Success(Unit)
    }

    override suspend fun getProductById(id: String): DataResult<Product> = runCatching {
        val product = supabase.from("products").select {
            filter { eq("id", id) }
        }.decodeSingle<Product>()
        DataResult.Success(product)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al obtener el producto")
    }

    override suspend fun getAllProductsAdmin(): DataResult<List<Product>> = runCatching {
        val result = supabase.from("products").select().decodeList<Product>()
        DataResult.Success(result)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al cargar inventario")
    }

    override suspend fun updateProductStatus(productId: String, status: String): DataResult<Unit> = runCatching {
        supabase.from("products").update(mapOf("status" to status)) {
            filter { eq("id", productId) }
        }
        // Refrescamos los flujos globales para que la Home se actualice si se aprueba algo
        fetchProducts()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al actualizar estado")
    }
}
