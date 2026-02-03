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

object CheckoutSummaryScreen : Screen {
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
                text = "Order Placed Successfully!", 
                fontFamily = SpaceGrotesk, 
                fontWeight = FontWeight.Bold, 
                fontSize = 22.sp
            )
            
            Text(
                text = "Your order has been received and is being processed. You can track it in 'My Orders'.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            Spacer(Modifier.height(40.dp))
            
            Button(
                onClick = {
                    navigator.replaceAll(BottomBarScreen(OrderTab))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Back to Home", fontWeight = FontWeight.Bold)
            }
        }
    }
}
