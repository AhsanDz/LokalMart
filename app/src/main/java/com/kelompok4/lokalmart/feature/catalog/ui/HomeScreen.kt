package com.kelompok4.lokalmart.feature.catalog.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.kelompok4.lokalmart.data.model.Category
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.catalog.viewmodel.ProductViewModel

// Mock Store Model
private data class MockStore(
    val id: String,
    val name: String,
    val distance: String,
    val rating: String,
)

// Mock Stores List
private val mockStores = listOf(
    MockStore("1", "Kriya Sari Craft", "1.2 km", "4.9"),
    MockStore("2", "Malang Batik Center", "2.5 km", "4.8"),
    MockStore("3", "Keripik Tempe Sanan", "3.1 km", "4.7"),
    MockStore("4", "Batik Malangan Bu Sari", "0.8 km", "5.0")
)

@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onPesananClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wishlistState = remember { mutableStateMapOf<String, Boolean>() }

    // Map Category index to ID
    val categoryList = uiState.categories
    val categoryMap = remember(categoryList) {
        val map = mutableMapOf<String, Int?>()
        map["Semua"] = null
        categoryList.forEach { cat ->
            when {
                cat.name.contains("Fashion", true) || cat.name.contains("Pakaian", true) -> map["Pakaian"] = cat.id
                cat.name.contains("Aksesoris", true) || cat.name.contains("Tas", true) -> {
                    map["Tas"] = cat.id
                    map["Aksesoris"] = cat.id
                }
                cat.name.contains("Alas Kaki", true) || cat.name.contains("Sepatu", true) -> map["Sepatu"] = cat.id
                cat.name.contains("Kerajinan", true) -> map["Kerajinan"] = cat.id
                cat.name.contains("Batik", true) || cat.name.contains("Tenun", true) -> map["Batik & Tenun"] = cat.id
                cat.name.contains("Dekor", true) || cat.name.contains("Rumah", true) -> map["Dekor Rumah"] = cat.id
            }
        }
        map
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            HomeBottomBar(
                selectedIndex = 0,
                onHomeClick = { /* sudah di home */ },
                onSearchClick = onSearchClick,
                onCartClick = onCartClick,
                onPesananClick = onPesananClick,
                onProfileClick = onProfileClick,
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header: gradient + search bar ────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeHeader(onSearchClick = onSearchClick)
            }

            // ── Promo Banner ─────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                PromoBanner()
            }

            // ── Kategori Section ─────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategorySection(
                    selectedCategoryId = uiState.selectedCategoryId,
                    categoryMap = categoryMap,
                    onCategorySelect = { viewModel.selectCategory(it) }
                )
            }

            // ── Toko Terdekat Section ────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                TokoTerdekatSection()
            }

            // ── Section title: Produk Pilihan ────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Produk pilihan",
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Lihat semua",
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { /* logic lihat semua */ }
                    )
                }
            }

            // ── Content states ───────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    items(6) {
                        ShimmerProductCard()
                    }
                }

                uiState.error != null -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorState(
                            message = uiState.error!!,
                            onRetry = { viewModel.loadHome() },
                        )
                    }
                }

                uiState.products.isEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState()
                    }
                }

                else -> {
                    items(uiState.products, key = { it.id }) { product ->
                        val isWishlisted = wishlistState[product.id] ?: false
                        ProductCard(
                            product = product,
                            isWishlisted = isWishlisted,
                            onWishlistToggle = { wishlistState[product.id] = !isWishlisted },
                            onClick = { onProductClick(product.id) },
                        )
                    }
                }
            }

            // spacer di bawah
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home Header — Gradient + Search Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF16A34A), Color(0xFF15803D)),
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column {
            // Profile & Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = "Logo",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hai, Sari 👋",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "Malang Kota",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { /* action notification */ }
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notifikasi",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { /* action chat */ }
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Pesan",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search bar (non-editable, tap navigates to Search)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Cari produk atau toko lokal...",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Promo Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PromoBanner() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF16A34A), Color(0xFF22C55E))
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PROMO HARI INI",
                        color = Color(0xFF15803D),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Gratis ongkir radius 5km",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Belanja produk UMKM di sekitarmu, sampai dalam 1 jam.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Section (2 Rows of 4 Circles)
// ─────────────────────────────────────────────────────────────────────────────

