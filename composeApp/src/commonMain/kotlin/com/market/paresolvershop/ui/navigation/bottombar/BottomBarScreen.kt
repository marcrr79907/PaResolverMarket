package com.market.paresolvershop.ui.navigation.bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.market.paresolvershop.ui.navigation.bottombar.HomeTab
import com.market.paresolvershop.ui.theme.AppShapes
import com.market.paresolvershop.ui.theme.Primary

// Cambiamos a data class para permitir pasar la pestaña inicial
data class BottomBarScreen(val initialTab: Tab = HomeTab) : Screen {
    @Composable
    override fun Content() {
        TabNavigator(initialTab) { tabNavigator ->
            Scaffold(
                content = { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        CurrentTab()
                    }
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                            .height(70.dp)
                            .clip(AppShapes.large),
                        color = Primary,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxSize(),
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            TabNavigationItem(HomeTab, "Explorer")
                            TabNavigationItem(CartTab, "Cart")
                            TabNavigationItem(FavoriteTab, "Wishlist")
                            TabNavigationItem(OrderTab, "My Order")
                            TabNavigationItem(ProfileTab, "Profile")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab, label: String) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = label,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }
        },
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = if (isSelected) 1f else 0.7f)
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.White.copy(alpha = 0.2f)
        )
    )
}
