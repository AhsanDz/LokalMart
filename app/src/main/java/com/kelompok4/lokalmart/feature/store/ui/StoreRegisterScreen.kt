package com.kelompok4.lokalmart.feature.store.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.core.common.components.LokalMartPrimaryButton
import com.kelompok4.lokalmart.core.common.theme.Green100
import com.kelompok4.lokalmart.core.common.theme.Green500
import com.kelompok4.lokalmart.core.common.theme.Green600
import com.kelompok4.lokalmart.feature.store.viewmodel.StoreViewModel

@Composable
fun StoreRegisterScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: StoreViewModel = hiltViewModel()
) {
    val state by viewModel.registerState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            // Success handler
        }
    }

    if (state.isSuccess) {
        StoreRegisterSuccessContent(
            onNavigateBack = {
                viewModel.resetRegisterState()
                onSuccess()
            }
        )
    } else {
        StoreRegisterFormContent(
            state = state,
            categories = viewModel.storeCategories,
            onNameChange = viewModel::onNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAddressChange = viewModel::onAddressChange,
            onContactPhoneChange = viewModel::onContactPhoneChange,
            onCategoryChange = viewModel::onCategoryChange,
            onSubmit = viewModel::registerStore,
            onNavigateBack = onNavigateBack
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Content
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreRegisterFormContent(
    state: com.kelompok4.lokalmart.feature.store.viewmodel.StoreRegisterUiState,
    categories: List<String>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onContactPhoneChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ===== Top bar dengan back button + centered title =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = textPrimary
                )
            }
            Text(
                text = "Buka Toko",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // ===== Verification Info Banner =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF0FDF4))
                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = green,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Toko akan diverifikasi admin (1-2 hari kerja). Kamu tetap bisa belanja selama menunggu.",
                    fontSize = 12.sp,
                    color = Color(0xFF15803D),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ===== Logo Toko Placeholder =====
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = textMuted,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Logo Toko",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Ganti foto",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = green,
                modifier = Modifier.clickable { /* upload logo */ }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ===== Form Fields =====
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nama Toko
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Nama usaha *")
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = { Text("Contoh: Kriya Sari Craft") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = green
                    ),
                    singleLine = true,
                    enabled = !state.isLoading
                )
            }

            // Kategori (dropdown)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Kategori usaha *")
                CategoryDropdown(
                    selectedCategory = state.category,
                    categories = categories,
                    onCategorySelected = onCategoryChange,
                    enabled = !state.isLoading
                )
            }

            // Deskripsi
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Deskripsi singkat *")
                OutlinedTextField(
                    value = state.description,
                    onValueChange = {
                        if (it.length <= 200) onDescriptionChange(it)
                    },
                    placeholder = { Text("Deskripsikan produk unik yang Anda jual...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = green
                    ),
                    maxLines = 4,
                    enabled = !state.isLoading
                )
                Text(
                    "${state.description.length} / 200 karakter",
                    fontSize = 11.sp,
                    color = textMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Alamat Lengkap
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Alamat lengkap *")
                OutlinedTextField(
                    value = state.address,
                    onValueChange = onAddressChange,
                    placeholder = { Text("Alamat lengkap fisik toko") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = green
                    ),
                    singleLine = true,
                    enabled = !state.isLoading
                )
            }

            // Nomor HP WhatsApp
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Nomor WhatsApp *")
                OutlinedTextField(
                    value = state.contactPhone.removePrefix("+62").removePrefix("62"),
                    onValueChange = onContactPhoneChange,
                    placeholder = { Text("8xxxxxxxxx") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                        ) {
                            Text(
                                "+62",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = green
                    ),
                    singleLine = true,
                    enabled = !state.isLoading
                )
            }

            // Error message
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Split Action Buttons (Kembali & Daftar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                    enabled = !state.isLoading
                ) {
                    Text("Kembali", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }

                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green),
                    enabled = !state.isLoading
                ) {
                    Text("Lanjut", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StoreRegisterSuccessContent(onNavigateBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Green100)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Green500,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Toko Berhasil Didaftarkan!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Toko Anda sedang dalam proses verifikasi oleh admin. " +
                    "Kami akan memberitahu Anda setelah toko disetujui.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFFFEF3C7))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "⏳ Menunggu Verifikasi",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
            )
        }

        Spacer(Modifier.height(32.dp))

        LokalMartPrimaryButton(
            text = "Lihat Toko Saya",
            onClick = onNavigateBack
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Dropdown
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory.ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = "Pilih kategori toko",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF16A34A),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                disabledBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334155)
    )
}
