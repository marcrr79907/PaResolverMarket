package com.market.paresolvershop.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.admin.*
import com.market.paresolvershop.ui.profile.ProfileViewModel
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun AdminScaffold(
    title: String,
    currentScreen: Screen,
    actions: @Composable RowScope.() -> Unit = {},
    extraHeader: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.currentOrThrow
    val profileViewModel = koinViewModel<ProfileViewModel>()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminSidebar(
                currentScreen = currentScreen,
                onItemSelected = { screen ->
                    scope.launch { drawerState.close() }
                    if (screen::class != currentScreen::class) {
                        navigator.push(screen)
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        profileViewModel.logOut()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                title,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Bars,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(20.dp),
                                    tint = Primary
                                )
                            }
                        },
                        actions = actions,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            navigationIconContentColor = Primary,
                            actionIconContentColor = Primary
                        )
                    )
                    extraHeader?.invoke(this)
                }
            },
            content = content
        )
    }
}

@Composable
fun AdminSidebar(
    currentScreen: Screen,
    onItemSelected: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                "PaResolver Market",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Primary
            )
            Text(
                "Panel de Administración",
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(40.dp))

            SidebarItem(
                "Dashboard",
                FontAwesomeIcons.Solid.ChartPie,
                isSelected = currentScreen is AdminScreen
            ) { onItemSelected(AdminScreen) }
            
            SidebarItem(
                "Inventario",
                FontAwesomeIcons.Solid.Boxes,
                isSelected = currentScreen is InventoryScreen
            ) { onItemSelected(InventoryScreen) }
            
            SidebarItem(
                "Categorías", 
                FontAwesomeIcons.Solid.Tags,
                isSelected = currentScreen is CategoryManagementScreen
            ) { onItemSelected(CategoryManagementScreen) }
            
            SidebarItem(
                "Órdenes", 
                FontAwesomeIcons.Solid.ShoppingCart,
                isSelected = currentScreen is OrderManagementScreen
            ) { onItemSelected(OrderManagementScreen) }
            
            SidebarItem(
                "Configuración", 
                FontAwesomeIcons.Solid.Cogs,
                isSelected = currentScreen is StoreManagementScreen
            ) { onItemSelected(StoreManagementScreen) }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = SoftGray.copy(alpha = 0.5f)
            )

            SidebarItem(
                "Cerrar Sesión",
                FontAwesomeIcons.Solid.SignOutAlt,
                color = Error
            ) { onLogout() }
        }
    }
}

@Composable
fun SidebarItem(
    label: String,
    icon: ImageVector,
    color: Color = OnSurface,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) Primary else color
            )
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Primary else color
            )
        }
    }
}
