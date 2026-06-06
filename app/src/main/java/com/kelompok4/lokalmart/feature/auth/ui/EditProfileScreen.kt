package com.kelompok4.lokalmart.feature.auth.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kelompok4.lokalmart.core.common.theme.*
import com.kelompok4.lokalmart.feature.auth.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.editProfileState.collectAsState()
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // State lokal untuk username dan email (karena tidak ada kolom username terpisah di tabel DB, kita buat tiruannya)
    var usernameState by remember { mutableStateOf("") }
    
    // Inisialisasi usernameState saat data profil dimuat
    LaunchedEffect(state.fullName) {
        if (state.fullName.isNotBlank() && usernameState.isBlank()) {
            usernameState = state.fullName.lowercase().replace(" ", "")
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                selectedImageUri = uri
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                viewModel.onAvatarChanged(bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetEditSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            // Header Persis Mockup: 'X' di Kiri, 'Edit Profil' di Tengah, 'Simpan' di Kanan
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Edit Profil",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(end = 40.dp) // Mengimbangi tombol back di kiri agar center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.updateProfile() },
                        enabled = !state.isLoading && state.fullName.isNotBlank()
                    ) {
                        Text(
                            text = "Simpan",
                            color = if (state.fullName.isNotBlank()) Green600 else Color(0xFFCBD5E1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            // ===== Avatar Edit Section =====
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Green600),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!state.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = state.fullName.take(2).uppercase().ifBlank { "SW" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Edit badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 2.dp, end = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "Ganti foto profil",
                fontSize = 13.sp,
                color = Green600,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { launcher.launch("image/*") }
            )

            Spacer(Modifier.height(28.dp))

            // ===== Form Fields =====
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Field 1: Nama lengkap
                Column {
                    FieldLabelText("Nama lengkap")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Green600,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A))
                    )
                }

                // Field 2: Username (Mockup)
                Column {
                    FieldLabelText("Username")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = usernameState,
                        onValueChange = { usernameState = it },
                        singleLine = true,
                        leadingIcon = {
                            Text(
                                text = "@",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Green600,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Username tersedia ✓",
                        fontSize = 11.sp,
                        color = Green600,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Field 3: Bio
                Column {
                    FieldLabelText("Bio")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.bio,
                        onValueChange = {
                            if (it.length <= 150) viewModel.onBioChange(it)
                        },
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Green600,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${state.bio.length} / 150 karakter",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Field 4: Nomor HP
                Column {
                    FieldLabelText("Nomor HP")
                    Spacer(Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Country Code Box
                        Row(
                            modifier = Modifier
                                .width(76.dp)
                                .height(56.dp)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "+62",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Phone Number Input
                        val cleanPhone = state.phone.removePrefix("+62").removePrefix("62")
                        OutlinedTextField(
                            value = cleanPhone,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() || it == '-' }
                                viewModel.onPhoneChange("+62$digits")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            placeholder = { Text("812-3456-7890", fontSize = 14.sp, color = Color(0xFF94A3B8)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Green600,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A))
                        )
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.error ?: "Terjadi kesalahan",
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(color = Green600)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FieldLabelText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334155)
    )
}
