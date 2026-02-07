package com.market.paresolvershop.ui.orders

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.market.paresolvershop.ui.orders.components.OrderCard
import com.market.paresolvershop.ui.profile.ProfileUiState
import com.market.paresolvershop.ui.profile.ProfileViewModel
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.BoxOpen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

object OrderHistoryScreen : Screen {

    @OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<OrderHistoryViewModel>()
        val profileViewModel = koinViewModel<ProfileViewModel>()
        
        val uiState by viewModel.uiState.collectAsState()
        val profileState by profileViewModel.uiState.collectAsState()
        
        val isAdmin = (profileState as? ProfileUiState.Authenticated)?.isAdmin ?: false

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("My Orders", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
                when (val state = uiState) {
                    is OrderHistoryUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                    is OrderHistoryUiState.Error -> Text(state.message, color = Error, modifier = Modifier.align(Alignment.Center))
                    is OrderHistoryUiState.Success -> {
                        if (state.orders.isEmpty()) {
                            EmptyOrdersView()
                        } else {
                            val groupedOrders = state.orders.groupBy { it.createdAt?.take(10) ?: "Unknown" }

                            LazyColumn(
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                groupedOrders.forEach { (date, ordersInDate) ->
                                    stickyHeader {
                                        DateHeader(date)
                                    }
                                    
                                    items(ordersInDate) { order ->
                                        OrderCard(
                                            order = order,
                                            isAdmin = isAdmin,
                                            onClick = { 
                                                order.id?.let { id ->
                                                    navigator.push(OrderDetailScreen(id))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = date.replace("-", " / "),
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun EmptyOrdersView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(FontAwesomeIcons.Solid.BoxOpen, null, modifier = Modifier.size(80.dp), tint = SoftGray)
        Spacer(Modifier.height(16.dp))
        Text("No orders yet", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Your previous orders will appear here.", color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}
