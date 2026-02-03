package com.market.paresolvershop.ui.navigation.bottombar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.market.paresolvershop.ui.admin.AdminScreen
import com.market.paresolvershop.ui.authentication.LoginScreen
import com.market.paresolvershop.ui.authentication.RegisterScreen
import com.market.paresolvershop.ui.profile.AddressManagementScreen
import com.market.paresolvershop.ui.profile.GuestProfileScreen
import com.market.paresolvershop.ui.profile.ProfileScreen
import com.market.paresolvershop.ui.profile.ProfileUiState
import com.market.paresolvershop.ui.profile.ProfileViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.User
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Profile"
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.User)
            return remember { TabOptions(index = 4u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(screen = ProfileRootScreen)
    }
}

object ProfileRootScreen : Screen {
    @OptIn(KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<ProfileViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        when (val state = uiState) {
            is ProfileUiState.Authenticated -> {
                ProfileScreen(
                    userEmail = state.user.email,
                    userName = state.user.name,
                    isAdmin = state.isAdmin,
                    onNavigateToAdmin = { navigator.push(AdminScreen) },
                    onNavigateToHistory = { /* Implementar más tarde */ },
                    onNavigateToAddresses = { navigator.push(AddressManagementScreen()) },
                    onLogout = viewModel::logOut
                )
            }
            is ProfileUiState.NotAuthenticated -> {
                GuestProfileScreen(
                    onLoginClick = { navigator.push(LoginScreen) },
                    onRegisterClick = { navigator.push(RegisterScreen) }
                )
            }
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message)
                }
            }
        }
    }
}
