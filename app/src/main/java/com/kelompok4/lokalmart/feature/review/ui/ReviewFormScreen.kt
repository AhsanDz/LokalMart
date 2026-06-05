package com.kelompok4.lokalmart.feature.review.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok4.lokalmart.feature.review.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFormScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigasi otomatis saat berhasil
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Beri Ulasan",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->

        // ── Sudah pernah review ──────────────────────────────────────────────
        if (uiState.hasAlreadyReviewed) {
            AlreadyReviewedState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onNavigateBack = onNavigateBack
            )
            return@Scaffold
        }

        // ── Success state ────────────────────────────────────────────────────
        if (uiState.isSuccess) {
            ReviewSuccessState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }

        // ── Form ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Rating Stars ─────────────────────────────────────────────────
            Text(
                "Bagaimana pengalamanmu?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Berikan rating untuk produk ini",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ── Star Rating Selector ─────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { viewModel.setRating(i) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (i <= uiState.rating) Icons.Default.Star
                            else Icons.Default.StarBorder,
                            contentDescription = "Rating $i",
                            tint = if (i <= uiState.rating) Color(0xFFF59E0B)
                            else Color(0xFFCBD5E1),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Rating label
            val ratingLabel = when (uiState.rating) {
                1 -> "Sangat Buruk"
                2 -> "Buruk"
                3 -> "Cukup"
                4 -> "Baik"
                5 -> "Sangat Baik"
                else -> "Ketuk bintang untuk memberi rating"
            }
            Text(
                ratingLabel,
                fontSize = 13.sp,
                fontWeight = if (uiState.rating > 0) FontWeight.Medium else FontWeight.Normal,
                color = if (uiState.rating > 0) Color(0xFFF59E0B) else Color(0xFF94A3B8)
            )

            Spacer(Modifier.height(28.dp))

            // ── Divider ──────────────────────────────────────────────────────
            HorizontalDivider(color = Color(0xFFE2E8F0))

            Spacer(Modifier.height(24.dp))

            // ── Comment Area ─────────────────────────────────────────────────
            Text(
                "Tulis Ulasan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.comment,
                onValueChange = viewModel::setComment,
                placeholder = {
                    Text(
                        "Ceritakan pengalamanmu...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Color(0xFF16A34A),
                    cursorColor = Color(0xFF16A34A)
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                ),
                maxLines = 6
            )

            Spacer(Modifier.height(6.dp))

            // ── Character counter ────────────────────────────────────────────
            Text(
                "${uiState.comment.length}/500",
                fontSize = 11.sp,
                color = if (uiState.comment.length > 450) Color(0xFFF59E0B)
                else Color(0xFF94A3B8),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(Modifier.height(16.dp))

            // ── Error Message ────────────────────────────────────────────────
            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let { error ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF2F2))
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            error,
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Submit Button ────────────────────────────────────────────────
            Button(
                onClick = viewModel::submitReview,
                enabled = !uiState.isLoading && uiState.rating > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Mengirim...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Kirim Ulasan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReviewSuccessState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7))
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Ulasan Terkirim!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Terima kasih telah memberikan ulasan.\nUlasanmu membantu pembeli lainnya.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Already Reviewed State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AlreadyReviewedState(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7))
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Sudah Diulas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Kamu sudah memberikan ulasan\nuntuk produk ini pada pesanan ini.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF16A34A)
            ),
            border = BorderStroke(1.dp, Color(0xFF16A34A))
        ) {
            Text(
                "Kembali",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
