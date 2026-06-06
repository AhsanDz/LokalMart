package com.kelompok4.lokalmart.feature.admin.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberAsyncImagePainter
import com.kelompok4.lokalmart.feature.admin.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreVerificationScreen(
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

    var showDialog by remember { mutableStateOf(false) }
    var selectedStoreId by remember { mutableStateOf("") }
    var isApproveAction by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPendingStores()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            Toast.makeText(context, "Verifikasi toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
            viewModel.resetActionStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Antrean Verifikasi Toko", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
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
            if (state.isLoading && state.pendingStores.isEmpty()) {
                CircularProgressIndicator(color = green, modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.error ?: "Gagal memuat toko", color = textMuted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadPendingStores() }, colors = ButtonDefaults.buttonColors(containerColor = green)) {
                        Text("Coba Lagi")
                    }
                }
            } else if (state.pendingStores.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Tidak ada antrean verifikasi toko baru.",
                        color = textMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.pendingStores) { store ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        if (!store.logoUrl.isNullOrBlank()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(store.logoUrl),
                                                contentDescription = store.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF94A3B8)), contentAlignment = Alignment.Center) {
                                                Text("🏪", fontSize = 20.sp)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = store.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEFF6FF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = store.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                        }
                                    }
                                }
                                
                                if (!store.description.isNullOrBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(text = "Deskripsi:", fontSize = 11.sp, color = textMuted, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Text(text = store.description, fontSize = 12.sp, color = textPrimary, lineHeight = 18.sp)
                                }

                                Spacer(Modifier.height(10.dp))
                                Text(text = "Alamat:", fontSize = 11.sp, color = textMuted, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                Text(text = store.address, fontSize = 12.sp, color = textPrimary)

                                if (!store.contactPhone.isNullOrBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Text(text = "Nomor Telepon:", fontSize = 11.sp, color = textMuted, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Text(text = store.contactPhone, fontSize = 12.sp, color = textPrimary)
                                }

                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedStoreId = store.id
                                            isApproveAction = false
                                            notes = ""
                                            showDialog = true
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = red),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, red),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Tolak", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Button(
                                        onClick = {
                                            selectedStoreId = store.id
                                            isApproveAction = true
                                            notes = ""
                                            showDialog = true
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = green),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Setujui", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal AlertDialog for verification note input
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (isApproveAction) "Setujui Pengajuan Toko" else "Tolak Pengajuan Toko",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isApproveAction) {
                            "Berikan catatan verifikasi persetujuan (opsional):"
                        } else {
                            "Berikan alasan penolakan pendaftaran toko (opsional):"
                        },
                        fontSize = 13.sp,
                        color = textMuted
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Tulis catatan admin di sini...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isApproveAction) green else red,
                            unfocusedBorderColor = borderCol
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val status = if (isApproveAction) "approved" else "rejected"
                        viewModel.verifyStore(selectedStoreId, status, notes)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isApproveAction) green else red)
                ) {
                    Text("Konfirmasi", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = textMuted)
                }
            },
            shape = RoundedCornerShape(14.dp)
        )
    }
}
