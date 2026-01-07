package com.market.paresolvershop.ui.navigation.bottombar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.market.paresolver.ui.navigation.bottombar.HomeTab

class BottomBarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // Usamos HomeTab como la pestaña inicial
        TabNavigator(HomeTab) { tabNavigator ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(tabNavigator.current.options.title) }
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        CurrentTab() // Muestra el contenido de la pestaña actual
                    }
                },
                bottomBar = {
                    NavigationBar {
                        // Lista de todas las pestañas de la barra inferior
                        TabNavigationItem(HomeTab)
                        TabNavigationItem(SearchTab)
                        TabNavigationItem(CartTab)
                        TabNavigationItem(ProfileTab)
                    }
                }
            )
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = tab.options.title,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        label = {
            Text(tab.options.title, style = MaterialTheme.typography.labelSmall)
        }
    )
}
