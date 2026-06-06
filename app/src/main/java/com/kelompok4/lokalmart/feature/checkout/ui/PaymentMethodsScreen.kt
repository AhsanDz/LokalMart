package com.kelompok4.lokalmart.feature.checkout.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.feature.checkout.data.SavedPaymentMethod
import com.kelompok4.lokalmart.feature.checkout.viewmodel.PaymentMethodViewModel

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val CardBg         = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE2E8F0)
private val GrayBg         = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onNavigateBack: () -> Unit,
    isSelectionMode: Boolean = false,
    viewModel: PaymentMethodViewModel = hiltViewModel()
) {
    val methods by viewModel.paymentMethods.collectAsState()
    val selectedMethod by viewModel.selectedPaymentMethod.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GrayBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) "Pilih Metode Pembayaran" else "Metode Pembayaran",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pembayaran")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (methods.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Metode Pembayaran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Hubungkan akun pembayaran digital atau transfer bank untuk bertransaksi lebih cepat.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(methods, key = { it.id }) { method ->
                        val isSelected = selectedMethod?.id == method.id
                        PaymentMethodItemRow(
                            method = method,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                viewModel.selectPaymentMethod(method)
                                if (isSelectionMode) {
                                    onNavigateBack()
                                }
                            },
                            onDelete = {
                                viewModel.deletePaymentMethod(method.id)
                            }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddPaymentMethodDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { type, name, details, isPrimary ->
                        viewModel.addPaymentMethod(type, name, details, isPrimary)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodItemRow(
    method: SavedPaymentMethod,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected && isSelectionMode) 2.dp else 1.dp,
            color = if (isSelected && isSelectionMode) GreenPrimary else BorderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = when (method.type) {
                    "qris" -> GreenPrimary
                    "gopay" -> Color(0xFF00AED6)
                    else -> Color(0xFF003087)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when (method.type) {
                            "qris" -> "QRIS"
                            "gopay" -> "GP"
                            else -> "BANK"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = method.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (method.isPrimary) {
                        Surface(
                            color = GreenLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Utama",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = method.details,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Metode",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Boolean) -> Unit
) {
    var type by remember { mutableStateOf("bank_transfer") }
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tambah Metode Pembayaran",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Jenis Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("bank_transfer" to "Bank", "gopay" to "GoPay").forEach { (itemType, label) ->
                        val selected = type == itemType
                        Surface(
                            color = if (selected) GreenLight else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { type = itemType }
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                label,
                                color = if (selected) GreenPrimary else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Rekening/Akun (cth: BCA Ahsan)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Nomor Rekening/Telepon") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                    Text("Jadikan Metode Utama", fontSize = 13.sp, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && details.isNotBlank()) {
                        onConfirm(type, name, details, isPrimary)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}
