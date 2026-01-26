package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepositoryImpl(
    private val supabase: SupabaseClient
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> = flow {
        try {
            val result = supabase.from("products")
                .select()
                .decodeList<Product>()
            emit(result)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductById(id: String): DataResult<Product> = runCatching {
        val product = supabase.from("products")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingle<Product>()
        DataResult.Success(product)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al obtener el producto")
    }

    override suspend fun createProduct(product: Product): DataResult<Unit> = runCatching {
        supabase.from("products").insert(product)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "No tienes permisos de administrador")
    }

    override suspend fun updateProduct(product: Product): DataResult<Unit> = runCatching {
        supabase.from("products").update(product) {
            filter { eq("id", product.id) }
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al actualizar")
    }

    override suspend fun deleteProduct(productId: String): DataResult<Unit> = runCatching {
        supabase.from("products").delete {
            filter { eq("id", productId) }
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al eliminar")
    }
}
