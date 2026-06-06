package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.feature.store.viewmodel.MyStoreViewModel

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val CardBg         = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE2E8F0)
private val GrayBg         = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreProfileScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToEditStore: (String) -> Unit,
    onNavigateToPublicProfile: (String) -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToSellerOrders: () -> Unit = {},
    viewModel: MyStoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyStore()
    }

    Scaffold(
        containerColor = GrayBg,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Profil Toko Saya",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            SellerBottomNavigation(
                activeTab = "toko",
                onTabClick = { tab ->
                    when (tab) {
                        "dashboard" -> onNavigateToDashboard()
                        "produk" -> onNavigateToInventory()
                        "order" -> onNavigateToSellerOrders()
                        "laporan" -> onNavigateToReports()
                        "toko" -> { /* Stay here */ }
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
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "Gagal memuat data toko",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                state.store?.let { store ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Store Header Profile Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Logo
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(1.dp, BorderColor, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!store.logoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = store.logoUrl,
                                            contentDescription = "Logo Toko",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("🏪", fontSize = 42.sp)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Nama Toko
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = store.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (store.status == "active") {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Terverifikasi",
                                            tint = Color(0xFF22C55E),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                // Kategori Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenLight)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = store.category,
                                        color = GreenPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Status Verifikasi Badge
                                val (statusText, statusBg, statusColor) = when (store.status) {
                                    "active" -> Triple("Toko Aktif", Color(0xFFDCFCE7), Color(0xFF16A34A))
                                    "pending" -> Triple("Menunggu Verifikasi", Color(0xFFFEF3C7), Color(0xFFD97706))
                                    "rejected" -> Triple("Verifikasi Ditolak", Color(0xFFFEE2E2), Color(0xFFEF4444))
                                    else -> Triple("Toko Ditangguhkan", Color(0xFFF1F5F9), Color(0xFF64748B))
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(statusBg)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // 2. Info Detail Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Detail Informasi Toko",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                HorizontalDivider(color = BorderColor)

                                // Deskripsi
                                InfoItemRow(
                                    icon = Icons.Default.Description,
                                    label = "Deskripsi Toko",
                                    value = store.description ?: "Belum ada deskripsi untuk toko ini."
                                )

                                // Alamat
                                InfoItemRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Alamat Toko",
                                    value = store.address
                                )

                                // WhatsApp
                                InfoItemRow(
                                    icon = Icons.Default.Phone,
                                    label = "Nomor WhatsApp",
                                    value = store.contactPhone ?: "Belum menambahkan kontak."
                                )
                            }
                        }

                        // 3. Action Buttons Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Edit Profil Toko
                            Button(
                                onClick = { onNavigateToEditStore(store.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Edit Profil Toko", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Preview Toko (Sebagai Pembeli)
                            OutlinedButton(
                                onClick = { onNavigateToPublicProfile(store.id) },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, GreenPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Lihat Toko (sebagai Pembeli)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Shortcut ke Kelola Produk
                            OutlinedButton(
                                onClick = onNavigateToInventory,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Kelola Produk & Stok", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}
