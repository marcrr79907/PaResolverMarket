package com.market.paresolvershop.ui.navigation.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.market.paresolvershop.ui.navigation.SearchScreen
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Search

object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Buscar"
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.Search)

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        Navigator(screen = SearchScreen)
    }
}
