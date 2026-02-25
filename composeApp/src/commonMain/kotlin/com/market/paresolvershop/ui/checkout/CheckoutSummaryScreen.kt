package com.market.paresolvershop.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.navigation.bottombar.BottomBarScreen
import com.market.paresolvershop.ui.navigation.bottombar.OrderTab
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CheckCircle

data class CheckoutSummaryScreen(val orderId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp), 
                shape = CircleShape, 
                color = Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.CheckCircle, 
                        contentDescription = null, 
                        modifier = Modifier.size(60.dp), 
                        tint = Primary
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "¡Pedido Realizado!", 
                fontFamily = SpaceGrotesk, 
                fontWeight = FontWeight.Bold, 
                fontSize = 24.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "ID: #${orderId.take(8).uppercase()}",
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
            
            Text(
                text = "Hemos recibido tu pedido y lo estamos procesando. Puedes seguir su estado en 'Mis Pedidos'.",
                textAlign = TextAlign.Center,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp),
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = {
                    // Volvemos al inicio, específicamente a la pestaña de pedidos
                    navigator.replaceAll(BottomBarScreen(OrderTab))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
            ) {
                Text("Ver mis pedidos", fontWeight = FontWeight.Bold, fontFamily = SpaceGrotesk)
            }
        }
    }
}
