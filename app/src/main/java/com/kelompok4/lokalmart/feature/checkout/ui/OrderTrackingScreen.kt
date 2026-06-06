package com.kelompok4.lokalmart.feature.checkout.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import com.kelompok4.lokalmart.feature.catalog.ui.BuyerBottomNavigation
import com.kelompok4.lokalmart.feature.checkout.data.OrderWithDetailsDto
import com.kelompok4.lokalmart.feature.checkout.viewmodel.OrderViewModel

@Composable
fun OrderTrackingScreen(
    navController: NavController,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchOrders()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BuyerBottomNavigation(
                activeTab = "Pesanan",
                onTabClick = { tab ->
                    when (tab) {
                        "Beranda" -> onNavigateToHome()
                        "Cari" -> onNavigateToSearch()
                        "Keranjang" -> onNavigateToCart()
                        "Profil" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pesanan Saya",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF0F172A)
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = Color(0xFF0F172A)
                )
            }
        }

        // Tabs
        val filteredOrders = uiState.orders.filter {
            it.status in (statusMap[selectedTab] ?: emptyList())
        }
        val berlangsung = uiState.orders.count { it.status in listOf("pending", "confirmed", "shipped") }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = green
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) green else Color(0xFF64748B)
                            )
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
}

@Composable
fun OrderCard(order: OrderWithDetailsDto, green: Color, navController: NavController) {
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Order ID + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORD-${order.id.takeLast(8).uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Surface(
                    color = (statusColor[order.status] ?: Color.Gray).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "● ${statusLabel[order.status] ?: order.status}",
                        fontSize = 10.sp,
                        color = statusColor[order.status] ?: Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Info Produk Utama & Gambar
            val firstItem = order.orderItems.firstOrNull()
            val productName = firstItem?.products?.name ?: "Produk Lokal"
            val qty = firstItem?.quantity ?: 1
            val imgUrl = firstItem?.products?.productImages?.firstOrNull { it.isPrimary }?.imageUrl
                ?: firstItem?.products?.productImages?.firstOrNull()?.imageUrl
            
            val otherCount = order.orderItems.size - 1
            val displayTitle = if (otherCount > 0) {
                "$productName\n+ $otherCount lainnya"
            } else if (qty > 1) {
                "$productName ×$qty"
            } else {
                productName
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!imgUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = imgUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = green,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 18.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.stores?.name ?: "Toko Lokal",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
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
                    val isCompleted = index < currentStep
                    val isCurrent = index == currentStep
                    
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (isCompleted || isCurrent) green else Color(0xFFE2E8F0),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                    if (index < statusSteps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (index < currentStep) green else Color(0xFFE2E8F0))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Bayar", "Konfirmasi", "Dikirim", "Selesai").forEachIndexed { index, step ->
                    val isActive = index <= currentStep
                    Text(
                        text = step,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) green else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Bottom bar: Status info kurir + Tombol Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Status Kurir/Toko
                val (infoText, infoIcon) = when (order.status) {
                    "pending" -> Pair("Menunggu pembayaran", Icons.Default.Schedule)
                    "confirmed" -> Pair("Disiapkan oleh penjual", Icons.Default.Schedule)
                    "shipped" -> Pair("2 jam lagi tiba", Icons.Default.LocalShipping)
                    "delivered" -> Pair("Pesanan telah diterima", Icons.Default.CheckCircle)
                    else -> Pair("Pesanan dibatalkan", Icons.Default.Info)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = infoIcon,
                        contentDescription = null,
                        tint = if (order.status == "shipped" || order.status == "delivered") green else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = infoText,
                        fontSize = 11.sp,
                        color = if (order.status == "shipped" || order.status == "delivered") green else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Tombol aksi
                val btnText = when (order.status) {
                    "pending" -> "Bayar"
                    "shipped" -> "Lacak"
                    else -> "Detail"
                }

                Button(
                    onClick = {
                        if (order.status == "pending") {
                            navController.navigate("payment/${order.id}/qris/${order.totalPrice}")
                        } else {
                            navController.navigate("order_detail/${order.id}")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (order.status == "pending") green else Color.White,
                        contentColor = if (order.status == "pending") Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier.height(36.dp),
                    border = if (order.status == "pending") null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(text = btnText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}