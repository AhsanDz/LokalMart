package com.kelompok4.lokalmart.feature.catalog.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.catalog.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import com.kelompok4.lokalmart.feature.review.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val BorderColor    = Color(0xFFE2E8F0)
private val CardBg         = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToStoreDetail: (String) -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    onNavigateToAllReviews: (String) -> Unit = {},
    onChatClick: (String, String) -> Unit = { _, _ -> },
    viewModel: ProductViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val wishlistSet by viewModel.wishlistProductIds.collectAsState()
    val reviewViewModel: ReviewViewModel = hiltViewModel()
    val reviewState by reviewViewModel.uiState.collectAsState()
    var selectedVariant by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    var isBuyNowClicked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, productId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProductDetail(productId)
                reviewViewModel.fetchProductReviews(productId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(detailState.isAddedToCart) {
        if (detailState.isAddedToCart) {
            if (isBuyNowClicked) {
                isBuyNowClicked = false
                viewModel.resetCartStatus()
                val storeId = detailState.store?.id ?: ""
                onNavigateToCheckout(storeId)
            } else {
                snackbarHostState.showSnackbar("Produk berhasil ditambahkan ke keranjang")
                viewModel.resetCartStatus()
            }
        }
    }

    LaunchedEffect(detailState.cartError) {
        detailState.cartError?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.resetCartStatus()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            detailState.product?.let { product ->
                BottomActionBar(
                    onChatClick = {
                        val store = detailState.store
                        if (store != null) {
                            onChatClick(store.id, store.name)
                        }
                    },
                    onAddToCartClick = { 
                        if (productId.startsWith("mock-")) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Produk demo tidak dapat ditambahkan. Silakan gunakan produk dari Toko asli Anda.")
                            }
                        } else {
                            viewModel.addToCart(productId, 1)
                        }
                    },
                    onBuyNowClick = { 
                        if (productId.startsWith("mock-")) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Produk demo tidak dapat dibeli. Silakan gunakan produk dari Toko asli Anda.")
                            }
                        } else {
                            isBuyNowClicked = true
                            viewModel.addToCart(productId, 1)
                        }
                    },
                    isOutOfStock = product.stock <= 0
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (detailState.isLoading) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (detailState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = detailState.error ?: "Gagal memuat detail produk",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProductDetail(productId) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                val product = detailState.product
                val store = detailState.store

                if (product != null && store != null) {
                    // Set default selected variant if empty
                    val variants = product.variant?.split(",")?.map { it.trim() } ?: emptyList()
                    if (selectedVariant.isEmpty() && variants.isNotEmpty()) {
                        selectedVariant = variants.first()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Image Header Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            // Main Product Image
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top transparent buttons bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back Button
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

                                // Share and Wishlist Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    val isWishlisted = wishlistSet.contains(productId)
                                    IconButton(
                                        onClick = { viewModel.toggleWishlist(productId) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.9f))
                                    ) {
                                        Icon(
                                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Wishlist",
                                            tint = if (isWishlisted) Color.Red else TextPrimary,
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
                        }

                        // 2. Info Section (Name, Price, Category & Stock pills)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(18.dp)
                        ) {
                            // Category & Stock Pills
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenLight)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = store.category, // fallback category name
                                        color = GreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                val stockColor = if (product.stock == 0) Color(0xFFEF4444) else TextSecondary
                                val stockBg = if (product.stock == 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(stockBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (product.stock == 0) "Stok Habis" else "Stok ${product.stock}",
                                        color = stockColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Name
                            Text(
                                text = product.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(6.dp))

                            // Price
                            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                                maximumFractionDigits = 0
                            }.format(product.price).replace("Rp", "Rp ")

                            Text(
                                text = formattedPrice,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )

                            Spacer(Modifier.height(8.dp))

                            // Rating & Review text summary
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (reviewState.reviews.isNotEmpty()) String.format(Locale.US, "%.1f", reviewState.averageRating) else "4.9",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(text = "•", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = "${product.soldCount} terjual",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(text = "•", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = if (reviewState.reviews.isNotEmpty()) "${reviewState.reviews.size} ulasan" else "87 ulasan",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // 3. Store Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🏪", fontSize = 22.sp)
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = store.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = store.address,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onNavigateToStoreDetail(store.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Kunjungi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // 4. Description and Variants
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(18.dp)
                        ) {
                            // Description
                            Text(
                                text = "Deskripsi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = product.description ?: "Tidak ada deskripsi produk.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            // Variants
                            if (variants.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Varian",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    variants.forEach { variant ->
                                        val isSelected = selectedVariant == variant
                                        val variantBg = if (isSelected) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                        val variantText = if (isSelected) Color.White else TextPrimary
                                        val variantBorder = if (isSelected) Color(0xFF1E293B) else BorderColor

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(variantBg)
                                                .border(1.dp, variantBorder, RoundedCornerShape(8.dp))
                                                .clickable { selectedVariant = variant }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = variant,
                                                color = variantText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // 5. Review Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ulasan (${reviewState.reviews.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (reviewState.reviews.isNotEmpty()) {
                                    Text(
                                        text = "Semua",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary,
                                        modifier = Modifier.clickable { onNavigateToAllReviews(productId) }
                                    )
                                }
                            }

                            if (reviewState.reviews.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Belum ada ulasan untuk produk ini",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                // Rating distribution box
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left average rating
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.1f", reviewState.averageRating),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GreenPrimary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            repeat(5) { i ->
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (i < reviewState.averageRating.toInt()) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Right star distribution bars
                                    val total = reviewState.reviews.size.coerceAtLeast(1)
                                    Column(
                                        modifier = Modifier.weight(2.5f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        RatingBarItem(stars = 5, progress = reviewState.star5Count.toFloat() / total, count = reviewState.star5Count.toString())
                                        RatingBarItem(stars = 4, progress = reviewState.star4Count.toFloat() / total, count = reviewState.star4Count.toString())
                                        RatingBarItem(stars = 3, progress = reviewState.star3Count.toFloat() / total, count = reviewState.star3Count.toString())
                                        RatingBarItem(stars = 2, progress = reviewState.star2Count.toFloat() / total, count = reviewState.star2Count.toString())
                                        RatingBarItem(stars = 1, progress = reviewState.star1Count.toFloat() / total, count = reviewState.star1Count.toString())
                                    }
                                }

                                // Single review card (latest review)
                                val latestReview = reviewState.reviews.first()
                                val buyerName = latestReview.profiles?.fullName ?: "Pembeli"
                                val initials = buyerName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                                val formattedDate = try {
                                    val clean = latestReview.createdAt?.replace("T", " ")?.substringBefore(".") ?: ""
                                    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val formatter = SimpleDateFormat("d MMM yyyy", Locale("in", "ID"))
                                    val date = parser.parse(clean)
                                    if (date != null) formatter.format(date) else "Baru saja"
                                } catch (e: Exception) {
                                    "Baru saja"
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(GreenLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initials,
                                                    color = GreenPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = buyerName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                        repeat(5) { i ->
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = if (i < latestReview.rating) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "• $formattedDate",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                        if (!latestReview.comment.isNullOrBlank()) {
                                            Spacer(Modifier.height(10.dp))
                                            Text(
                                                text = latestReview.comment,
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBarItem(
    stars: Int,
    progress: Float,
    count: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$stars",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(8.dp)
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = Color(0xFFF59E0B),
            trackColor = Color(0xFFE2E8F0)
        )
        Text(
            text = count,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.width(16.dp)
        )
    }
}

@Composable
private fun BottomActionBar(
    onChatClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    isOutOfStock: Boolean = false
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Chat Icon button
            IconButton(
                onClick = onChatClick,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5ED))
                    .border(1.dp, Color(0xFFD1E7DD), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Chat",
                    tint = GreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Add to Cart button
            Button(
                onClick = onAddToCartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOutOfStock) Color(0xFFF1F5F9) else Color(0xFFE8F5ED),
                    contentColor = if (isOutOfStock) Color(0xFF94A3B8) else GreenPrimary
                ),
                enabled = !isOutOfStock,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Keranjang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Buy Now button
            Button(
                onClick = onBuyNowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOutOfStock) Color(0xFFCBD5E1) else GreenPrimary,
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                enabled = !isOutOfStock,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = if (isOutOfStock) "Stok Habis" else "Beli Sekarang",
                    color = if (isOutOfStock) Color(0xFF94A3B8) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
