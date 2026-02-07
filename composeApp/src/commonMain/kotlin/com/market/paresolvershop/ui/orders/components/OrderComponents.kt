package com.market.paresolvershop.ui.orders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.market.paresolvershop.domain.model.Order
import com.market.paresolvershop.ui.theme.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    isAdmin: Boolean,
    onStatusClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.id?.take(8)}",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    
                    // CORRECCIÓN: Mostrar nombre del cliente solo si es Admin y NO es null
                    if (isAdmin && order.customerName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(FontAwesomeIcons.Solid.UserCircle, null, modifier = Modifier.size(14.dp), tint = Primary)
                            Text(
                                text = " Customer: ${order.customerName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(FontAwesomeIcons.Solid.MapMarkerAlt, null, modifier = Modifier.size(12.dp), tint = if (isAdmin) OnSurfaceVariant else Primary)
                        Text(
                            text = " ${if (isAdmin) "Delivery to: " else ""}${order.fullRecipientName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                StatusBadge(
                    status = order.status,
                    onClick = onStatusClick
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (isAdmin && order.createdAt != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Icon(FontAwesomeIcons.Solid.Calendar, null, modifier = Modifier.size(12.dp), tint = OnSurfaceVariant)
                            Text(" ${order.createdAt.take(10)}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(
                        text = "$${order.totalAmount}",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        fontSize = 17.sp
                    )
                }

                if (isAdmin && onStatusClick != null) {
                    TextButton(onClick = onStatusClick) {
                        Text("Change Status", style = MaterialTheme.typography.labelLarge, color = Primary)
                    }
                } else {
                    Button(
                        onClick = onClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant, contentColor = OnSurface)
                    ) {
                        Text("Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    onClick: (() -> Unit)? = null
) {
    val color = getStatusColor(status)
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = status.uppercase(),
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            if (onClick != null) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp).padding(start = 4.dp),
                    tint = color
                )
            }
        }
    }
}

@Composable
fun OrderProductItemRow(
    name: String,
    imageUrl: String?,
    category: String,
    quantity: Int,
    price: Double? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(category, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("x$quantity", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (price != null) {
                Text("$$price", style = MaterialTheme.typography.labelSmall, color = Primary)
            }
        }
    }
}

fun getStatusColor(status: String): Color = when (status.lowercase()) {
    "delivered" -> Success
    "shipped" -> Color(0xFF2196F3)
    "pending" -> Color(0xFFFFA000)
    "cancelled" -> Error
    else -> Primary
}
