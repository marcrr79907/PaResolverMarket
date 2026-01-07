package com.market.paresolvershop.domain.model

/**
 * Una clase sellada genérica para encapsular los resultados de operaciones de datos.
 * Puede contener un éxito con datos de tipo [T], o un error con un mensaje.
 *
 * @param T El tipo de datos esperado en caso de éxito.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val message: String) : DataResult<Nothing>
}
