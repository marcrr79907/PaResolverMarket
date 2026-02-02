package com.market.paresolvershop.ui.profilemagnament

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    userName: String,
    isAdmin: Boolean,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { /* Handle back or menu */ },
                        modifier = Modifier.padding(start = 12.dp).background(SurfaceVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(FontAwesomeIcons.Solid.ArrowLeft, contentDescription = "Back", modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // 1. Perfil Foto y Nombre
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, SoftGray),
                color = SurfaceVariant
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.User,
                    contentDescription = null,
                    modifier = Modifier.padding(30.dp),
                    tint = OnSurfaceVariant
                )
            }

            Text(
                text = userName,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // 2. Card de Funciones Comunes
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Common Functions",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileGridItem("History", FontAwesomeIcons.Solid.History, onNavigateToHistory)
                        ProfileGridItem("Admin", FontAwesomeIcons.Solid.Tools, if (isAdmin) onNavigateToAdmin else null)
                        ProfileGridItem("Addresses", FontAwesomeIcons.Solid.MapMarkerAlt, onNavigateToAddresses)
                    }

                    Spacer(Modifier.height(24.dp))

                    // 3. Botones de Acción Estilo Lista
                    ActionListItem("Rating and Review", FontAwesomeIcons.Solid.Pen)
                    ActionListItem("Contact Support", FontAwesomeIcons.Solid.Phone)
                    ActionListItem("Social Media Link", FontAwesomeIcons.Solid.ShareAlt)

                    Spacer(Modifier.height(24.dp))

                    // 4. Logout Button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
                    ) {
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileGridItem(label: String, icon: ImageVector, onClick: (() -> Unit)?) {
    val alpha = if (onClick != null) 1f else 0.3f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }.alpha(alpha)
    ) {
        Surface(
            modifier = Modifier.size(65.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
        Text(label, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), fontFamily = Inter)
    }
}

@Composable
fun ActionListItem(label: String, icon: ImageVector) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = SurfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.White) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = label,
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(FontAwesomeIcons.Solid.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
        }
    }
}

// Necesario para el ProfileGridItem
private fun Modifier.alpha(alpha: Float): Modifier = this.then(Modifier.background(Color.Transparent)) // Dummy, use graphicsLayer if needed

@Composable
fun GuestProfileScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = SurfaceVariant
        ) {
            Icon(FontAwesomeIcons.Solid.UserLock, contentDescription = null, modifier = Modifier.padding(24.dp), tint = Primary)
        }

        Spacer(Modifier.height(24.dp))

        Text("Tu Perfil", style = MaterialTheme.typography.displayLarge, color = Primary)
        Text(
            "Inicia sesión para gestionar tus pedidos y direcciones",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Primary)
        ) {
            Text("Crear Cuenta", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}
