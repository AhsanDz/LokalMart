package com.kelompok4.lokalmart.feature.cart.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok4.lokalmart.feature.catalog.ui.BuyerBottomNavigation
import com.kelompok4.lokalmart.feature.cart.data.CartItem
import com.kelompok4.lokalmart.feature.cart.viewmodel.CartViewModel
import com.kelompok4.lokalmart.feature.cart.viewmodel.itemsByStore
import com.kelompok4.lokalmart.feature.cart.viewmodel.selectedItems

@Composable
fun CartScreen(
    onCheckoutClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Snackbar untuk error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BuyerBottomNavigation(
                activeTab = "Keranjang",
                onTabClick = { tab ->
                    when (tab) {
                        "Beranda" -> onNavigateToHome()
                        "Cari" -> onNavigateToSearch()
                        "Pesanan" -> onNavigateToOrders()
                        "Profil" -> onNavigateToProfile()
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
            // ── Top Bar ───────────────────────────────────────────────────────
            CartTopBar(
                itemCount  = uiState.items.size,
                isEditMode = uiState.isEditMode,
                onBack     = onNavigateBack,
                onEdit     = viewModel::toggleEditMode
            )

            // ── Edit mode banner ──────────────────────────────────────────────
            if (uiState.isEditMode) {
                EditModeBanner(
                    selectedCount = uiState.selectedCount,
                    totalCount    = uiState.items.size,
                    onSelectAll   = viewModel::selectAll
                )
            }

            // ── Content ───────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> CartLoadingState()
                    uiState.items.isEmpty() -> CartEmptyState()
                    else -> CartItemsList(
                        itemsByStore  = uiState.itemsByStore,
                        isEditMode    = uiState.isEditMode,
                        onIncrement   = viewModel::increment,
                        onDecrement   = viewModel::decrement,
                        onToggleSelect = viewModel::toggleItemSelection,
                        onToggleStoreSelect = viewModel::toggleStoreSelection
                    )
                }
            }

            // ── Bottom Bar ────────────────────────────────────────────────────
            if (uiState.items.isNotEmpty()) {
                if (uiState.isEditMode) {
                    EditModeBottomBar(
                        selectedCount = uiState.selectedCount,
                        onDelete      = viewModel::deleteSelected,
                        onCancel      = viewModel::toggleEditMode
                    )
                } else {
                    CartBottomBar(
                        totalItems  = uiState.totalItems,
                        totalPrice  = uiState.totalPrice,
                        onCheckout  = {
                            val storeId = uiState.selectedItems.firstOrNull()?.storeId ?: ""
                            onCheckoutClick(storeId)
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartTopBar(
    itemCount: Int,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF0F172A))
        }

        Text(
            if (isEditMode) "Pilih Item" else "Keranjang ($itemCount)",
            color      = Color(0xFF0F172A),
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold
        )

        TextButton(onClick = onEdit) {
            Text(
                if (isEditMode) "Selesai" else "Edit",
                color      = Color(0xFF16A34A),
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit mode banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditModeBanner(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0FDF4))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            "$selectedCount dari $totalCount dipilih",
            color    = Color(0xFF0F172A),
            fontSize = 13.sp
        )
        TextButton(onClick = onSelectAll) {
            Text(
                "Pilih semua",
                color      = Color(0xFF15803D),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Items List — digroup per toko
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartItemsList(
    itemsByStore: Map<String, List<CartItem>>,
    isEditMode: Boolean,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onToggleStoreSelect: (String) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        itemsByStore.forEach { (storeName, storeItems) ->
            // Header toko
            item {
                val allSelected = storeItems.all { it.isSelected }
                StoreHeader(
                    storeName = storeName,
                    isActive = storeItems.first().isStoreActive,
                    isSelected = allSelected,
                    onToggleSelect = { onToggleStoreSelect(storeName) }
                )
            }
            // Item-item per toko
            items(storeItems, key = { it.cartId }) { item ->
                CartItemCard(
                    item           = item,
                    isEditMode     = isEditMode,
                    onIncrement    = { onIncrement(item.cartId) },
                    onDecrement    = { onDecrement(item.cartId) },
                    onToggleSelect = { onToggleSelect(item.cartId) }
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StoreHeader(
    storeName: String,
    isActive: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) Color(0xFF16A34A) else Color.White)
                .border(2.dp, if (isSelected) Color(0xFF16A34A) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .clickable(onClick = onToggleSelect)
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(10.dp))

        Icon(
            Icons.Default.Store,
            contentDescription = null,
            tint     = Color(0xFF0F172A),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(storeName, color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        if (isActive) {
            Spacer(Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(9.dp))
                Spacer(Modifier.width(4.dp))
                Text("Verified", color = Color(0xFF15803D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    isEditMode: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val borderColor = if (item.isSelected) Color(0xFF16A34A) else Color(0xFFE2E8F0)
    val bgColor     = if (item.isSelected) Color(0xFFF0FDF4) else Color.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggleSelect)
            .padding(11.dp)
    ) {
        // Checkbox (selalu ditampilkan)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (item.isSelected) Color(0xFF16A34A) else Color.White)
                .border(2.dp, if (item.isSelected) Color(0xFF16A34A) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .clickable(onClick = onToggleSelect)
        ) {
            if (item.isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(10.dp))

        // Foto produk
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(65.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFDCFCE7))
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(28.dp))
            }
        }

        Spacer(Modifier.width(8.dp))

        // Detail produk
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.productName,
                color      = if (isEditMode && !item.isSelected) Color(0xFF64748B) else Color(0xFF0F172A),
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.padding(bottom = 4.dp)
            )
            if (item.variant != null) {
                Text(
                    "Varian: ${item.variant}",
                    color    = Color(0xFF64748B),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Rp ${formatPrice(item.productPrice)}",
                    color      = if (isEditMode && !item.isSelected) Color(0xFF64748B) else Color(0xFF0F172A),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f)
                )

                // Quantity stepper (hanya di mode normal)
                if (!isEditMode) {
                    QuantityStepper(
                        quantity   = item.quantity,
                        maxStock   = item.productStock,
                        onDecrement = onDecrement,
                        onIncrement = onIncrement
                    )
                } else {
                    // Di edit mode tampilkan qty saja
                    Text(
                        "Qty: ${item.quantity}",
                        color    = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    maxStock: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF1F5F9))
            .padding(vertical = 2.dp)
    ) {
        // Tombol −
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .clickable(onClick = onDecrement)
                .padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Text("−", color = Color(0xFF0F172A), fontSize = 13.sp)
        }

        Text(
            "$quantity",
            color      = Color(0xFF0F172A),
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 8.dp)
        )

        // Tombol +
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (quantity >= maxStock) Color(0xFFE2E8F0) else Color.White)
                .clickable(enabled = quantity < maxStock, onClick = onIncrement)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                "+",
                color    = if (quantity >= maxStock) Color(0xFFCBD5E1) else Color(0xFF0F172A),
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Bars
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartBottomBar(
    totalItems: Int,
    totalPrice: Double,
    onCheckout: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 13.dp)
    ) {
        Column {
            Text(
                "Total ($totalItems item)",
                color    = Color(0xFF64748B),
                fontSize = 10.sp
            )
            Text(
                "Rp ${formatPrice(totalPrice)}",
                color      = Color(0xFF0F172A),
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (totalItems > 0) Color(0xFF16A34A) else Color(0xFFCBD5E1))
                .clickable(enabled = totalItems > 0, onClick = onCheckout)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                "Checkout →",
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EditModeBottomBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 13.dp)
    ) {
        // Tombol Pindah (wishlist — placeholder)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
            modifier = Modifier
                .weight(0.5f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .clickable(onClick = onCancel)
                .padding(vertical = 13.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Pindah", color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // Tombol Hapus
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
            modifier = Modifier
                .weight(0.5f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .clickable(enabled = selectedCount > 0, onClick = onDelete)
                .padding(vertical = 13.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "Hapus${if (selectedCount > 0) " ($selectedCount)" else ""}",
                color      = Color(0xFFDC2626),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty & Loading States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier.fillMaxSize()
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Keranjang masih kosong", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text("Tambahkan produk dari katalog", color = Color(0xFFCBD5E1), fontSize = 12.sp)
    }
}

@Composable
private fun CartLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(color = Color(0xFF16A34A))
        Spacer(Modifier.height(12.dp))
        Text("Memuat keranjang...", color = Color(0xFF64748B), fontSize = 13.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Util
// ─────────────────────────────────────────────────────────────────────────────

private fun formatPrice(price: Double): String {
    return String.format("%,.0f", price).replace(',', '.')
}