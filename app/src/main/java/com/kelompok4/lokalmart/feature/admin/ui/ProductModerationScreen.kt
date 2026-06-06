package com.kelompok4.lokalmart.feature.admin.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kelompok4.lokalmart.feature.admin.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductModerationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val green = Color(0xFF16A34A)
    val red = Color(0xFFEF4444)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)
    val borderCol = Color(0xFFE2E8F0)
    val cardBg = Color(0xFFFFFFFF)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProducts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderasi Produk", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
        ) {
            if (state.isLoading && state.products.isEmpty()) {
                CircularProgressIndicator(color = green, modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.error ?: "Gagal memuat produk", color = textMuted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProducts() }, colors = ButtonDefaults.buttonColors(containerColor = green)) {
                        Text("Coba Lagi")
                    }
                }
            } else if (state.products.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Tidak ada produk terdaftar.",
                        color = textMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.products) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Toko: ${product.stores?.name ?: "Toko Lokal"}",
                                        fontSize = 11.sp,
                                        color = textMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = rupiahFormat.format(product.price).replace("Rp", "Rp "),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = green
                                        )
                                        Text(
                                            text = "Stok: ${product.stock}",
                                            fontSize = 12.sp,
                                            color = textMuted
                                        )
                                    }
                                }

                                Spacer(Modifier.width(16.dp))

                                // Status Badge & Toggle Button
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = if (product.isActive) green.copy(alpha = 0.12f) else red.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = if (product.isActive) "Aktif" else "Ditangguhkan",
                                            color = if (product.isActive) green else red,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.toggleProductStatus(product.id, !product.isActive)
                                            val action = if (product.isActive) "menangguhkan" else "mengaktifkan"
                                            Toast.makeText(context, "Berhasil $action produk!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (product.isActive) red else green
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = if (product.isActive) "Suspend" else "Aktifkan",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
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
}
