package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.core.common.theme.*
import com.kelompok4.lokalmart.feature.store.viewmodel.MyStoreViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val CardBg         = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE2E8F0)
private val GrayBg         = Color(0xFFF8FAFC)

@Composable
fun MyStoreScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToEditStore: (String) -> Unit,
    onNavigateToPublicProfile: (String) -> Unit,
    onNavigateToMyStoreProfile: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToSellerOrders: () -> Unit = {},
    onNavigateToSellerChat: () -> Unit = {},
    viewModel: MyStoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadMyStore()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = GrayBg,
        bottomBar = {
            SellerBottomNavigation(
                activeTab = "dashboard",
                onTabClick = { tab ->
                    when (tab) {
                        "produk" -> onNavigateToInventory()
                        "order" -> onNavigateToSellerOrders()
                        "laporan" -> onNavigateToReports()
                        "toko" -> onNavigateToMyStoreProfile()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "Gagal memuat data toko",
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kembali Ke Profil")
                    }
                }
            } else {
                state.store?.let { store ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Header (Toko Saya & Nama Toko + Back Button)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, BorderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Toko Saya",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = store.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Notification Bell Icon
                            IconButton(
                                onClick = { /* TODO */ },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Status Warning (if pending)
                        if (store.status == "pending") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Toko sedang menunggu verifikasi oleh Admin (1-2 hari kerja). Selama menunggu, Anda sudah dapat mengelola produk & stok.",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        // 2. Revenue Card (Pendapatan Hari Ini)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF15803D), Color(0xFF16A34A))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    text = "PENDAPATAN HARI INI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(4.dp))
                                val formattedRevenue = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).apply {
                                    maximumFractionDigits = 0
                                }.format(state.todayRevenue).replace("Rp", "Rp ")
                                
                                Text(
                                    text = formattedRevenue,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "↗ +18% dibanding kemarin",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 3. Stats Row (Order baru, Pesan, Produk aktif)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DashboardStatCard(
                                title = "Order baru",
                                count = state.newOrdersCount.toString(),
                                badgeText = "Baru",
                                isBadgePositive = true,
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatCard(
                                title = "Pesan",
                                count = "5",
                                badgeText = "2 baru",
                                isBadgePositive = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToSellerChat() }
                            )
                            DashboardStatCard(
                                title = "Produk aktif",
                                count = state.activeProductsCount.toString(),
                                badgeText = "Aktif",
                                isBadgePositive = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // 4. Penjualan 7 hari chart
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Penjualan 7 hari",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Detail →",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary,
                                        modifier = Modifier.clickable { onNavigateToReports() }
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                // Simple Bar Chart
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val salesData = listOf(
                                        "Sn" to 0.4f,
                                        "Sl" to 0.6f,
                                        "Rb" to 0.5f,
                                        "Km" to 0.8f,
                                        "Jm" to 0.7f,
                                        "Sb" to 1.0f,
                                        "Mg" to 0.9f
                                    )

                                    salesData.forEach { (day, value) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            val isWeekend = day == "Sb" || day == "Mg"
                                            val barColor = if (isWeekend) Color(0xFF16A34A) else Color(0xFFDCFCE7)
                                            Box(
                                                modifier = Modifier
                                                    .width(26.dp)
                                                    .height((70 * value).dp)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(barColor)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = day,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // 5. Order Baru List Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order baru",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Lihat semua (${state.newOrders.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary,
                                    modifier = Modifier.clickable { onNavigateToSellerOrders() }
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            if (state.newOrders.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBg),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Belum ada order baru masuk",
                                            color = TextSecondary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                state.newOrders.forEach { order ->
                                    val buyerName = order.profiles?.fullName ?: "Pembeli"
                                    val initials = buyerName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                                    
                                    val itemSummary = if (order.orderItems.isNotEmpty()) {
                                        val firstItem = order.orderItems.first()
                                        val productName = firstItem.products?.name ?: "Produk"
                                        val qty = firstItem.quantity
                                        val otherCount = order.orderItems.size - 1
                                        if (otherCount > 0) {
                                            "$productName x$qty +$otherCount produk lainnya"
                                        } else {
                                            "$productName x$qty"
                                        }
                                    } else {
                                        "Tidak ada rincian item"
                                    }
                                    
                                    val orderPriceFormatted = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).apply {
                                        maximumFractionDigits = 0
                                    }.format(order.totalPrice).replace("Rp", "Rp ")

                                    OrderDashboardItem(
                                        orderId = "ORD-${order.id.takeLast(8).uppercase()}",
                                        status = if (order.status == "pending") "MENUNGGU BAYAR" else "BARU",
                                        name = buyerName,
                                        avatarInitials = initials,
                                        itemsSummary = itemSummary,
                                        totalPrice = orderPriceFormatted,
                                        showActions = order.status == "confirmed",
                                        onConfirmClick = { viewModel.confirmOrder(order.id) },
                                        onRejectClick = { viewModel.rejectOrder(order.id) }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    count: String,
    badgeText: String,
    isBadgePositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isBadgePositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBadgePositive) Color(0xFF15803D) else Color(0xFFB91C1C)
                )
            }
        }
    }
}

@Composable
private fun OrderDashboardItem(
    orderId: String,
    status: String,
    name: String,
    avatarInitials: String,
    itemsSummary: String,
    totalPrice: String,
    showActions: Boolean,
    onConfirmClick: () -> Unit = {},
    onRejectClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: Order ID and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = orderId,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                val badgeBg = if (status == "BARU") Color(0xFFFEF3C7) else Color(0xFFDBEAFE)
                val badgeText = if (status == "BARU") Color(0xFFD97706) else Color(0xFF2563EB)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "● $status",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Middle row: Customer info & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitials,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = itemsSummary,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = totalPrice,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            // Bottom Actions (if applicable)
            if (showActions) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Tolak",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onConfirmClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(
                            text = "Konfirmasi →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerBottomNavigation(
    activeTab: String,
    onTabClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SellerBottomNavItem(
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                isActive = activeTab == "dashboard",
                onClick = { onTabClick("dashboard") }
            )
            SellerBottomNavItem(
                icon = Icons.Default.Inventory,
                label = "Produk",
                isActive = activeTab == "produk",
                onClick = { onTabClick("produk") }
            )
            SellerBottomNavItem(
                icon = Icons.Default.ListAlt,
                label = "Order",
                isActive = activeTab == "order",
                onClick = { onTabClick("order") }
            )
            SellerBottomNavItem(
                icon = Icons.Default.BarChart,
                label = "Laporan",
                isActive = activeTab == "laporan",
                onClick = { onTabClick("laporan") }
            )
            SellerBottomNavItem(
                icon = Icons.Default.Storefront,
                label = "Toko",
                isActive = activeTab == "toko",
                onClick = { onTabClick("toko") }
            )
        }
    }
}

@Composable
private fun SellerBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) GreenPrimary else Color(0xFF94A3B8),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) GreenPrimary else Color(0xFF94A3B8)
        )
    }
}
