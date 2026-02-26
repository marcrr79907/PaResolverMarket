package com.market.paresolvershop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.market.paresolvershop.di.addressModule
import com.market.paresolvershop.di.authModule
import com.market.paresolvershop.di.cartModule
import com.market.paresolvershop.di.checkoutModule
import com.market.paresolvershop.di.orderModule
import com.market.paresolvershop.di.platformModule
import com.market.paresolvershop.di.productModule
import com.market.paresolvershop.di.storeConfigModule
import com.market.paresolvershop.di.supabaseModule
import com.market.paresolvershop.ui.admin.AdminScreen
import com.market.paresolvershop.ui.navigation.bottombar.BottomBarScreen
import com.market.paresolvershop.ui.profile.ProfileUiState
import com.market.paresolvershop.ui.profile.ProfileViewModel
import com.market.paresolvershop.ui.theme.ShopAppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    KoinApplication(
        application = {
            modules(
                addressModule, authModule, cartModule, checkoutModule,
                orderModule, platformModule, productModule, storeConfigModule, supabaseModule,
            )
        }
    ) {
        ShopAppTheme {
            val profileViewModel = koinViewModel<ProfileViewModel>()
            val uiState by profileViewModel.uiState.collectAsState()

            // Root Routing dinámico basado en el rol
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    // Aquí podrías mostrar una Splash real o CircularProgress
                }
                is ProfileUiState.Authenticated -> {
                    if (state.isAdmin) {
                        // El Admin entra a su propio ecosistema sin BottomBar
                        Navigator(screen = AdminScreen) { navigator -> SlideTransition(navigator) }
                    } else {
                        // El Cliente entra al flujo normal
                        Navigator(screen = BottomBarScreen()) { navigator -> SlideTransition(navigator) }
                    }
                }
                else -> {
                    // Los invitados siempre ven el catálogo (Guest Mode)
                    Navigator(screen = BottomBarScreen()) { navigator -> SlideTransition(navigator) }
                }
            }
        }
    }
}
