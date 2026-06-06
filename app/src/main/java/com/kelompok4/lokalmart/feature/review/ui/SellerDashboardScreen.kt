package com.kelompok4.lokalmart.feature.review.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kelompok4.lokalmart.feature.review.viewmodel.DashboardViewModel
import com.kelompok4.lokalmart.feature.store.ui.SellerBottomNavigation
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMyStoreProfile: () -> Unit,
    onNavigateToSellerOrders: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    val green = Color(0xFF2DB87C)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)
    val borderCol = Color(0xFFE2E8F0)
    val cardBg = Color(0xFFFFFFFF)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAnalytics(state.currentPeriodDays)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Penjualan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            SellerBottomNavigation(
                activeTab = "laporan",
                onTabClick = { tab ->
                    when (tab) {
                        "dashboard" -> onNavigateToDashboard()
                        "produk" -> onNavigateToInventory()
                        "order" -> onNavigateToSellerOrders()
                        "toko" -> onNavigateToMyStoreProfile()
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green)
            }
        } else if (state.error != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error ?: "Gagal memuat analitik", color = textMuted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadAnalytics(state.currentPeriodDays) },
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Coba Lagi")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Selector Rentang Waktu (Chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PeriodTabChip(
                        label = "7 Hari Terakhir",
                        isSelected = state.currentPeriodDays == 7,
                        onClick = { viewModel.loadAnalytics(7) },
                        green = green,
                        modifier = Modifier.weight(1f)
                    )
                    PeriodTabChip(
                        label = "30 Hari Terakhir",
                        isSelected = state.currentPeriodDays == 30,
                        onClick = { viewModel.loadAnalytics(30) },
                        green = green,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Card Total Pendapatan Ringkasan Premium
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16A34A)), // Green banner theme
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PENDAPATAN PERIODE INI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = rupiahFormat.format(state.totalRevenue).replace("Rp", "Rp "),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total Transaksi",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${state.totalOrders} order",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Produk Terjual",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${state.totalItemsSold} barang",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 3. Card Grafik Bar Chart Kustom
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tren Penjualan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Agregasi pendapatan penjualan toko Anda",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                        Spacer(Modifier.height(24.dp))

                        // Custom drawn chart using Canvas
                        val analyticsData = state.analyticsData
                        val maxRevenue = remember(analyticsData) {
                            analyticsData.maxOfOrNull { it.totalRevenue }?.coerceAtLeast(1000.0) ?: 100000.0
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                val chartHeight = canvasHeight - 30.dp.toPx() // bottom label space
                                val barCount = analyticsData.size.coerceAtLeast(1)
                                val spacing = 12.dp.toPx()
                                val totalSpacing = spacing * (barCount + 1)
                                val barWidth = (canvasWidth - totalSpacing) / barCount

                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#64748B")
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }

                                analyticsData.forEachIndexed { idx, item ->
                                    // Calculate coordinates
                                    val x = spacing + idx * (barWidth + spacing)
                                    val heightRatio = (item.totalRevenue / maxRevenue).toFloat()
                                    val barHeight = chartHeight * heightRatio
                                    val y = chartHeight - barHeight

                                    // Draw bar with gradient
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFF22C55E), Color(0xFF15803D))
                                        ),
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )

                                    // Day of week labels underneath
                                    val date = LocalDate.parse(item.period)
                                    val dayInitial = when (date.dayOfWeek) {
                                        java.time.DayOfWeek.MONDAY -> "Sn"
                                        java.time.DayOfWeek.TUESDAY -> "Sl"
                                        java.time.DayOfWeek.WEDNESDAY -> "Rb"
                                        java.time.DayOfWeek.THURSDAY -> "Km"
                                        java.time.DayOfWeek.FRIDAY -> "Jm"
                                        java.time.DayOfWeek.SATURDAY -> "Sb"
                                        java.time.DayOfWeek.SUNDAY -> "Mg"
                                        else -> ""
                                    }
                                    
                                    val label = if (state.currentPeriodDays == 7) {
                                        dayInitial
                                    } else {
                                        // For 30 days, draw dates selectively to avoid cluttering
                                        if (idx % 5 == 0) date.dayOfMonth.toString() else ""
                                    }

                                    drawContext.canvas.nativeCanvas.drawText(
                                        label,
                                        x + barWidth / 2,
                                        canvasHeight - 6.dp.toPx(),
                                        textPaint
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Card Breakdown Kinerja Produk
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Rincian Performa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rata-rata Pendapatan / Hari", fontSize = 13.sp, color = textMuted)
                            val dailyAvg = state.totalRevenue / state.currentPeriodDays
                            Text(
                                text = rupiahFormat.format(dailyAvg).replace("Rp", "Rp "),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = borderCol)
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rata-rata Order / Hari", fontSize = 13.sp, color = textMuted)
                            val orderAvg = state.totalOrders.toFloat() / state.currentPeriodDays.toFloat()
                            Text(
                                text = String.format(Locale.US, "%.1f order", orderAvg),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = borderCol)
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rata-rata Barang Terjual / Hari", fontSize = 13.sp, color = textMuted)
                            val itemAvg = state.totalItemsSold.toFloat() / state.currentPeriodDays.toFloat()
                            Text(
                                text = String.format(Locale.US, "%.1f barang", itemAvg),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    green: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) green.copy(alpha = 0.12f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) green else Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) green else Color(0xFF475569)
            )
        }
    }
}
