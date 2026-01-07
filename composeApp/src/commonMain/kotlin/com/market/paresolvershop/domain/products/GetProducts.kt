package com.market.paresolvershop.domain.products

import com.market.paresolvershop.data.repository.ProductRepository

class GetProducts(private val repository: ProductRepository) {
    // La función 'invoke' permite llamar a la clase como si fuera una función.
    operator fun invoke() = repository.getAllProducts()
}
