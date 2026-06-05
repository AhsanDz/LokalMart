package com.kelompok4.lokalmart.feature.catalog.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.feature.catalog.viewmodel.ProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onAddToCart: (productId: String) -> Unit,
    onStoreClick: (storeId: String) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    var isFavorite by remember { mutableStateOf(false) }

    // local variant state
    var selectedVariant by remember { mutableStateOf("Natural") }
    val mockVariants = listOf("Natural", "Cokelat", "Hitam", "Cream")

    Scaffold(
        bottomBar = {
            if (state.product != null) {
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chat outlined
                        OutlinedButton(
                            onClick = { /* navigate to chat */ },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                        ) {
                            Icon(
                                Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF64748B)
                            )
                        }

                        // + Keranjang outlined
                        OutlinedButton(
                            onClick = {
                                state.product?.let { onAddToCart(it.id) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, green),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = green),
                            enabled = (state.product?.stock ?: 0) > 0
                        ) {
                            Text(
                                "+ Keranjang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Beli Sekarang filled
                        Button(
                            onClick = {
                                state.product?.let { onAddToCart(it.id) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = green),
                            enabled = (state.product?.stock ?: 0) > 0
                        ) {
                            Text(
                                "Beli Sekarang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = green) }
        } else if (state.product != null) {
            val product = state.product!!
            Box(modifier = Modifier.fillMaxSize()) {
                // Main scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = padding.calculateBottomPadding() + 80.dp)
                ) {
                    // ── Image Gallery area ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFDCFCE7), Color(0xFFF0FDF4))
                                )
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = green.copy(alpha = 0.3f),
                                modifier = Modifier.size(72.dp)
                            )
                        }

                        // Category chips on bottom-left of image
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(green)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = state.store?.category ?: "Tas & Aksesoris",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Stok ${product.stock}",
                                    color = green,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ── Product Details Section ──
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title & Price
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = product.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                lineHeight = 26.sp
                            )
                            Text(
                                text = "Rp ${formatPriceWithDots(product.price)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }

                        // Rating & Sold Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "%.1f".format(product.rating),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text("·", color = textMuted)
                            Text(
                                "${product.soldCount} terjual",
                                fontSize = 13.sp,
                                color = textMuted
                            )
                            Text("·", color = textMuted)
                            Text(
                                "87 ulasan",
                                fontSize = 13.sp,
                                color = green,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        // ── Store Verification Card ──
                        Card(
                            onClick = { onStoreClick(product.storeId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Store initials avatar
                                val initials = (product.storeName ?: "Toko").take(2).uppercase()
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = green,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            product.storeName ?: "Toko",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Outlined.Verified,
                                            contentDescription = "Terverifikasi",
                                            tint = green,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        "Malang Kota · 1.2 km",
                                        fontSize = 11.sp,
                                        color = textMuted
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onStoreClick(product.storeId) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, green),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = green)
                                ) {
                                    Text("Kunjungi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        // ── Description ──
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Deskripsi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                            Text(
                                product.description ?: "Tidak ada deskripsi.",
                                fontSize = 13.sp,
                                color = textMuted,
                                lineHeight = 20.sp
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        // ── Variant selector ──
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Varian",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                mockVariants.forEach { variant ->
                                    val isSelected = selectedVariant == variant
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color(0xFF0F172A)
                                                else Color.White
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedVariant = variant }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = variant,
                                            color = if (isSelected) Color.White else textPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        // ── Ulasan Section ──
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Ulasan (87)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                                Text(
                                    "Semua",
                                    color = green,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { /* logic ulasan semua */ }
                                )
                            }

                            // Review summary chart row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "4.9",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Row {
                                        repeat(5) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.width(16.dp))

                                // Visual bar chart
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.weight(2.5f)
                                ) {
                                    val distributions = listOf(0.85f, 0.10f, 0.03f, 0.01f, 0.01f)
                                    distributions.forEachIndexed { i, pct ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "${5 - i}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textMuted,
                                                modifier = Modifier.width(10.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFCBD5E1),
                                                modifier = Modifier.size(8.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFFE2E8F0))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(pct)
                                                        .background(Color(0xFFF59E0B))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            // Individual Review Item
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "S",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = textPrimary
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Sari Wulandari",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                            Row {
                                                repeat(5) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        "2 hari lalu",
                                        fontSize = 10.sp,
                                        color = textMuted
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Kualitas produk sangat bagus, bahan awet dan jahitannya rapi sekali. Penjual juga sangat ramah dan responsif.",
                                    fontSize = 12.sp,
                                    color = textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Transparent Overlaid Header (Back button, Wishlist, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Suka",
                                tint = if (isFavorite) Color(0xFFEF4444) else textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* share logic */ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = "Bagikan",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(state.error ?: "Terjadi kesalahan", color = textMuted)
                }
            }
        }
    }
}
