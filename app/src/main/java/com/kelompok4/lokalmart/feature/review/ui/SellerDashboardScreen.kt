package com.kelompok4.lokalmart.feature.review.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok4.lokalmart.feature.review.data.ReviewItem
import com.kelompok4.lokalmart.feature.review.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard Penjualan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Muat Ulang",
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                DashboardLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            uiState.error != null -> {
                DashboardErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadDashboard() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = 32.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── Summary Cards ────────────────────────────────────────
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SummaryCard(
                                icon = Icons.Default.ShoppingCart,
                                label = "Total Pesanan",
                                value = uiState.totalOrders.toString(),
                                iconTint = Color(0xFF3B82F6),
                                bgColor = Color(0xFFEFF6FF),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Total Pendapatan",
                                value = formatRupiah(uiState.totalRevenue),
                                iconTint = Color(0xFF16A34A),
                                bgColor = Color(0xFFF0FDF4),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SummaryCard(
                                icon = Icons.Default.Inventory,
                                label = "Produk Terjual",
                                value = uiState.totalItemsSold.toString(),
                                iconTint = Color(0xFFF59E0B),
                                bgColor = Color(0xFFFFFBEB),
                                modifier = Modifier.weight(1f)
                            )
                            // Average Rating Card
                            AverageRatingCard(
                                rating = uiState.averageRating,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ── Divider ──────────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(4.dp))
                    }

                    // ── Recent Reviews Header ────────────────────────────────
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.RateReview,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Ulasan Terbaru",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // ── Reviews List or Empty ────────────────────────────────
                    if (uiState.recentReviews.isEmpty()) {
                        item {
                            EmptyReviewsState()
                        }
                    } else {
                        items(
                            items = uiState.recentReviews,
                            key = { it.id }
                        ) { review ->
                            ReviewCard(review = review)
                        }
                    }

                    // Bottom spacing
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(2.dp))

        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Average Rating Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AverageRatingCard(
    rating: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFFBEB))
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                String.format("%.1f", rating),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                "/5",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 1.dp, start = 2.dp)
            )
        }

        Spacer(Modifier.height(2.dp))

        Text(
            "Rating Rata-rata",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Review Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReviewCard(review: ReviewItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar placeholder
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7))
            ) {
                Text(
                    review.reviewerName.take(1).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    review.reviewerName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Star rating row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star
                            else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) Color(0xFFF59E0B)
                            else Color(0xFFCBD5E1),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Date
            Text(
                formatReviewDate(review.createdAt),
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Comment
        if (!review.comment.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                review.comment,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                lineHeight = 18.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty / Loading / Error States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyReviewsState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(32.dp)
    ) {
        Icon(
            Icons.Default.RateReview,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Belum ada ulasan",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ulasan dari pembeli akan muncul di sini",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(color = Color(0xFF16A34A))
        Spacer(Modifier.height(12.dp))
        Text(
            "Memuat dashboard...",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun DashboardErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF16A34A)
            ),
            border = BorderStroke(1.dp, Color(0xFF16A34A))
        ) {
            Text(
                "Coba Lagi",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Util
// ─────────────────────────────────────────────────────────────────────────────

private fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount.toLong())}"
}

private fun formatReviewDate(isoDate: String?): String {
    if (isoDate == null) return "-"
    return try {
        // Format: "2026-01-15T10:30:00+07:00" → "15 Jan 2026"
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) {
            val months = listOf(
                "", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agt", "Sep", "Okt", "Nov", "Des"
            )
            val day = parts[2].toInt()
            val month = months.getOrElse(parts[1].toInt()) { "" }
            val year = parts[0]
            "$day $month $year"
        } else {
            isoDate.take(10)
        }
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