private data class LocalCategory(
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun CategorySection(
    selectedCategoryId: Int?,
    categoryMap: Map<String, Int?>,
    onCategorySelect: (Int?) -> Unit
) {
    val row1 = listOf(
        LocalCategory("Pakaian", Icons.Outlined.Checkroom),
        LocalCategory("Tas", Icons.Outlined.ShoppingBag),
        LocalCategory("Sepatu", Icons.Default.Storefront), // Fallback to Storefront for Sepatu
        LocalCategory("Kerajinan", Icons.Outlined.Brush)
    )
    val row2 = listOf(
        LocalCategory("Aksesoris", Icons.Outlined.Watch),
        LocalCategory("Batik & Tenun", Icons.Outlined.Texture),
        LocalCategory("Dekor Rumah", Icons.Outlined.Weekend),
        LocalCategory("Lainnya", Icons.Outlined.MoreHoriz)
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(
                text = "Kategori",
                color = Color(0xFF0F172A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Semua",
                color = Color(0xFF16A34A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onCategorySelect(null) }
            )
        }

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row1.forEach { cat ->
                val targetId = categoryMap[cat.label]
                CategoryCircle(
                    category = cat,
                    isSelected = selectedCategoryId != null && selectedCategoryId == targetId,
                    onClick = { onCategorySelect(targetId) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row2.forEach { cat ->
                val targetId = categoryMap[cat.label]
                CategoryCircle(
                    category = cat,
                    isSelected = selectedCategoryId != null && selectedCategoryId == targetId,
                    onClick = { onCategorySelect(targetId) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCircle(
    category: LocalCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF0FDF4))
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) Color(0xFF16A34A) else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Icon(
                category.icon,
                contentDescription = category.label,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.label,
            color = Color(0xFF334155),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toko Terdekat Section (Horizontal Scroll)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TokoTerdekatSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(
                text = "Toko terdekat",
                color = Color(0xFF0F172A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lihat semua",
                color = Color(0xFF16A34A),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { /* Lihat semua toko */ }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            mockStores.forEach { store ->
                StoreCard(store = store)
            }
        }
    }
}

@Composable
private fun StoreCard(store: MockStore) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .width(135.dp)
            .clickable { /* detail toko */ }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Avatar / Icon toko
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0FDF4))
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = store.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = store.rating,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Text(
                    text = store.distance,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x0D000000),
                spotColor = Color(0x1A000000),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        // Gambar produk + Heart icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF0FDF4))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(48.dp),
                )
            }

            // Heart Icon at top right
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable { onWishlistToggle() }
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color(0xFFEF4444) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            // Nama produk
            Text(
                text = product.name,
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(6.dp))

            // Harga
            Text(
                text = "Rp ${formatPriceWithDots(product.price)}",
                color = Color(0xFF16A34A),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(Modifier.height(6.dp))

            // Rating + terjual
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = String.format("%.1f", product.rating),
                    color = Color(0xFF0F172A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = " · ",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                )
                Text(
                    text = "${product.soldCount} terjual",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Nama toko
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = product.storeName ?: "Toko",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shimmer Product Card (Loading)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShimmerProductCard() {
    val brush = shimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(brush)
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty & Error States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0FDF4))
        ) {
            Text(text = "📦", fontSize = 36.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Belum ada produk",
            color = Color(0xFF0F172A),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Produk untuk kategori ini belum tersedia",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp)
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(16.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF16A34A))
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Coba Lagi",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Navigation Bar
// ─────────────────────────────────────────────────────────────────────────────

private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Cari", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem("Keranjang", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomNavItem("Pesanan", Icons.Outlined.Description, Icons.Outlined.Description),
    BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
private fun HomeBottomBar(
    selectedIndex: Int,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onPesananClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
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
            bottomNavItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val tint = if (isSelected) Color(0xFF16A34A) else Color(0xFF94A3B8)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(onClick = callbacks[index])
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        if (isSelected) item.selectedIcon else item.unselectedIcon,
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
// Shimmer Brush Helper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0),
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Price Formatter — Indonesian format (Rp X.XXX)
// ─────────────────────────────────────────────────────────────────────────────

internal fun formatPriceWithDots(price: Double): String {
    val longPrice = price.toLong()
    return longPrice.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}
