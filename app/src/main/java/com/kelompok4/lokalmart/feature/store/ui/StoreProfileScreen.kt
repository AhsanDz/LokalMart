package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import com.kelompok4.lokalmart.core.common.theme.*
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.store.viewmodel.StoreProfileViewModel
import java.text.NumberFormat
import java.util.Locale

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val BorderColor    = Color(0xFFE2E8F0)
private val CoverGradient  = Color(0xFFCBD5E1)

@Composable
fun StoreProfileScreen(
    storeId: String,
    onNavigateBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateToEditStore: (String) -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onChatClick: (String, String) -> Unit = { _, _ -> },
    viewModel: StoreProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(storeId) {
        viewModel.loadStoreProfile(storeId)
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
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
                    Text(
                        text = state.error ?: "Gagal memuat profil toko",
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadStoreProfile(storeId) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                state.store?.let { store ->
                    var activeTab by remember { mutableStateOf("produk") }
                    val categoryChips = listOf("Semua", "Tenun", "Tas", "Pakaian", "Lainnya")
                    var selectedChip by remember { mutableStateOf("Semua") }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Cover Image & Floating Card combined header
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                // Cover Photo Background (Gray gradient/pattern)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(CoverGradient, Color(0xFF94A3B8))
                                            )
                                        )
                                )

                                // Transparent Top Buttons Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Back Circle Button
                                    IconButton(
                                        onClick = onNavigateBack,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.9f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Kembali",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Right Action Circle Buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                        IconButton(
                                            onClick = { /* TODO */ },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.9f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorit",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { /* TODO */ },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.9f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Bagikan",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Store Info Card (Floating)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 85.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Store Logo (Overlapping or centered inside card)
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .border(2.dp, Color.White, RoundedCornerShape(14.dp)),
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
                                                Text("🏪", fontSize = 28.sp)
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // Store Name & Verified Icon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = store.name,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))

                                        // Address & Distance Mockup
                                        Text(
                                            text = "${store.address.take(15)}... • 1.2 km",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        // Rating & Reviews Summary
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "4.9",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "• 142 ulasan • Buka",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        Spacer(Modifier.height(16.dp))
                                        HorizontalDivider(color = BorderColor)
                                        Spacer(Modifier.height(14.dp))

                                        // Stat Row: 24 Produk, 1.2k Followers, 4.9 Rating, 98% Selesai
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            StoreProfileStatItem(value = state.products.size.toString(), label = "Produk")
                                            StoreProfileStatItem(value = "1.2k", label = "Followers")
                                            StoreProfileStatItem(value = "4.9", label = "Rating")
                                            StoreProfileStatItem(value = "98%", label = "Selesai")
                                        }

                                        Spacer(Modifier.height(16.dp))

                                        // Action buttons: + Follow, Chat (Buyer) OR Edit Profil, Kelola Stok (Seller)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            if (state.isOwner) {
                                                Button(
                                                    onClick = { onNavigateToEditStore(store.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(vertical = 12.dp),
                                                    modifier = Modifier.weight(1.2f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = Color.White
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Edit Profil Toko", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = onNavigateToInventory,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                                    contentPadding = PaddingValues(vertical = 12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Inventory,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Kelola Stok", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { /* TODO */ },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(vertical = 12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("+ Follow", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { onChatClick(store.id, store.name) },
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                                    contentPadding = PaddingValues(vertical = 12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ChatBubbleOutline,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Tab Headers: Produk, Ulasan, Tentang
                        item(span = { GridItemSpan(2) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                StoreTabItem(
                                    label = "Produk • ${state.products.size}",
                                    isActive = activeTab == "produk",
                                    onClick = { activeTab = "produk" }
                                )
                                StoreTabItem(
                                    label = "Ulasan",
                                    isActive = activeTab == "ulasan",
                                    onClick = { activeTab = "ulasan" }
                                )
                                StoreTabItem(
                                    label = "Tentang",
                                    isActive = activeTab == "tentang",
                                    onClick = { activeTab = "tentang" }
                                )
                            }
                        }

                        // Tab Contents:
                        if (activeTab == "produk") {
                            // Category chips & sorting row
                            item(span = { GridItemSpan(2) }) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Categories chips row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        categoryChips.take(3).forEach { chip ->
                                            val isSelected = chip == selectedChip
                                            val bg = if (isSelected) TextPrimary else Color(0xFFF1F5F9)
                                            val tc = if (isSelected) Color.White else TextPrimary
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(bg)
                                                    .clickable { selectedChip = chip }
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = chip,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = tc
                                                )
                                            }
                                        }

                                        Spacer(Modifier.weight(1f))

                                        // Terlaris Dropdown Mockup
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.clickable { /* TODO */ }
                                        ) {
                                            Text(
                                                text = "Terlaris",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GreenPrimary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Star, // temporary down arrow stand-in or dynamic
                                                contentDescription = null,
                                                tint = GreenPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Product list items
                            if (state.products.isEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Toko ini belum menambahkan produk",
                                            color = TextSecondary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                val filteredProducts = if (selectedChip == "Semua") {
                                    state.products
                                } else {
                                    state.products.filter { it.name.contains(selectedChip, ignoreCase = true) }
                                }

                                items(filteredProducts, key = { it.id }) { product ->
                                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        ProductGridItem(
                                            product = product,
                                            onClick = { onProductClick(product.id) }
                                        )
                                    }
                                }
                            }
                        } else if (activeTab == "ulasan") {
                            item(span = { GridItemSpan(2) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Ulasan Toko",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = "Toko ini memiliki rating 4.9 dari 142 ulasan pelanggan.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Tentang Tab
                            item(span = { GridItemSpan(2) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Tentang Toko",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = store.description ?: "Toko ini belum menyetel deskripsi.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                    HorizontalDivider(color = BorderColor)
                                    Text(
                                        text = "Alamat Toko",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = store.address,
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreProfileStatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StoreTabItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) GreenPrimary else TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(40.dp)
                .background(if (isActive) GreenPrimary else Color.Transparent)
        )
    }
}

@Composable
private fun ProductGridItem(
    product: Product,
    onClick: () -> Unit
) {
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("📦", fontSize = 36.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Name
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(36.dp)
                )

                Spacer(Modifier.height(6.dp))

                // Price
                Text(
                    text = rupiahFormat.format(product.price).replace("Rp", "Rp "),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary
                )

                Spacer(Modifier.height(8.dp))

                // Rating & Sold
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (product.rating > 0) String.format(Locale.US, "%.1f", product.rating) else "4.9",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "|",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Terjual ${if (product.soldCount > 0) product.soldCount else 120}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
