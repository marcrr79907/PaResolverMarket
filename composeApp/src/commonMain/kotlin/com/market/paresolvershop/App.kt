package com.market.paresolvershop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.market.paresolvershop.di.authModule
import com.market.paresolvershop.di.cartModule
import com.market.paresolvershop.di.checkoutModule
import com.market.paresolvershop.di.platformModule
import com.market.paresolvershop.di.productModule
import com.market.paresolvershop.di.supabaseModule
import com.market.paresolvershop.ui.navigation.bottombar.BottomBarScreen
import org.koin.compose.KoinApplication

@Composable
fun App() {
    // Inicializar Koin para toda la aplicación
    KoinApplication(application = {
        modules(
            supabaseModule,
            authModule,
            platformModule,
            productModule,
            cartModule,
            checkoutModule
        )
    }) {
        // Tema de Material Design
        MaterialTheme {
            Navigator(screen = BottomBarScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
