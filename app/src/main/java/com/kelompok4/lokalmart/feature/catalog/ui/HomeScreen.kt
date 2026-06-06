package com.kelompok4.lokalmart.feature.catalog.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Category
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.catalog.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val CardBg         = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE2E8F0)
private val GrayBg         = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToStoreDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToAllCategories: () -> Unit = {},
    onNavigateToCategoryProducts: (Int, String) -> Unit = { _, _ -> },
    onNavigateToAllProducts: () -> Unit = {},
    onNavigateToAllStores: () -> Unit = {},
    viewModel: ProductViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val wishlistSet by viewModel.wishlistProductIds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeCatalog()
    }

    Scaffold(
        containerColor = GrayBg,
        bottomBar = {
            BuyerBottomNavigation(
                activeTab = "Beranda",
                onTabClick = { tab ->
                    when (tab) {
                        "Cari" -> onNavigateToSearch()
                        "Keranjang" -> onNavigateToCart()
                        "Pesanan" -> onNavigateToOrders()
                        "Profil" -> onNavigateToProfile()
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
            if (homeState.isLoading) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Header (Malang Kota & Notification/Chat Buttons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green marker box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hai, ${homeState.userName} ✨",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Malang Kota",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Notification & Chat Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            IconButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, BorderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifikasi",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onNavigateToChat,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, BorderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "Chat",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 2. Search Field Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { onNavigateToSearch() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Cari produk atau toko lokal...",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 3. Promo Banner (Gratis Ongkir)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF15803D), Color(0xFF16A34A))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "PROMO HARI INI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Gratis ongkir radius 5km",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Belanja produk UMKM di sekitarmu, sampai dalam 1 jam.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 4. Kategori Section
                    HomeSectionHeader(title = "Kategori", actionText = "Semua", onActionClick = onNavigateToAllCategories)
                    
                    Spacer(Modifier.height(10.dp))

                    CategoryGrid(categories = homeState.categories, onCategoryClick = onNavigateToCategoryProducts)

                    Spacer(Modifier.height(24.dp))

                    // 5. Toko Favorit Section
                    HomeSectionHeader(title = "Toko favorit", actionText = "Lihat semua", onActionClick = onNavigateToAllStores)
                    
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (homeState.favoriteStores.isNotEmpty()) {
                            homeState.favoriteStores.forEach { store ->
                                FavoriteStoreCard(
                                    name = store.name,
                                    rating = "4.8",
                                    imageUrl = store.logoUrl ?: "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?q=80&w=300",
                                    onClick = { onNavigateToStoreDetail(store.id) }
                                )
                            }
                        } else {
                            FavoriteStoreCard(
                                name = "Kriya Sari Craft",
                                rating = "4.9",
                                imageUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?q=80&w=300",
                                onClick = { onNavigateToStoreDetail("mock-store-1") }
                            )
                            FavoriteStoreCard(
                                name = "Tenun Lestari",
                                rating = "4.8",
                                imageUrl = "https://images.unsplash.com/photo-1524295988897-b13b5b6302e6?q=80&w=300",
                                onClick = { onNavigateToStoreDetail("mock-store-2") }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // 6. Produk Pilihan Grid
                    HomeSectionHeader(title = "Produk pilihan", actionText = "Lihat semua", onActionClick = onNavigateToAllProducts)
                    
                    Spacer(Modifier.height(10.dp))

                    ProductGridSection(
                        products = homeState.products,
                        wishlistSet = wishlistSet,
                        onProductClick = onNavigateToProductDetail,
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = actionText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary,
            modifier = Modifier.clickable(onClick = onActionClick)
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    onCategoryClick: (Int, String) -> Unit
) {
    val emojis = mapOf(
        "Pakaian" to "👕",
        "Tas" to "👜",
        "Sepatu" to "👟",
        "Kerajinan" to "🎨",
        "Aksesoris" to "💼",
        "Batik & Tenun" to "🧣",
        "Dekor Rumah" to "🏠",
        "Lainnya" to "➕"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.take(4).forEach { category ->
                CategoryItem(
                    name = category.name,
                    emoji = emojis[category.name] ?: "📦",
                    modifier = Modifier.weight(1f),
                    onClick = { onCategoryClick(category.id, category.name) }
                )
            }
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.drop(4).take(4).forEach { category ->
                CategoryItem(
                    name = category.name,
                    emoji = emojis[category.name] ?: "📦",
                    modifier = Modifier.weight(1f),
                    onClick = { onCategoryClick(category.id, category.name) }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    name: String,
    emoji: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun FavoriteStoreCard(
    name: String,
    rating: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = rating,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductGridSection(
    products: List<Product>,
    wishlistSet: Set<String>,
    onProductClick: (String) -> Unit,
    onWishlistToggle: (String) -> Unit
) {
    // Custom 2-column flow using nested Rows to work perfectly inside a scrollable screen
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val chunkedProducts = products.chunked(2)
        chunkedProducts.forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowProducts.forEach { product ->
                    ProductCardItem(
                        product = product,
                        isWishlisted = wishlistSet.contains(product.id),
                        onWishlistToggle = { onWishlistToggle(product.id) },
                        onClick = { onProductClick(product.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // If there's an odd item in the last row, fill the remaining space with a blank spacer
                if (rowProducts.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProductCardItem(
    product: Product,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Product Image
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite icon overlay
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .align(Alignment.TopEnd)
                        .clickable(onClick = onWishlistToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Suka",
                        tint = if (isWishlisted) Color.Red else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(6.dp))

                // Price Rupiah format
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                    maximumFractionDigits = 0
                }.format(product.price).replace("Rp", "Rp ")

                Text(
                    text = formattedPrice,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary
                )

                Spacer(Modifier.height(6.dp))

                // Rating & Sold count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = product.rating.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(text = "•", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = "${product.soldCount} terjual",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// Buyer Bottom Navigation bar
@Composable
fun BuyerBottomNavigation(
    activeTab: String,
    onTabClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BuyerBottomNavItem(
                icon = Icons.Default.Home,
                label = "Beranda",
                isActive = activeTab == "Beranda",
                onClick = { onTabClick("Beranda") }
            )
            BuyerBottomNavItem(
                icon = Icons.Default.Search,
                label = "Cari",
                isActive = activeTab == "Cari",
                onClick = { onTabClick("Cari") }
            )
            BuyerBottomNavItem(
                icon = Icons.Default.ShoppingCart,
                label = "Keranjang",
                isActive = activeTab == "Keranjang",
                onClick = { onTabClick("Keranjang") }
            )
            BuyerBottomNavItem(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = "Pesanan",
                isActive = activeTab == "Pesanan",
                onClick = { onTabClick("Pesanan") }
            )
            BuyerBottomNavItem(
                icon = Icons.Default.Person,
                label = "Profil",
                isActive = activeTab == "Profil",
                onClick = { onTabClick("Profil") }
            )
        }
    }
}

@Composable
private fun BuyerBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) GreenPrimary else Color(0xFF94A3B8),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) GreenPrimary else Color(0xFF94A3B8)
        )
    }
}
