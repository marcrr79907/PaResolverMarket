package com.market.paresolvershop.data.repository

import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun createCategory(category: Category): DataResult<Unit>
    suspend fun deleteCategory(id: String): DataResult<Unit>
}
