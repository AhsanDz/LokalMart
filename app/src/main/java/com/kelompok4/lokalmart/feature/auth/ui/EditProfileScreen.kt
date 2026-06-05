package com.kelompok4.lokalmart.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.kelompok4.lokalmart.feature.auth.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.editProfileState.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val green = Color(0xFF16A34A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    // Local state for mockup Username field
    var username by remember { mutableStateOf("sari_wulandari") }
    val user = profileState.user
    val initials = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.take(2)?.uppercase() ?: "SW"

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetEditState()
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Edit Profil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveProfile,
                        enabled = !state.isLoading
                    ) {
                        Text(
                            "Simpan",
                            color = green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Centered initials avatar with camera icon overlay
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7))
                        .border(1.dp, Color(0xFFBBF7D0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(green)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Ganti Foto",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ganti foto profil",
                color = green,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* logic upload */ }
            )

            Spacer(Modifier.height(24.dp))

            // ── Form fields ──
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nama Lengkap
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel("Nama lengkap")
                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        placeholder = { Text("Nama lengkap Anda") },
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

                // Username (mock)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel("Username")
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = {
                            Text(
                                "@",
                                color = textMuted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Tersedia",
                                tint = green,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            cursorColor = green
                        ),
                        singleLine = true,
                        enabled = !state.isLoading
                    )
                    Text(
                        "Username tersedia ✓",
                        color = green,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Bio (character counter)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel("Bio")
                    OutlinedTextField(
                        value = state.bio,
                        onValueChange = {
                            if (it.length <= 150) viewModel.onBioChange(it)
                        },
                        placeholder = {
                            Text(
                                "Ceritakan sedikit tentang dirimu...",
                                fontSize = 13.sp,
                                color = textMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            cursorColor = green
                        ),
                        maxLines = 3,
                        enabled = !state.isLoading
                    )
                    Text(
                        "${state.bio.length} / 150 karakter",
                        fontSize = 11.sp,
                        color = textMuted,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Nomor HP with +62 prefix separated
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel("Nomor HP")
                    OutlinedTextField(
                        value = state.phone.removePrefix("+62").removePrefix("62"),
                        onValueChange = { viewModel.onPhoneChange(it) },
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

                // Email (terverifikasi)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel("Email (terverifikasi)")
                    OutlinedTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "TERVERIFIKASI",
                                    color = green,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE2E8F0),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            disabledBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        singleLine = true,
                        enabled = false
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

                LokalMartPrimaryButton(
                    text = "Simpan Perubahan",
                    onClick = viewModel::saveProfile,
                    enabled = !state.isLoading,
                    isLoading = state.isLoading
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334155)
    )
}
