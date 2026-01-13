package com.market.paresolvershop.ui.navigation.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.market.paresolvershop.ui.checkout.CheckoutShippingScreen
import com.market.paresolvershop.ui.navigation.CartScreen
import com.market.paresolvershop.ui.products.CartViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ShoppingCart
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object CartTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Carrito"
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.ShoppingCart)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon
                )
            }
        }

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val cartViewModel = koinViewModel<CartViewModel>()
        CartScreen(
            cartViewModel = cartViewModel,
            // Usamos el navegador padre para salir del flujo de pestañas
            onCheckout = { navigator.parent?.push(CheckoutShippingScreen) }
        )
    }
}
