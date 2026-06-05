package com.kelompok4.lokalmart.feature.admin.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.User
import com.kelompok4.lokalmart.data.model.UserRole
import com.kelompok4.lokalmart.feature.admin.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    // Confirmation dialog state
    var showApproveDialog by remember { mutableStateOf<Store?>(null) }
    var showRejectDialog by remember { mutableStateOf<Store?>(null) }
    var rejectNotes by remember { mutableStateOf("") }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AdminBottomNavBar(
                selectedIndex = uiState.selectedTab,
                onTabSelect = { index ->
                    if (index < 3) viewModel.selectTab(index)
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.pendingStores.isEmpty() && uiState.allProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                AdminLoadingState()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Dark Green/Navy Gradient Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF064E3B), Color(0xFF0F172A))
                        ),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Admin Panel",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "LokalMart · ID-001",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { viewModel.loadDashboard() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Muat Ulang",
                                    tint = Color.White
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Tab Navigation ──
            val tabTitles = listOf("Verifikasi · ${uiState.stats.pendingStores}", "Moderasi · ${uiState.stats.totalProducts}", "Pengguna")
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.White,
                contentColor = green,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = green
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTab == index) green else textMuted
                            )
                        }
                    )
                }
            }

            // ── Tab Content ──
            when (uiState.selectedTab) {
                0 -> {
                    // Header Stats for Verification Tab
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        AdminStatCard(
                            label = "Menunggu review",
                            value = uiState.stats.pendingStores.toString(),
                            bottomText = "toko baru",
                            dotColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = "Disetujui hari ini",
                            value = "5",
                            bottomText = "3 admin aktif",
                            dotColor = green,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    StoreVerificationTab(
                        stores = uiState.pendingStores,
                        onApprove = { showApproveDialog = it },
                        onReject = { showRejectDialog = it }
                    )
                }
                1 -> ProductModerationTab(
                    products = uiState.allProducts,
                    onToggleActive = { productId, isActive ->
                        viewModel.toggleProduct(productId, isActive)
                    }
                )
                2 -> UserManagementTab(
                    users = uiState.allUsers,
                    onToggleActive = { userId, isActive ->
                        viewModel.toggleUser(userId, isActive)
                    }
                )
            }
        }
    }

    // Approve Dialog
    showApproveDialog?.let { store ->
        AlertDialog(
            onDismissRequest = { showApproveDialog = null },
            containerColor = Color.White,
            title = {
                Text(
                    "Setujui Toko",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    "Apakah kamu yakin ingin menyetujui toko \"${store.name}\"?",
                    fontSize = 13.sp,
                    color = textMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveStore(store.id)
                        showApproveDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = green),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Setujui", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = null }) {
                    Text("Batal", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }

    // Reject Dialog
    showRejectDialog?.let { store ->
        AlertDialog(
            onDismissRequest = {
                showRejectDialog = null
                rejectNotes = ""
            },
            containerColor = Color.White,
            title = {
                Text(
                    "Tolak Toko",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        "Apakah kamu yakin ingin menolak toko \"${store.name}\"?",
                        fontSize = 13.sp,
                        color = textMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectNotes,
                        onValueChange = { rejectNotes = it },
                        placeholder = {
                            Text(
                                "Catatan penolakan (opsional)",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFEF4444),
                            cursorColor = Color(0xFFEF4444)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = textPrimary
                        ),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectStore(
                            store.id,
                            rejectNotes.trim().ifBlank { null }
                        )
                        showRejectDialog = null
                        rejectNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tolak", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectDialog = null
                    rejectNotes = ""
                }) {
                    Text("Batal", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Admin Stat Card (Upgraded matching mockup design)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminStatCard(
    label: String,
    value: String,
    bottomText: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = bottomText,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 1: Verifikasi Toko (List)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StoreVerificationTab(
    stores: List<Store>,
    onApprove: (Store) -> Unit,
    onReject: (Store) -> Unit
) {
    if (stores.isEmpty()) {
        EmptyTabState(
            icon = Icons.Default.Verified,
            title = "Tidak ada antrian",
            subtitle = "Semua toko sudah diverifikasi"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(stores, key = { it.id }) { store ->
            PendingStoreCard(
                store = store,
                onApprove = { onApprove(store) },
                onReject = { onReject(store) }
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PendingStoreCard(
    store: Store,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Store logo placeholder circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        store.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "oleh Owner · ${store.contactPhone ?: "-"}",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        store.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = green
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "PENDING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Address Row
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    store.address,
                    fontSize = 11.sp,
                    color = textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            // Date Row
            store.createdAt?.let { date ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Diajukan: ${formatDate(date)}",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Setujui", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2: Produk
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductModerationTab(
    products: List<Product>,
    onToggleActive: (String, Boolean) -> Unit
) {
    val textMuted = Color(0xFF64748B)
    if (products.isEmpty()) {
        EmptyTabState(
            icon = Icons.Default.Inventory,
            title = "Belum ada produk",
            subtitle = "Produk dari penjual akan muncul di sini"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products, key = { it.id }) { product ->
            ProductModerationCard(
                product = product,
                onToggle = { isActive -> onToggleActive(product.id, isActive) }
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ProductModerationCard(
    product: Product,
    onToggle: (Boolean) -> Unit
) {
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (product.isActive) Color(0xFFF0FDF4) else Color(0xFFF1F5F9))
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = if (product.isActive) green else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.isActive) textPrimary else textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatRupiah(product.price),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = green
                    )
                    Text(" · ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        "Stok: ${product.stock}",
                        fontSize = 11.sp,
                        color = textMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (product.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (product.isActive) "Aktif" else "Nonaktif",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isActive) green else Color(0xFFEF4444)
                    )
                }

                Switch(
                    checked = product.isActive,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = green,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 3: Pengguna
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserManagementTab(
    users: List<User>,
    onToggleActive: (String, Boolean) -> Unit
) {
    if (users.isEmpty()) {
        EmptyTabState(
            icon = Icons.Default.People,
            title = "Belum ada pengguna",
            subtitle = "Daftar pengguna akan muncul di sini"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(users, key = { it.id }) { user ->
            UserManagementCard(
                user = user,
                onToggle = { isActive -> onToggleActive(user.id, isActive) }
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun UserManagementCard(
    user: User,
    onToggle: (Boolean) -> Unit
) {
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (user.role) {
                            UserRole.ADMIN -> Color(0xFFEFF6FF)
                            UserRole.SELLER -> Color(0xFFF0FDF4)
                            else -> Color(0xFFF1F5F9)
                        }
                    )
            ) {
                Text(
                    user.fullName.take(1).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (user.role) {
                        UserRole.ADMIN -> Color(0xFF3B82F6)
                        UserRole.SELLER -> green
                        else -> Color(0xFF64748B)
                    }
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.fullName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isActive) textPrimary else textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    RoleBadge(role = user.role)
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    user.email,
                    fontSize = 11.sp,
                    color = textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            if (user.role != UserRole.ADMIN) {
                Switch(
                    checked = user.isActive,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = green,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val green = Color(0xFF16A34A)
    val (label, bgColor, textColor) = when (role) {
        UserRole.ADMIN -> Triple("Admin", Color(0xFFEFF6FF), Color(0xFF3B82F6))
        UserRole.SELLER -> Triple("Penjual", Color(0xFFF0FDF4), green)
        else -> Triple("Pembeli", Color(0xFFF1F5F9), Color(0xFF64748B))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared Empty / Loading States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyTabState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdminLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(color = Color(0xFF16A34A))
        Spacer(Modifier.height(12.dp))
        Text(
            "Memuat data admin...",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Admin Bottom Navigation Bar (5 Items)
// ─────────────────────────────────────────────────────────────────────────────

private data class AdminBottomItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val adminBottomItems = listOf(
    AdminBottomItem("Verif", Icons.Filled.CheckCircle, Icons.Filled.CheckCircle),
    AdminBottomItem("Moderasi", Icons.Filled.RemoveRedEye, Icons.Filled.RemoveRedEye),
    AdminBottomItem("Pengguna", Icons.Filled.People, Icons.Filled.People),
    AdminBottomItem("Log", Icons.Outlined.Description, Icons.Outlined.Description),
    AdminBottomItem("Setting", Icons.Filled.Settings, Icons.Filled.Settings)
)

@Composable
private fun AdminBottomNavBar(
    selectedIndex: Int,
    onTabSelect: (Int) -> Unit
) {
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
            adminBottomItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val tint = if (isSelected) Color(0xFF16A34A) else Color(0xFF94A3B8)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelect(index) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        item.selectedIcon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tint
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Util
// ─────────────────────────────────────────────────────────────────────────────

private fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount.toLong())}"
}

private fun formatDate(isoDate: String): String {
    return try {
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) {
            val months = listOf(
                "", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agt", "Sep", "Okt", "Nov", "Des"
            )
            val day = parts[2].toInt()
            val month = months.getOrElse(parts[1].toInt()) { "" }
            val year = parts[0]
            "$day $month $year"
        } else {
            isoDate.take(10)
        }
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
