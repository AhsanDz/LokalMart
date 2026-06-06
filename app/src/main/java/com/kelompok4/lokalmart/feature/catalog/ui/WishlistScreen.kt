package com.kelompok4.lokalmart.feature.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
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
import com.kelompok4.lokalmart.core.util.Resource
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
fun WishlistScreen(
    onNavigateBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val wishlistSet by viewModel.wishlistProductIds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeCatalog()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wishlist Saya",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.background(Color.White)
            )
        },
        containerColor = GrayBg
    ) { paddingValues ->
        val wishlistedProducts = homeState.products.filter { wishlistSet.contains(it.id) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (homeState.isLoading) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (wishlistedProducts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "❤️",
                        fontSize = 56.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Wishlist Kamu Kosong",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Jelajahi produk lokal terbaik kami dan tambahkan ke wishlist Anda!",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(wishlistedProducts, key = { it.id }) { product ->
                        WishlistProductItem(
                            product = product,
                            onProductClick = { onProductClick(product.id) },
                            onWishlistToggle = { viewModel.toggleWishlist(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistProductItem(
    product: Product,
    onProductClick: () -> Unit,
    onWishlistToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Hapus",
                        tint = Color.Red,
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
