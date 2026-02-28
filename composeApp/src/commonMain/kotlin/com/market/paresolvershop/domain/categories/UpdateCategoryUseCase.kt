package com.market.paresolvershop.domain.categories

import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult

class UpdateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): DataResult<Unit> {
        if (category.name.isBlank()) return DataResult.Error("El nombre no puede estar vacío")
        return repository.updateCategory(category)
    }
}
