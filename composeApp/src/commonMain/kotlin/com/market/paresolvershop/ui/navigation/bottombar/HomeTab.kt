package com.market.paresolver.ui.navigation.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.market.paresolvershop.ui.products.CatalogScreen
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Home
import org.koin.core.annotation.KoinExperimentalAPI

object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Inicio"
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        Navigator(screen = CatalogScreen)
    }
}