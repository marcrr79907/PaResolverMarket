package com.market.paresolvershop.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.market.paresolvershop.ui.theme.Error
import com.market.paresolvershop.ui.theme.Inter
import com.market.paresolvershop.ui.theme.OnSurface
import com.market.paresolvershop.ui.theme.OnSurfaceVariant
import com.market.paresolvershop.ui.theme.Primary
import com.market.paresolvershop.ui.theme.SoftGray
import com.market.paresolvershop.ui.theme.SpaceGrotesk
import com.market.paresolvershop.ui.theme.SurfaceVariant
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronRight
import compose.icons.fontawesomeicons.solid.History
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.Phone
import compose.icons.fontawesomeicons.solid.ShieldAlt
import compose.icons.fontawesomeicons.solid.Tools
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserLock
import compose.icons.fontawesomeicons.solid.UserTimes
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
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
    val viewModel = koinViewModel<ProfileViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.Success -> snackbarHostState.showSnackbar(event.message)
                is ProfileEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar tu cuenta?", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es irreversible y perderás todo tu historial de pedidos. ¿Deseas continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Eliminar para siempre", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
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

            // Avatar Section
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, SoftGray),
                color = SurfaceVariant
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.User,
                    contentDescription = null,
                    modifier = Modifier.padding(28.dp),
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

            // Main Functions Card
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

                    Spacer(Modifier.height(32.dp))

                    // Legal & Support Items
                    ActionListItem("Contact Support", FontAwesomeIcons.Solid.Phone) {}
                    ActionListItem("Privacy Policy", FontAwesomeIcons.Solid.ShieldAlt) { /* Navegar a WebView o similar */ }
                    
                    Spacer(Modifier.height(16.dp))

                    // Dangerous Actions
                    ActionListItem("Delete Account", FontAwesomeIcons.Solid.UserTimes, Error) {
                        showDeleteDialog = true
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OnSurface)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (onClick != null) 1f else 0.3f)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
fun ActionListItem(
    label: String, 
    icon: ImageVector, 
    contentColor: Color = OnSurface,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = SurfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.White) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
                }
            }
            Text(
                text = label,
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            Icon(FontAwesomeIcons.Solid.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
        }
    }
}

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
