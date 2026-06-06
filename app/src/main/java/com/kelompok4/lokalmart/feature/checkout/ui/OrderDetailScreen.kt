package com.kelompok4.lokalmart.feature.checkout.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.feature.checkout.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import com.kelompok4.lokalmart.feature.review.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onNavigateToReviewForm: (String, String) -> Unit = { _, _ -> },
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewViewModel: ReviewViewModel = hiltViewModel()
    val reviewState by reviewViewModel.uiState.collectAsState()
    
    val green = Color(0xFF2DB87C)
    val divider = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchOrderDetail(orderId)
                reviewViewModel.fetchReviewedProductIds(orderId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Pesanan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = "Hubungi CS", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = green,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.errorMessage ?: "Terjadi kesalahan", color = textMuted, fontSize = 14.sp)
                }
            } else {
                uiState.selectedOrder?.let { order ->
                    val statusSteps = listOf("pending", "confirmed", "shipped", "delivered")
                    val currentStep = statusSteps.indexOf(order.status).takeIf { it >= 0 } ?: 0

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 80.dp) // padding for bottom bar buttons
                    ) {
                        
                        // ── 1. Banner Status Pengiriman Biru ───────────────────
                        val bannerBg = Color(0xFFEFF6FF)
                        val bannerCircle = Color(0xFF3B82F6)
                        
                        val bannerTitle = when (order.status) {
                            "pending" -> "Menunggu Pembayaran"
                            "confirmed" -> "Pesanan Dikonfirmasi"
                            "shipped" -> "Dalam Pengiriman"
                            "delivered" -> "Pesanan Selesai"
                            else -> "Pesanan Dibatalkan"
                        }
                        
                        val bannerSubtitle = when (order.status) {
                            "pending" -> "Selesaikan transaksi Anda segera"
                            "confirmed" -> "Penjual sedang menyiapkan barang Anda"
                            "shipped" -> "Estimasi tiba: ${formatTimelineDate(order.createdAt, 2880)}"
                            "delivered" -> "Tiba pada: ${formatTimelineDate(order.createdAt, 2880)}"
                            else -> "Pesanan ini dibatalkan"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bannerBg)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(bannerCircle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (order.status) {
                                                "pending" -> Icons.Default.Payment
                                                "confirmed" -> Icons.Default.Store
                                                "shipped" -> Icons.Default.LocalShipping
                                                "delivered" -> Icons.Default.CheckCircle
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = bannerTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = bannerSubtitle,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                }

                                if (order.status == "shipped" || order.status == "delivered") {
                                    Spacer(Modifier.height(12.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "LML-REG-${order.id.takeLast(8).uppercase()}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = "Lacak →",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = green,
                                                modifier = Modifier.clickable { }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            // ── 2. Riwayat Status (Vertical Timeline) ─────────────
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Riwayat Status",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    val timelineData = mutableListOf<TimelineItem>()
                                    
                                    // 1. Pesanan Dibuat
                                    timelineData.add(
                                        TimelineItem(
                                            title = "Pesanan Dibuat",
                                            subtitle = "Menunggu pembayaran",
                                            time = formatTimelineDate(order.createdAt, 0),
                                            isActive = true
                                        )
                                    )
                                    
                                    // 2. Pembayaran Berhasil
                                    timelineData.add(
                                        TimelineItem(
                                            title = "Pembayaran Berhasil",
                                            subtitle = "${order.payments?.method?.replace("_", " ")?.uppercase() ?: "QRIS"} · Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                                            time = formatTimelineDate(order.createdAt, 4),
                                            isActive = currentStep >= 1
                                        )
                                    )

                                    // 3. Pesanan Dikonfirmasi
                                    timelineData.add(
                                        TimelineItem(
                                            title = "Pesanan Dikonfirmasi",
                                            subtitle = "Pesanan diterima penjual",
                                            time = formatTimelineDate(order.createdAt, 34),
                                            isActive = currentStep >= 1
                                        )
                                    )

                                    // 4. Pesanan Dikemas
                                    timelineData.add(
                                        TimelineItem(
                                            title = "Pesanan Dikemas",
                                            subtitle = "Penjual telah mengemas pesanan",
                                            time = formatTimelineDate(order.createdAt, 300),
                                            isActive = currentStep >= 2
                                        )
                                    )

                                    // 5. Dalam Pengiriman
                                    timelineData.add(
                                        TimelineItem(
                                            title = "Dalam Pengiriman",
                                            subtitle = "Paket di hub kurir",
                                            time = formatTimelineDate(order.createdAt, 1320),
                                            isActive = currentStep >= 2
                                        )
                                    )

                                    // 6. Pesanan Selesai
                                    timelineData.add(
                                        TimelineItem(
                                            title = "Pesanan Selesai",
                                            subtitle = "Pesanan telah diterima",
                                            time = formatTimelineDate(order.createdAt, 2880),
                                            isActive = currentStep >= 3
                                        )
                                    )

                                    // Render timeline
                                    val activeItems = timelineData.filter { it.isActive }.reversed()
                                    
                                    activeItems.forEachIndexed { idx, item ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(24.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .background(if (idx == 0) green else Color(0xFFCBD5E1), CircleShape)
                                                )
                                                if (idx < activeItems.lastIndex) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(44.dp)
                                                            .background(Color(0xFFE2E8F0))
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (idx == 0) green else textPrimary
                                                    )
                                                    Text(
                                                        text = item.time,
                                                        fontSize = 11.sp,
                                                        color = textMuted
                                                    )
                                                }
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = item.subtitle,
                                                    fontSize = 12.sp,
                                                    color = textMuted
                                                )
                                                Spacer(Modifier.height(14.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // ── 3. Store Card ─────────────────────────────────────
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = green,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = order.stores?.name ?: "Toko Lokal",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = textPrimary
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(Icons.Default.Verified, contentDescription = null, tint = green, modifier = Modifier.size(8.dp))
                                                    Spacer(Modifier.width(2.dp))
                                                    Text("Verified", color = green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = order.stores?.address ?: "Malang, Jawa Timur",
                                            fontSize = 11.sp,
                                            color = textMuted
                                        )
                                    }
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Kunjungi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // ── 4. Produk yang Dipesan ────────────────────────────
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Produk Dipesan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = divider)
                                    Spacer(Modifier.height(12.dp))

                                    uiState.selectedOrderItems.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val imgUrl = item.products?.productImages?.firstOrNull { it.isPrimary }?.imageUrl
                                                ?: item.products?.productImages?.firstOrNull()?.imageUrl
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFF1F5F9))
                                            ) {
                                                if (!imgUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = imgUrl,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.products?.name ?: "Produk Lokal",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = textPrimary,
                                                    maxLines = 1
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "Varian Default · x${item.quantity}",
                                                    fontSize = 11.sp,
                                                    color = textMuted
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Rp ${"%,.0f".format(item.priceAtOrder * item.quantity).replace(",", ".")}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = textPrimary
                                                )
                                                val orderDetail = uiState.selectedOrder
                                                if (orderDetail != null && orderDetail.status == "delivered") {
                                                    Spacer(Modifier.height(6.dp))
                                                    val hasReviewed = reviewState.reviewedProductIds.contains(item.productId)
                                                    if (hasReviewed) {
                                                        Text(
                                                            text = "Sudah Diulas",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = textMuted
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Tulis Ulasan",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = green,
                                                            modifier = Modifier
                                                                .clickable {
                                                                    onNavigateToReviewForm(orderId, item.productId)
                                                                }
                                                                .border(1.dp, green, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (index < uiState.selectedOrderItems.lastIndex) {
                                            HorizontalDivider(color = divider, modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }

                            // ── 5. Alamat Pengiriman Kurir ────────────────────────
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = green, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Alamat Pengiriman", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = divider)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Sari Wulandari · 0812-3456-7890",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = order.shippingAddress,
                                        fontSize = 12.sp,
                                        color = textMuted,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // ── 6. Ringkasan Pembayaran Detail ────────────────────
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Ringkasan Pembayaran", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = divider)
                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Subtotal", fontSize = 12.sp, color = textMuted)
                                        Text("Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}", fontSize = 12.sp, color = textPrimary)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Ongkos kirim (JNE)", fontSize = 12.sp, color = textMuted)
                                        Text("Rp 12.000", fontSize = 12.sp, color = textPrimary)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Diskon GRATIS ONGKIR", fontSize = 12.sp, color = green)
                                        Text("-Rp 12.000", fontSize = 12.sp, color = green)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = divider)
                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                        Text(
                                            "Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = green
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Metode", fontSize = 12.sp, color = textMuted)
                                        Text(
                                            text = order.payments?.method?.replace("_", " ")?.uppercase() ?: "QRIS",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 7. Sticky Bottom Action Buttons ───────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = textPrimary
                                ),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Hubungi CS", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            if (order.status == "shipped") {
                                Button(
                                    onClick = { viewModel.confirmOrderReceipt(order.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = green,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Konfirmasi Terima", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TimelineItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val isActive: Boolean
)

fun formatTimelineDate(createdAt: String?, minutesOffset: Int): String {
    if (createdAt == null) return "-"
    return try {
        val cleanInput = createdAt.replace("T", " ").substringBefore(".")
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = parser.parse(cleanInput) ?: return "-"
        
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, minutesOffset)
        
        val formatter = SimpleDateFormat("dd MMM · HH:mm", Locale("id", "ID"))
        formatter.format(calendar.time)
    } catch (e: Exception) {
        createdAt
    }
}
