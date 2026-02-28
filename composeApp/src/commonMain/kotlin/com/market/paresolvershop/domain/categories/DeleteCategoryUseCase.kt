package com.market.paresolvershop.domain.categories

import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.domain.model.DataResult

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String): DataResult<Unit> {
        return repository.deleteCategory(id)
    }
}
