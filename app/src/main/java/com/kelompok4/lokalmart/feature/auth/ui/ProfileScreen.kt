package com.kelompok4.lokalmart.feature.auth.ui

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.data.model.UserRole
import com.kelompok4.lokalmart.feature.auth.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToPesanan: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.profileState.collectAsState()
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            ProfileBottomNavBar(
                selectedIndex = 4, // Profile tab aktif
                onHomeClick = onNavigateToHome,
                onSearchClick = onNavigateToSearch,
                onCartClick = onNavigateToCart,
                onPesananClick = onNavigateToPesanan,
                onProfileClick = { /* sudah di profile */ }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = green)
                }
            }
            state.error != null && state.user == null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        state.error ?: "Terjadi kesalahan",
                        color = textMuted,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                val user = state.user ?: return@Scaffold
                val initials = user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── Scrollable Body ──
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Green gradient header block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF16A34A), Color(0xFF15803D))
                                    )
                                )
                                .statusBarsPadding()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Title and edit pencil row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Profil Saya",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable { onEditProfile() }
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Profil",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Avatar circle with initials
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .border(2.dp, Color.White, CircleShape)
                                ) {
                                    Text(
                                        text = initials.ifBlank { "U" },
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Name and email
                                Text(
                                    text = user.fullName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = user.email,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )

                                Spacer(Modifier.height(10.dp))

                                // Role Badges Row
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Buyer pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            "BUYER",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }

                                    // Seller/Admin pill if applicable
                                    if (user.role == UserRole.SELLER || user.role == UserRole.ADMIN) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .padding(horizontal = 12.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                user.role.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Space for the overlapping stats card
                        Spacer(Modifier.height(50.dp))

                        // ── Store Info Card (for seller) ──
                        if (user.role == UserRole.SELLER) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0FDF4))
                                        .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                                        .clickable { onNavigateToStore() }
                                        .padding(14.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCFCE7))
                                    ) {
                                        Icon(
                                            Icons.Default.Store,
                                            contentDescription = null,
                                            tint = green,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Toko Saya: Kriya Sari Craft",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textPrimary
                                        )
                                        Text(
                                            "3 order baru · Rp 420.000 hari ini",
                                            fontSize = 11.sp,
                                            color = textMuted
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = green,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        } else if (user.role == UserRole.ADMIN) {
                            // Admin Panel navigation shortcut card
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                                        .clickable { onNavigateToAdmin() }
                                        .padding(14.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDBEAFE))
                                    ) {
                                        Icon(
                                            Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Dashboard Admin Panel",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Text(
                                            "Kelola verifikasi toko & moderasi produk",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1D4ED8)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF1D4ED8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        } else {
                            // Buyer register store prompt
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFFBEB))
                                        .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                                        .clickable { onNavigateToStore() }
                                        .padding(14.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7))
                                    ) {
                                        Icon(
                                            Icons.Default.Store,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Mulai Berjualan?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF78350F)
                                        )
                                        Text(
                                            "Buka toko gratis dan jangkau pembeli terdekat",
                                            fontSize = 11.sp,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        // ── Menu List Items ──
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column {
                                ProfileMenuItem(
                                    icon = Icons.Outlined.Person,
                                    title = "Edit Profil & Foto",
                                    onClick = onEditProfile
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                ProfileMenuItem(
                                    icon = Icons.Outlined.LocationOn,
                                    title = "Alamat Tersimpan",
                                    onClick = { /* navigate to address screen */ }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                ProfileMenuItem(
                                    icon = Icons.Outlined.Payment,
                                    title = "Metode Pembayaran",
                                    onClick = { /* navigate to pay method screen */ }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                ProfileMenuItem(
                                    icon = Icons.Outlined.Notifications,
                                    title = "Notifikasi",
                                    onClick = { /* navigate to notification screen */ }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Logout button
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.signOut()
                                        onLogout()
                                    }
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Keluar",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Keluar dari Akun",
                                    color = Color(0xFFEF4444),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                    }

                    // ── Overlapping Stats Card ──
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 210.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(84.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(number = "12", label = "Pesanan")
                            VerticalDivider()
                            StatItem(number = "8", label = "Ulasan")
                            VerticalDivider()
                            StatItem(number = "24", label = "Wishlist")
                        }
                    }
                }
            }
        }
    }
}

// Stats Card Sub-composables
@Composable
private fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color(0xFF0F172A)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color(0xFFE2E8F0))
    )
}

// Menu list sub-composable
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp)
        )
    }
}

// 5-item Bottom navigation bar
@Composable
private fun ProfileBottomNavBar(
    selectedIndex: Int,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onPesananClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val tabs = listOf(
        Triple("Beranda", Icons.Outlined.Home, Icons.Filled.Home),
        Triple("Cari", Icons.Outlined.Search, Icons.Filled.Search),
        Triple("Keranjang", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
        Triple("Pesanan", Icons.Outlined.Description, Icons.Outlined.Description),
        Triple("Profil", Icons.Outlined.Person, Icons.Filled.Person)
    )
    val callbacks = listOf(onHomeClick, onSearchClick, onCartClick, onPesananClick, onProfileClick)

    Column {
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, (label, outlinedIcon, filledIcon) ->
                val isSelected = index == selectedIndex
                val icon = if (isSelected) filledIcon else outlinedIcon
                val tint = if (isSelected) Color(0xFF16A34A) else Color(0xFF94A3B8)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(onClick = callbacks[index])
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tint
                    )
                }
            }
        }
    }
}
