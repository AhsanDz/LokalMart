package com.kelompok4.lokalmart.feature.review.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kelompok4.lokalmart.feature.review.data.ReviewWithUserDto
import com.kelompok4.lokalmart.feature.review.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    navController: NavController,
    productId: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    val green = Color(0xFF2DB87C)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)
    val borderCol = Color(0xFFE2E8F0)
    val cardBg = Color(0xFFFFFFFF)

    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = Semua, 1..5 = Bintang 1..5

    LaunchedEffect(productId) {
        viewModel.fetchProductReviews(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Semua Ulasan (${state.reviews.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (state.isLoadingReviews) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green)
            }
        } else if (state.reviewsError != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(state.reviewsError ?: "Gagal memuat ulasan", color = textMuted)
            }
        } else {
            val filteredReviews = if (selectedFilter == 0) {
                state.reviews
            } else {
                state.reviews.filter { it.rating == selectedFilter }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(padding)
            ) {
                // 1. Rating Summary Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Average Score
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", state.averageRating),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = green
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(5) { i ->
                                    val active = i < state.averageRating.toInt()
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (active) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${state.reviews.size} ulasan",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }

                        // Right Distribution Bars
                        val totalReviews = state.reviews.size.coerceAtLeast(1)
                        Column(
                            modifier = Modifier.weight(2f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RatingDistributionRow(stars = 5, count = state.star5Count, total = totalReviews, green = green)
                            RatingDistributionRow(stars = 4, count = state.star4Count, total = totalReviews, green = green)
                            RatingDistributionRow(stars = 3, count = state.star3Count, total = totalReviews, green = green)
                            RatingDistributionRow(stars = 2, count = state.star2Count, total = totalReviews, green = green)
                            RatingDistributionRow(stars = 1, count = state.star1Count, total = totalReviews, green = green)
                        }
                    }
                }

                // 2. Filter Horizontal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem(label = "Semua", isSelected = selectedFilter == 0, onClick = { selectedFilter = 0 }, green = green)
                    for (i in 5 downTo 1) {
                        val count = when (i) {
                            5 -> state.star5Count
                            4 -> state.star4Count
                            3 -> state.star3Count
                            2 -> state.star2Count
                            else -> state.star1Count
                        }
                        FilterChipItem(
                            label = "$i Bintang ($count)",
                            isSelected = selectedFilter == i,
                            onClick = { selectedFilter = i },
                            green = green
                        )
                    }
                }

                HorizontalDivider(color = borderCol)

                // 3. Reviews list
                if (filteredReviews.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada ulasan dalam kategori ini.", color = textMuted, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredReviews) { review ->
                            ReviewItemRow(review = review, textPrimary = textPrimary, textMuted = textMuted, borderCol = borderCol, cardBg = cardBg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingDistributionRow(
    stars: Int,
    count: Int,
    total: Int,
    green: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$stars", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(10.dp))
        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
        
        val progress = count.toFloat() / total.toFloat()
        LinearProgressIndicator(
            progress = { progress },
            color = green,
            trackColor = Color(0xFFE2E8F0),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        )
        Text(text = "$count", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(20.dp))
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    green: Color
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) green.copy(alpha = 0.12f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) green else Color(0xFFCBD5E1))
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) green else Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ReviewItemRow(
    review: ReviewWithUserDto,
    textPrimary: Color,
    textMuted: Color,
    borderCol: Color,
    cardBg: Color
) {
    val buyerName = review.profiles?.fullName ?: "Pembeli"
    val initials = buyerName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    val formattedDate = remember(review.createdAt) {
        if (review.createdAt == null) "-" else {
            try {
                val clean = review.createdAt.replace("T", " ").substringBefore(".")
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("d MMM yyyy", Locale("in", "ID"))
                val date = parser.parse(clean)
                if (date != null) formatter.format(date) else "-"
            } catch (e: Exception) {
                "-"
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Buyer profile info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color(0xFF2DB87C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buyerName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
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
                                    tint = if (i < review.rating) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Text(
                            text = "· $formattedDate",
                            fontSize = 10.sp,
                            color = textMuted
                        )
                    }
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = review.comment,
                    fontSize = 12.sp,
                    color = textPrimary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
