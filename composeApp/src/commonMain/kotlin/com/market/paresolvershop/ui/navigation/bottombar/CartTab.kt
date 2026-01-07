package com.market.paresolvershop.ui.navigation.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.market.paresolvershop.ui.navigation.CartScreen
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ShoppingCart

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

    @Composable
    override fun Content() {
        CartScreen(onCheckout = { /* TODO */ })
    }
}
