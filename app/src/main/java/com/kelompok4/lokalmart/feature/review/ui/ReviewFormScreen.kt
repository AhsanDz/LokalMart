package com.kelompok4.lokalmart.feature.review.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kelompok4.lokalmart.feature.review.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFormScreen(
    navController: NavController,
    orderId: String,
    productId: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val green = Color(0xFF2DB87C)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)
    val borderCol = Color(0xFFE2E8F0)

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        viewModel.fetchProductDetail(productId)
    }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            Toast.makeText(context, "Ulasan berhasil dikirim!", Toast.LENGTH_SHORT).show()
            viewModel.resetSubmitStatus()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tulis Ulasan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, borderCol)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (rating > 0) {
                            viewModel.submitReview(productId, orderId, rating, comment)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = rating > 0 && !state.isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = green,
                        disabledContainerColor = green.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Kirim Ulasan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoadingProduct) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green)
            }
        } else if (state.productError != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.productError ?: "Gagal memuat produk", color = textMuted)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.fetchProductDetail(productId) },
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Coba Lagi")
                }
            }
        } else {
            val product = state.selectedProduct
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Info Produk
                if (product != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            val imgUrl = product.imageUrl
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                if (!imgUrl.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imgUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary,
                                    maxLines = 2
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "UMKM Binaan LokalMart",
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }
                    }
                }

                // Card Rating Bintang
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Bagaimana kualitas produk ini?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                val isSelected = i <= rating
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Bintang $i",
                                    tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { rating = i }
                                )
                            }
                        }

                        val ratingLabel = when (rating) {
                            1 -> "Sangat Buruk"
                            2 -> "Buruk"
                            3 -> "Cukup"
                            4 -> "Baik"
                            5 -> "Sangat Baik"
                            else -> "Pilih Rating Bintang"
                        }
                        Text(
                            text = ratingLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rating > 0) Color(0xFFF59E0B) else textMuted
                        )
                    }
                }

                // Card Komentar/Ulasan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Tulis Komentar Anda",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            placeholder = { Text("Ceritakan pengalaman Anda menggunakan produk ini...", fontSize = 13.sp, color = textMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green,
                                unfocusedBorderColor = borderCol
                            )
                        )
                    }
                }

                state.submitError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
