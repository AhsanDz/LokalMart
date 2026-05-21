package com.kelompok4.lokalmart.feature.checkout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.feature.checkout.viewmodel.OrderViewModel

@Composable
fun OrderTrackingScreen(
    navController: NavController,
    buyerId: String,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val green = Color(0xFF2DB87C)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Berlangsung", "Selesai", "Dibatalkan")
    val statusMap = mapOf(
        0 to listOf("pending", "confirmed", "shipped"),
        1 to listOf("delivered"),
        2 to listOf("cancelled")
    )

    LaunchedEffect(buyerId) {
        viewModel.fetchOrders(buyerId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pesanan Saya", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Cari")
            }
        }

        // Tabs
        val filteredOrders = uiState.orders.filter {
            it.status in (statusMap[selectedTab] ?: emptyList())
        }
        val berlangsung = uiState.orders.count { it.status in listOf("pending", "confirmed", "shipped") }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = green
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(title, fontSize = 13.sp)
                            if (index == 0 && berlangsung > 0) {
                                Surface(
                                    color = green,
                                    shape = CircleShape,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$berlangsung", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders) { order ->
                    OrderCard(order = order, green = green, navController = navController)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, green: Color, navController: NavController) {
    val statusSteps = listOf("pending", "confirmed", "shipped", "delivered")
    val currentStep = statusSteps.indexOf(order.status).takeIf { it >= 0 } ?: 0
    val statusLabel = mapOf(
        "pending" to "MENUNGGU BAYAR",
        "confirmed" to "DISIAPKAN",
        "shipped" to "DIKIRIM",
        "delivered" to "SELESAI",
        "cancelled" to "DIBATALKAN"
    )
    val statusColor = mapOf(
        "pending" to Color(0xFFF59E0B),
        "confirmed" to Color(0xFF3B82F6),
        "shipped" to Color(0xFF2DB87C),
        "delivered" to Color(0xFF6B7280),
        "cancelled" to Color(0xFFEF4444)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Order ID + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ORD-${order.id.takeLast(8).uppercase()}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = (statusColor[order.status] ?: Color.Gray).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "●${statusLabel[order.status] ?: order.status}",
                        fontSize = 11.sp,
                        color = statusColor[order.status] ?: Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total harga
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusSteps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (index <= currentStep) green else Color(0xFFE5E7EB),
                                shape = CircleShape
                            )
                    )
                    if (index < statusSteps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (index < currentStep) green else Color(0xFFE5E7EB))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Bayar", "Konfirmasi", "Dikirim", "Selesai").forEach {
                    Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = { navController.navigate("order_detail/${order.id}") },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(if (order.status == "shipped") "Lacak" else "Detail", fontSize = 13.sp)
                }
            }
        }
    }
}