package com.market.paresolvershop.domain.orders

import com.market.paresolvershop.data.repository.CartRepository
import com.market.paresolvershop.data.repository.OrderRepository
import com.market.paresolvershop.domain.model.CartItem
import com.market.paresolvershop.domain.model.DataResult
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.domain.model.OrderItem

class PlaceOrderUseCase(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        order: Order,
        cartItems: List<CartItem>
    ): DataResult<String> {
        // 1. Validación de Negocio: No permitir pedidos vacíos
        if (cartItems.isEmpty()) {
            return DataResult.Error("El carrito está vacío")
        }

        // 2. Mapeo de CartItems a OrderItems (Lógica de Dominio)
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                id = "",
                orderId = "",
                productId = cartItem.product.id,
                quantity = cartItem.quantity,
                priceAtPurchase = cartItem.product.price
            )
        }

        // 3. Ejecutar la creación del pedido
        val result = orderRepository.createOrder(order, orderItems)

        // 4. Si el pedido fue exitoso, limpiamos el carrito (Orquestación)
        if (result is DataResult.Success) {
            cartRepository.clearCart()
        }

        return result
    }
}
