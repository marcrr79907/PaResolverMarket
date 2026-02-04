package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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
                    val result = supabase.from("categories").select().decodeList<Category>()
                    emit(result)
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
    }

    override suspend fun createCategory(category: Category): DataResult<Unit> = runCatching {
        supabase.from("categories").insert(category)
        refreshTrigger.emit(Unit)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al crear categoría")
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
