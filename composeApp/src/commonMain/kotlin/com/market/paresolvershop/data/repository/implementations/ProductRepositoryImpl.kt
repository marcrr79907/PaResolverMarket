package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.ProductEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
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

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    override fun getProducts(categoryId: String?): Flow<List<Product>> {
        return refreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            flow {
                try {
                    val result = supabase.from("products").select {
                        filter {
                            eq("status", "approved") 
                            if (categoryId != null) {
                                eq("category_id", categoryId)
                            }
                        }
                    }.decodeList<ProductEntity>()
                    emit(result.map { it.toDomain() })
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
        val entity = supabase.from("products").select {
            filter { eq("id", id) }
        }.decodeSingle<ProductEntity>()
        DataResult.Success(entity.toDomain())
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al obtener el producto")
    }

    override suspend fun createProduct(product: Product): DataResult<Unit> = runCatching {
        supabase.from("products").insert(product.toEntity())
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al crear producto")
    }

    override suspend fun updateProduct(product: Product): DataResult<Unit> = runCatching {
        supabase.from("products").update(product.toEntity()) {
            filter { eq("id", product.id) }
        }
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al actualizar producto")
    }

    override suspend fun deleteProduct(productId: String): DataResult<Unit> = runCatching {
        supabase.from("products").delete {
            filter { eq("id", productId) }
        }
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al eliminar producto")
    }

    override suspend fun getAllProductsAdmin(): DataResult<List<Product>> = runCatching {
        val result = supabase.from("products").select().decodeList<ProductEntity>()
        DataResult.Success(result.map { it.toDomain() })
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al cargar inventario")
    }

    override suspend fun updateProductStatus(productId: String, status: String): DataResult<Unit> = runCatching {
        supabase.from("products").update(mapOf("status" to status)) {
            filter { eq("id", productId) }
        }
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al actualizar estado")
    }
}
