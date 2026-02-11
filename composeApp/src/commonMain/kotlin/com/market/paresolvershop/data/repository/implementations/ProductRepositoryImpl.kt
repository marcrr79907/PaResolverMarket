package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.ProductEntity
import com.market.paresolvershop.data.model.ProductImageEntity
import com.market.paresolvershop.data.model.ProductVariantEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toUpdateEntity
import com.market.paresolvershop.data.model.toEntity
import com.market.paresolvershop.data.repository.ProductRepository
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
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
                    val result = supabase.from("products").select(
                        columns = Columns.raw("*, product_images(*), product_variants(*)")
                    ) {
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
        val entity = supabase.from("products").select(
            columns = Columns.raw("*, product_images(*), product_variants(*)")
        ) {
            filter { eq("id", id) }
        }.decodeSingle<ProductEntity>()
        DataResult.Success(entity.toDomain())
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al obtener el producto")
    }

    override suspend fun createProduct(product: Product): DataResult<Unit> = runCatching {
        val insertedProduct = supabase.from("products").insert(product.toEntity()) {
            select()
        }.decodeSingle<ProductEntity>()
        
        val productId = insertedProduct.id ?: throw Exception("ID de producto no generado")

        if (product.images.isNotEmpty()) {
            val imagesEntities = product.images.map { url ->
                ProductImageEntity(product_id = productId, image_url = url)
            }
            supabase.from("product_images").insert(imagesEntities)
        }

        if (product.variants.isNotEmpty()) {
            val variantsEntities = product.variants.map { variant ->
                ProductVariantEntity(
                    product_id = productId,
                    name = variant.name,
                    price_override = variant.price,
                    stock = variant.stock,
                    sku = variant.sku
                )
            }
            supabase.from("product_variants").insert(variantsEntities)
        }

        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al crear producto")
    }

    override suspend fun updateProduct(product: Product): DataResult<Unit> = runCatching {
        // 1. Actualizar información básica en la tabla 'products'
        supabase.from("products").update(product.toUpdateEntity()) {
            filter { eq("id", product.id) }
        }

        // 2. Sincronizar Imágenes: Borramos las anteriores y guardamos la lista actual
        supabase.from("product_images").delete {
            filter { eq("product_id", product.id) }
        }
        if (product.images.isNotEmpty()) {
            val imagesEntities = product.images.map { url ->
                ProductImageEntity(product_id = product.id, image_url = url)
            }
            supabase.from("product_images").insert(imagesEntities)
        }

        // 3. Sincronizar Variantes: Borramos las anteriores y guardamos las nuevas
        supabase.from("product_variants").delete {
            filter { eq("product_id", product.id) }
        }
        if (product.variants.isNotEmpty()) {
            val variantsEntities = product.variants.map { variant ->
                ProductVariantEntity(
                    product_id = product.id,
                    name = variant.name,
                    price_override = variant.price,
                    stock = variant.stock,
                    sku = variant.sku
                )
            }
            supabase.from("product_variants").insert(variantsEntities)
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
        val result = supabase.from("products").select(
            columns = Columns.raw("*, product_images(*), product_variants(*)")
        ).decodeList<ProductEntity>()
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
