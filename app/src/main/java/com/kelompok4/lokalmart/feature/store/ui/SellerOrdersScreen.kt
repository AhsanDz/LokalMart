package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.feature.store.data.StoreOrderDto
import com.kelompok4.lokalmart.feature.store.viewmodel.SellerOrdersViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMyStoreProfile: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    viewModel: SellerOrdersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("semua") } // semua, baru, dikirim, selesai, batal

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadOrders()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Filter orders locally
    val filteredOrders = when (selectedTab) {
        "baru" -> state.orders.filter { it.status == "confirmed" || it.status == "pending" }
        "dikirim" -> state.orders.filter { it.status == "shipped" }
        "selesai" -> state.orders.filter { it.status == "delivered" }
        "batal" -> state.orders.filter { it.status == "cancelled" }
        else -> state.orders
    }

    Scaffold(
        containerColor = GrayBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kelola Pesanan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.background(Color.White)
            )
        },
        bottomBar = {
            SellerBottomNavigation(
                activeTab = "order",
                onTabClick = { tab ->
                    when (tab) {
                        "dashboard" -> onNavigateToDashboard()
                        "produk" -> onNavigateToInventory()
                        "laporan" -> onNavigateToReports()
                        "toko" -> onNavigateToMyStoreProfile()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Tabs: Semua, Baru, Dikirim, Selesai, Batal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val countSemua = state.orders.size
                    val countBaru = state.orders.count { it.status == "confirmed" || it.status == "pending" }
                    val countDikirim = state.orders.count { it.status == "shipped" }
                    val countSelesai = state.orders.count { it.status == "delivered" }
                    val countBatal = state.orders.count { it.status == "cancelled" }

                    OrderTabItem(label = "Semua ($countSemua)", isActive = selectedTab == "semua", onClick = { selectedTab = "semua" }, modifier = Modifier.weight(1f))
                    OrderTabItem(label = "Baru ($countBaru)", isActive = selectedTab == "baru", onClick = { selectedTab = "baru" }, modifier = Modifier.weight(1f))
                    OrderTabItem(label = "Kirim ($countDikirim)", isActive = selectedTab == "dikirim", onClick = { selectedTab = "dikirim" }, modifier = Modifier.weight(1f))
                    OrderTabItem(label = "Selesai ($countSelesai)", isActive = selectedTab == "selesai", onClick = { selectedTab = "selesai" }, modifier = Modifier.weight(1f))
                    OrderTabItem(label = "Batal ($countBatal)", isActive = selectedTab == "batal", onClick = { selectedTab = "batal" }, modifier = Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        Text(
                            text = state.error ?: "Gagal memuat pesanan",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadOrders() },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                } else if (filteredOrders.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Tidak Ada Transaksi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
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

                            SellerOrderRowItem(
                                orderId = "ORD-${order.id.takeLast(8).uppercase()}",
                                status = order.status,
                                name = buyerName,
                                avatarInitials = initials,
                                itemsSummary = itemSummary,
                                totalPrice = orderPriceFormatted,
                                showActions = order.status == "confirmed" || order.status == "pending",
                                onConfirm = { viewModel.confirmOrder(order.id) },
                                onReject = { viewModel.rejectOrder(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderTabItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) Color.White else Color.Transparent
    val tc = if (isActive) TextPrimary else TextSecondary
    val fw = if (isActive) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = fw,
            color = tc,
            maxLines = 1
        )
    }
}

@Composable
private fun SellerOrderRowItem(
    orderId: String,
    status: String,
    name: String,
    avatarInitials: String,
    itemsSummary: String,
    totalPrice: String,
    showActions: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                
                val statusText = when (status) {
                    "pending" -> "MENUNGGU BAYAR"
                    "confirmed" -> "BARU"
                    "shipped" -> "DIKIRIM"
                    "delivered" -> "SELESAI"
                    "cancelled" -> "BATAL"
                    else -> status.uppercase()
                }
                
                val badgeBg = when (status) {
                    "confirmed", "pending" -> Color(0xFFFEF3C7)
                    "shipped" -> Color(0xFFDBEAFE)
                    "delivered" -> Color(0xFFDCFCE7)
                    else -> Color(0xFFFEE2E2)
                }
                
                val badgeText = when (status) {
                    "confirmed", "pending" -> Color(0xFFD97706)
                    "shipped" -> Color(0xFF2563EB)
                    "delivered" -> Color(0xFF16A34A)
                    else -> Color(0xFFEF4444)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "● $statusText",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = totalPrice,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            if (showActions) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
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
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(
                            text = "Konfirmasi →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
