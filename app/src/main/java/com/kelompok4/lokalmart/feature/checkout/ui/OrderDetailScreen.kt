package com.kelompok4.lokalmart.feature.checkout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.feature.checkout.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onReviewClick: (orderId: String, productId: String) -> Unit = { _, _ -> },
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetail(orderId)
    }

    val order = uiState.selectedOrder

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pesanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = green)
            }
        } else if (order != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Status Card ──
                StatusCard(order = order, green = green, textPrimary = textPrimary, textMuted = textMuted)

                // ── Order Info Card ──
                OrderInfoCard(order = order, textPrimary = textPrimary, textMuted = textMuted)

                // ── Shipping Address Card ──
                ShippingCard(order = order, textPrimary = textPrimary, textMuted = textMuted)

                // ── Payment Card ──
                PaymentCard(order = order, green = green, textPrimary = textPrimary, textMuted = textMuted)

                // ── Action Buttons ──
                if (order.status == "delivered") {
                    Button(
                        onClick = { onReviewClick(order.id, "") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Beri Ulasan", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (order.status == "pending") {
                    OutlinedButton(
                        onClick = { /* cancel order */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                    ) {
                        Text("Batalkan Pesanan", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gagal memuat detail pesanan", color = textMuted)
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.errorMessage ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusCard(order: Order, green: Color, textPrimary: Color, textMuted: Color) {
    val statusSteps = listOf("pending", "confirmed", "shipped", "delivered")
    val currentStep = statusSteps.indexOf(order.status).takeIf { it >= 0 } ?: 0
    val isCancelled = order.status == "cancelled"

    val statusLabel = mapOf(
        "pending" to "Menunggu Pembayaran",
        "confirmed" to "Pesanan Dikonfirmasi",
        "shipped" to "Dalam Pengiriman",
        "delivered" to "Pesanan Selesai",
        "cancelled" to "Pesanan Dibatalkan"
    )
    val statusColor = mapOf(
        "pending" to Color(0xFFF59E0B),
        "confirmed" to Color(0xFF3B82F6),
        "shipped" to green,
        "delivered" to Color(0xFF6B7280),
        "cancelled" to Color(0xFFDC2626)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = statusColor[order.status] ?: Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    statusLabel[order.status] ?: order.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = statusColor[order.status] ?: Color.Gray
                )
            }

            if (!isCancelled) {
                Spacer(Modifier.height(16.dp))

                // Progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statusSteps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
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

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Bayar", "Konfirmasi", "Kirim", "Selesai").forEach {
                        Text(it, fontSize = 10.sp, color = textMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderInfoCard(order: Order, textPrimary: Color, textMuted: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Informasi Pesanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrimary
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(12.dp))

            InfoRow("ID Pesanan", "ORD-${order.id.takeLast(8).uppercase()}", textPrimary, textMuted)
            Spacer(Modifier.height(8.dp))
            InfoRow(
                "Total Pembayaran",
                "Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                textPrimary,
                textMuted,
                valueBold = true
            )
            if (order.createdAt != null) {
                Spacer(Modifier.height(8.dp))
                InfoRow(
                    "Tanggal Pesan",
                    order.createdAt.take(10),
                    textPrimary,
                    textMuted
                )
            }
        }
    }
}

@Composable
private fun ShippingCard(order: Order, textPrimary: Color, textMuted: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Alamat Pengiriman",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                order.shippingAddress.ifEmpty { "Alamat belum diisi" },
                fontSize = 13.sp,
                color = textMuted,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PaymentCard(order: Order, green: Color, textPrimary: Color, textMuted: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Rincian Pembayaran",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textPrimary
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(12.dp))

            InfoRow(
                "Subtotal Produk",
                "Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                textPrimary,
                textMuted
            )
            Spacer(Modifier.height(6.dp))
            InfoRow("Ongkos Kirim", "Gratis", textPrimary, textMuted)
            Spacer(Modifier.height(6.dp))
            InfoRow("Biaya Layanan", "Rp 0", textPrimary, textMuted)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(12.dp))

            InfoRow(
                "Total",
                "Rp ${"%,.0f".format(order.totalPrice).replace(",", ".")}",
                textPrimary,
                green,
                valueBold = true,
                valueSize = 16
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    textPrimary: Color,
    textMuted: Color,
    valueBold: Boolean = false,
    valueSize: Int = 13
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = textMuted)
        Text(
            value,
            fontSize = valueSize.sp,
            color = textPrimary,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
