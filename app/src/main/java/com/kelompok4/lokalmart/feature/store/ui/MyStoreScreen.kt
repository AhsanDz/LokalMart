package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.core.common.theme.Green100
import com.kelompok4.lokalmart.core.common.theme.Green500
import com.kelompok4.lokalmart.core.common.theme.Green600
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.StoreStatus
import com.kelompok4.lokalmart.feature.store.viewmodel.StoreViewModel

@Composable
fun MyStoreScreen(
    onNavigateBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    viewModel: StoreViewModel = hiltViewModel()
) {
    val state by viewModel.myStoreState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyStore()
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            // FAB hanya muncul jika toko aktif
            if (state.store?.status == StoreStatus.ACTIVE) {
                FloatingActionButton(
                    onClick = onAddProduct,
                    containerColor = Green600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ===== Top bar =====
            MyStoreTopBar(onNavigateBack = onNavigateBack)

            when {
                state.isLoading -> MyStoreLoadingState()
                state.error != null -> MyStoreErrorState(state.error!!)
                state.store == null -> MyStoreEmptyState()
                else -> MyStoreContent(
                    store = state.store!!,
                    products = state.products,
                    onEditProduct = onEditProduct,
                    onAddProduct = onAddProduct
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MyStoreTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Green600
            )
        }
        Spacer(Modifier.width(50.dp))
        Text(
            text = "Toko Saya",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.16).sp,
            color = Color(0xFF0F172A)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Store Content (Toko Ada)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MyStoreContent(
    store: Store,
    products: List<Product>,
    onEditProduct: (String) -> Unit,
    onAddProduct: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Store Info Card ──────────────────────────────────────────────
        item {
            StoreInfoCard(store = store)
        }

        // ── Pesan verifikasi jika pending ────────────────────────────────
        if (store.status == StoreStatus.PENDING) {
            item {
                PendingVerificationBanner()
            }
        }

        if (store.status == StoreStatus.SUSPENDED) {
            item {
                SuspendedBanner()
            }
        }

        // ── Statistik toko ──────────────────────────────────────────────
        item {
            StoreStatsRow(totalProducts = products.size)
        }

        // ── Header kelola produk ────────────────────────────────────────
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "Kelola Produk",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${products.size} produk",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // ── Daftar produk atau empty state ───────────────────────────────
        if (products.isEmpty()) {
            item {
                ProductsEmptyState(
                    canAdd = store.status == StoreStatus.ACTIVE,
                    onAddProduct = onAddProduct
                )
            }
        } else {
            items(products, key = { it.id }) { product ->
                ProductManageCard(
                    product = product,
                    onEdit = { onEditProduct(product.id) }
                )
            }
        }

        // Extra spacing di bawah untuk FAB
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Store Info Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StoreInfoCard(store: Store) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Logo toko placeholder
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Green100)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Green600,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kategori badge
                    CategoryBadge(category = store.category)
                    // Status badge
                    StatusBadge(status = store.status)
                }
            }
        }

        if (!store.address.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(10.dp))
            Text(
                text = store.address,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!store.description.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = store.description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        StoreStatus.ACTIVE -> Triple(
            Color(0xFFDCFCE7), Color(0xFF15803D), "Aktif"
        )
        StoreStatus.PENDING -> Triple(
            Color(0xFFFEF3C7), Color(0xFF92400E), "Menunggu Verifikasi"
        )
        StoreStatus.SUSPENDED -> Triple(
            Color(0xFFFEE2E2), Color(0xFFDC2626), "Ditangguhkan"
        )
        StoreStatus.REJECTED -> Triple(
            Color(0xFFFEE2E2), Color(0xFFDC2626), "Ditolak"
        )
        else -> Triple(
            Color(0xFFF1F5F9), Color(0xFF64748B), status
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pending / Suspended Banners
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PendingVerificationBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF3C7))
            .padding(14.dp)
    ) {
        Text(
            text = "⏳",
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Toko sedang dalam proses verifikasi admin",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Anda belum bisa menambahkan produk hingga toko disetujui.",
                fontSize = 11.sp,
                color = Color(0xFFA16207)
            )
        }
    }
}

@Composable
private fun SuspendedBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEE2E2))
            .padding(14.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier
                .size(24.dp)
                .padding(end = 2.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Toko ditangguhkan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Hubungi admin untuk informasi lebih lanjut.",
                fontSize = 11.sp,
                color = Color(0xFFB91C1C)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Store Stats Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StoreStatsRow(totalProducts: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        StatItem(
            icon = Icons.Default.Inventory2,
            value = "$totalProducts",
            label = "Produk"
        )
        StatItem(
            icon = Icons.Default.ShoppingBag,
            value = "0",
            label = "Pesanan"
        )
        StatItem(
            icon = Icons.Default.Star,
            value = "0.0",
            label = "Rating"
        )
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product Manage Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductManageCard(product: Product, onEdit: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable(onClick = onEdit)
            .padding(11.dp)
    ) {
        // Gambar produk placeholder
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFDCFCE7))
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Rp ${formatPrice(product.price)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Green600
            )
            Spacer(Modifier.height(4.dp))
            // Stock indicator
            StockIndicator(stock = product.stock)
        }

        Spacer(Modifier.width(8.dp))

        // Edit button
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Produk",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StockIndicator(stock: Int) {
    val (bgColor, textColor, label) = when {
        stock <= 0 -> Triple(
            Color(0xFFFEE2E2), Color(0xFFDC2626), "Habis"
        )
        stock <= 5 -> Triple(
            Color(0xFFFEF3C7), Color(0xFF92400E), "Stok: $stock (hampir habis)"
        )
        else -> Triple(
            Color(0xFFDCFCE7), Color(0xFF15803D), "Stok: $stock"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty / Loading / Error States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductsEmptyState(canAdd: Boolean, onAddProduct: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .background(Color(0xFFFAFAFA))
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Belum ada produk",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (canAdd) "Tambahkan produk pertama Anda"
            else "Toko harus diverifikasi dulu sebelum bisa menambah produk",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )
        if (canAdd) {
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green600)
                    .clickable(onClick = onAddProduct)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tambah Produk",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MyStoreLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(color = Green600)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Memuat data toko...",
            color = Color(0xFF64748B),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MyStoreErrorState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun MyStoreEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Store,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Anda belum memiliki toko",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Daftarkan toko untuk mulai berjualan",
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Util
// ─────────────────────────────────────────────────────────────────────────────

private fun formatPrice(price: Double): String {
    val long = price.toLong()
    return java.text.NumberFormat.getInstance(java.util.Locale("id", "ID")).format(long)
}
