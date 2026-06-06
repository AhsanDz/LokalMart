package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.core.common.theme.*
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.store.viewmodel.InventoryViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import java.text.NumberFormat
import java.util.Locale

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val BorderColor    = Color(0xFFE2E8F0)
private val CardBg         = Color(0xFFFFFFFF)

@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (String) -> Unit = {},
    onNavigateToMyStoreProfile: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSellerOrders: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("semua") } // semua, aktif, habis

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadInventory()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Filter products locally for UI tabs
    val filteredProducts = when (selectedTab) {
        "aktif" -> state.products.filter { it.stock > 0 && it.isActive }
        "habis" -> state.products.filter { it.stock == 0 }
        else -> state.products
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            SellerBottomNavigation(
                activeTab = "produk",
                onTabClick = { tab ->
                    when (tab) {
                        "dashboard" -> onNavigateBack() // return to dashboard
                        "order" -> onNavigateToSellerOrders()
                        "laporan" -> onNavigateToReports()
                        "toko" -> onNavigateToMyStoreProfile()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Produk",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Header (Produk Saya & Search)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, BorderColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Produk Saya • ${state.products.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Search Button
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2. Segmented Tabs: Semua, Aktif, Habis
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val activeCount = state.products.count { it.stock > 0 && it.isActive }
                        val outCount = state.products.count { it.stock == 0 }

                        SegmentedTabItem(
                            label = "Semua • ${state.products.size}",
                            isActive = selectedTab == "semua",
                            onClick = { selectedTab = "semua" },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedTabItem(
                            label = "Aktif • $activeCount",
                            isActive = selectedTab == "aktif",
                            onClick = { selectedTab = "aktif" },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedTabItem(
                            label = "Habis • $outCount",
                            isActive = selectedTab == "habis",
                            onClick = { selectedTab = "habis" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 3. Products List
                if (state.isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        CircularProgressIndicator(
                            color = GreenPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else if (state.error != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "Gagal memuat daftar produk",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadInventory() },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                } else if (filteredProducts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada produk dalam kategori ini.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductManagementRow(
                                product = product,
                                onEditClick = { onNavigateToEditProduct(product.id) }
                            )
                        }
                    }
                }
            }

            if (state.isUpdating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
        }
    }
}

@Composable
private fun SegmentedTabItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) Color.White else Color.Transparent
    val tc = if (isActive) TextPrimary else TextSecondary
    val fw = if (isActive) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = fw,
            color = tc
        )
    }
}

@Composable
private fun ProductManagementRow(
    product: Product,
    onEditClick: () -> Unit
) {
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image Thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
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
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF94A3B8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (product.isActive) "📦" else "NONAKTIF",
                                fontSize = if (product.isActive) 24.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = rupiahFormat.format(product.price).replace("Rp", "Rp "),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (product.rating > 0) String.format(Locale.US, "%.1f", product.rating) else "4.9",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "• ${product.soldCount} terjual",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        // Category Tag badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GreenLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Tas", // Mockup default / dynamic in future
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Edit pencil button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Produk",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Stock progress bar section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stok",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                val stockText = when {
                    product.stock == 0 -> "Habis"
                    product.stock <= 5 -> "${product.stock} tersisa"
                    else -> "${product.stock} tersisa"
                }

                val stockTextColor = when {
                    product.stock == 0 -> Color(0xFFEF4444) // Red
                    product.stock <= 5 -> Color(0xFFF97316) // Orange
                    else -> TextPrimary
                }

                Text(
                    text = stockText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = stockTextColor
                )
            }

            Spacer(Modifier.height(6.dp))

            // Progress bar indicator
            val progress = when {
                product.stock == 0 -> 0f
                product.stock >= 30 -> 1f
                else -> product.stock / 30f
            }

            val progressBarColor = when {
                product.stock == 0 -> Color(0xFFEF4444)
                product.stock <= 5 -> Color(0xFFF97316)
                else -> Color(0xFF22C55E)
            }

            LinearProgressIndicator(
                progress = { progress },
                color = progressBarColor,
                trackColor = Color(0xFFE2E8F0),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
            )
        }
    }
}
