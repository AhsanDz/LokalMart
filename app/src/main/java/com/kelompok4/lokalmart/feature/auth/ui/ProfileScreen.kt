package com.kelompok4.lokalmart.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import com.kelompok4.lokalmart.core.common.theme.*
import com.kelompok4.lokalmart.data.model.UserRole
import com.kelompok4.lokalmart.feature.auth.viewmodel.ProfileViewModel
import com.kelompok4.lokalmart.feature.catalog.ui.BuyerBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToMyStore: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToSavedAddresses: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogoutSuccess: () -> Unit,
    onNavigateToAdminPanel: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.profileState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil Saya",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateToEditProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profil",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600
                )
            )
        },
        bottomBar = {
            BuyerBottomNavigation(
                activeTab = "Profil",
                onTabClick = { tab ->
                    when (tab) {
                        "Beranda" -> onNavigateToHome()
                        "Cari" -> onNavigateToSearch()
                        "Keranjang" -> onNavigateToCart()
                        "Pesanan" -> onNavigateToOrders()
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
                    color = Green600,
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
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "Gagal memuat profil",
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                state.user?.let { user ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Background Header Hijau & Stats Card Overlay in Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(204.dp)
                        ) {
                            // Green Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(Green600)
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                // Profile Details Row (avatar, name, email, badges)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
                                        .offset(y = (-24).dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!user.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = user.avatarUrl,
                                                contentDescription = "Avatar Pengguna",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = user.fullName.take(1).uppercase(),
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Green600
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = user.fullName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = user.email,
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(6.dp))

                                        // Role Badges
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            RoleBadge(text = "BUYER")
                                            if (user.role == UserRole.SELLER) {
                                                RoleBadge(text = "SELLER")
                                            }
                                        }
                                    }
                                }
                            }

                            // Stats Card (Overlapping by sitting at the bottom of the 204dp box)
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .height(84.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatsItem(number = state.totalOrders.toString(), label = "Pesanan", modifier = Modifier.weight(1f))
                                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                                    StatsItem(number = state.totalReviews.toString(), label = "Ulasan", modifier = Modifier.weight(1f))
                                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                                    StatsItem(number = state.totalWishlist.toString(), label = "Wishlist", modifier = Modifier.weight(1f), onClick = onNavigateToWishlist)
                                }
                            }
                        }

                        // 2. Profile Actions Content (placed directly inside scrollable column with horizontal padding)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 16.dp)
                        ) {



                            // B. Toko Saya Banner
                            val isSeller = user.role == UserRole.SELLER
                            val bannerBg = if (isSeller) Color(0xFFF0FDF4) else Color(0xFFEFF6FF)
                            val bannerBorder = if (isSeller) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)
                            val iconBg = if (isSeller) Green600 else InfoBlue
                            val titleText = if (isSeller) "Toko Saya: ${state.storeName ?: "Toko Saya"}" else "Buka Toko Gratis"
                            val rupiahFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).apply {
                                maximumFractionDigits = 0
                            }
                            val subtitleText = if (isSeller) "${state.newOrdersCount} order baru · ${rupiahFormat.format(state.todayRevenue).replace("Rp", "Rp ")} hari ini" else "Mulai berjualan produk UMKM Anda"
                            val targetNavigate = if (isSeller) onNavigateToMyStore else onNavigateToStore

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bannerBg)
                                    .border(1.dp, bannerBorder, RoundedCornerShape(14.dp))
                                    .clickable { targetNavigate() }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = titleText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = subtitleText,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isSeller) Green600 else InfoBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (user.role == "admin") {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFFEF2F2))
                                        .border(1.dp, Color(0xFFFEE2E8), RoundedCornerShape(14.dp))
                                        .clickable { onNavigateToAdminPanel() }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Panel Kontrol Admin",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Kelola Toko, Produk, dan Akun Platform",
                                            fontSize = 11.sp,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // C. Menu Items
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    ProfileMenuItem(
                                        icon = Icons.Default.Person,
                                        title = "Edit Profil & Foto",
                                        onClick = onNavigateToEditProfile
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                                    ProfileMenuItem(
                                        icon = Icons.Default.LocationOn,
                                        title = "Alamat Tersimpan",
                                        onClick = onNavigateToSavedAddresses
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                                    ProfileMenuItem(
                                        icon = Icons.Default.Payment,
                                        title = "Metode Pembayaran",
                                        onClick = onNavigateToPaymentMethods
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                                    ProfileMenuItem(
                                        icon = Icons.Default.Notifications,
                                        title = "Notifikasi",
                                        onClick = onNavigateToNotifications
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                                    ProfileMenuItem(
                                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                                        title = "Keluar dari Sesi",
                                        iconColor = ErrorRed,
                                        iconBgColor = Color(0xFFFEE2E2),
                                        onClick = {
                                            viewModel.logout {
                                                onLogoutSuccess()
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatsItem(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = number,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    iconColor: Color = Green600,
    iconBgColor: Color = Green50,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp)
        )
    }
}

