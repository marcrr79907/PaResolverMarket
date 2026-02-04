package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.CategoryEntity
import com.market.paresolvershop.data.model.toDomain
import com.market.paresolvershop.data.model.toEntity
import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

class CategoryRepositoryImpl(
    private val supabase: SupabaseClient
) : CategoryRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    override fun getCategories(): Flow<List<Category>> {
        return refreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            flow {
                try {
                    val entities = supabase.from("categories").select().decodeList<CategoryEntity>()
                    emit(entities.map { it.toDomain() })
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
    }

    override fun getActiveCategories(): Flow<List<Category>> {
        return refreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            flow {
                try {
                    // Seleccionamos categorías que tienen al menos un producto aprobado
                    // Usamos un join con la tabla products
                    val entities = supabase.from("categories").select(
                        columns = Columns.raw("*, products!inner(status)")
                    ) {
                        filter {
                            eq("products.status", "approved")
                        }
                    }.decodeList<CategoryEntity>()
                    
                    // Supabase con !inner a veces devuelve duplicados si hay muchos productos,
                    // así que nos aseguramos de que sean únicas.
                    emit(entities.distinctBy { it.id }.map { it.toDomain() })
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
    }

    override suspend fun createCategory(category: Category): DataResult<Unit> = runCatching {
        supabase.from("categories").insert(category.toEntity())
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al crear categoría")
    }

    override suspend fun updateCategory(category: Category): DataResult<Unit> = runCatching {
        supabase.from("categories").update(category.toEntity()) {
            filter { eq("id", category.id) }
        }
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al actualizar categoría")
    }

    override suspend fun deleteCategory(id: String): DataResult<Unit> = runCatching {
        supabase.from("categories").delete {
            filter { eq("id", id) }
        }
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al eliminar categoría")
    }
}
