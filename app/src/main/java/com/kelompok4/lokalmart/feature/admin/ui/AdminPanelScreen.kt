package com.kelompok4.lokalmart.feature.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kelompok4.lokalmart.feature.admin.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStoreVerification: () -> Unit,
    onNavigateToProductModeration: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    val red = Color(0xFFEF4444)
    val green = Color(0xFF16A34A)
    val blue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)
    val borderCol = Color(0xFFE2E8F0)
    val cardBg = Color(0xFFFFFFFF)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPendingStores()
                viewModel.loadProducts()
                viewModel.loadUsers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Kontrol Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header
            Text(
                text = "Selamat Datang, Admin 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = textPrimary
            )
            Text(
                text = "Kelola dan moderasi ekosistem platform LokalMart secara real-time.",
                fontSize = 12.sp,
                color = textMuted
            )

            Spacer(Modifier.height(4.dp))

            // 1. Dashboard Stat Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatItem(
                    label = "Antrean Toko",
                    count = state.pendingStores.size.toString(),
                    color = Color(0xFFF59E0B),
                    icon = Icons.Default.NewReleases,
                    modifier = Modifier.weight(1f)
                )
                AdminStatItem(
                    label = "Total Produk",
                    count = state.products.size.toString(),
                    color = green,
                    icon = Icons.Default.Category,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatItem(
                    label = "Total Akun",
                    count = state.users.size.toString(),
                    color = blue,
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f)
                )
                AdminStatItem(
                    label = "Toko Aktif",
                    count = state.products.map { it.storeId }.distinct().size.toString(),
                    color = Color(0xFF8B5CF6),
                    icon = Icons.Default.Store,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Menu Moderasi Utama",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary
            )

            // 2. Navigation Actions Cards
            AdminMenuCard(
                title = "Verifikasi Toko Baru",
                subtitle = "Tinjau dan setujui pendaftaran toko UMKM baru (${state.pendingStores.size} menunggu)",
                icon = Icons.Default.Store,
                iconColor = Color(0xFFF59E0B),
                onClick = onNavigateToStoreVerification,
                borderCol = borderCol,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            AdminMenuCard(
                title = "Moderasi Produk",
                subtitle = "Tinjau semua produk di platform dan tangguhkan produk melanggar",
                icon = Icons.Default.Gavel,
                iconColor = red,
                onClick = onNavigateToProductModeration,
                borderCol = borderCol,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            AdminMenuCard(
                title = "Manajemen Akun Pengguna",
                subtitle = "Kelola keaktifan akun pembeli dan penjual di LokalMart",
                icon = Icons.Default.Block,
                iconColor = blue,
                onClick = onNavigateToUserManagement,
                borderCol = borderCol,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted
            )
        }
    }
}

@Composable
private fun AdminStatItem(
    label: String,
    count: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Spacer(Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    borderCol: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textMuted,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
