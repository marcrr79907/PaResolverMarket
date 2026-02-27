package com.market.paresolvershop.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.market.paresolvershop.ui.admin.components.AdminScaffold
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import org.koin.core.annotation.KoinExperimentalAPI

object AdminScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        AdminScaffold(
            title = "Dashboard Administrativo",
            currentScreen = AdminScreen
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Resumen del Negocio",
                    style = Typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        "Ventas hoy",
                        "12",
                        FontAwesomeIcons.Solid.ShoppingBag,
                        Primary,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Ingresos",
                        "$1,240",
                        FontAwesomeIcons.Solid.DollarSign,
                        Color(0xFF4CAF50),
                        Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        "Stock Crítico",
                        "4",
                        FontAwesomeIcons.Solid.ExclamationTriangle,
                        Error,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Nuevos Usuarios",
                        "8",
                        FontAwesomeIcons.Solid.Users,
                        OnSurfaceVariant,
                        Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.5f))

                Text(
                    "Gestión Rápida",
                    style = Typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardActionRow(
                        "Control de Inventario",
                        FontAwesomeIcons.Solid.Boxes
                    ) { navigator.push(InventoryScreen) }
                    DashboardActionRow(
                        "Gestión de Categorías",
                        FontAwesomeIcons.Solid.Tags
                    ) { navigator.push(CategoryManagementScreen) }
                    DashboardActionRow(
                        "Administrar Órdenes",
                        FontAwesomeIcons.Solid.ClipboardList
                    ) { navigator.push(OrderManagementScreen) }
                    DashboardActionRow(
                        "Ajustes de la Tienda",
                        FontAwesomeIcons.Solid.Store
                    ) { navigator.push(StoreManagementScreen) }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                fontFamily = SpaceGrotesk
            )
            Text(label, fontSize = 12.sp, color = OnSurfaceVariant, fontFamily = Inter)
        }
    }
}

@Composable
fun DashboardActionRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SurfaceVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp)
                    .background(Primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = Primary)
            }
            Text(
                label,
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                fontWeight = FontWeight.Bold,
                fontFamily = Inter,
                fontSize = 15.sp
            )
            Icon(
                FontAwesomeIcons.Solid.ChevronRight,
                null,
                modifier = Modifier.size(14.dp),
                tint = OnSurfaceVariant
            )
        }
    }
}
