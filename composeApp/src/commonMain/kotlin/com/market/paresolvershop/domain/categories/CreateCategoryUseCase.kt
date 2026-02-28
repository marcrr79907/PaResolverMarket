package com.market.paresolvershop.domain.categories

import com.market.paresolvershop.data.repository.CategoryRepository
import com.market.paresolvershop.domain.model.Category
import com.market.paresolvershop.domain.model.DataResult

class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String): DataResult<Unit> {
        if (name.isBlank()) return DataResult.Error("El nombre no puede estar vacío")
        return repository.createCategory(Category(name = name))
    }
}
