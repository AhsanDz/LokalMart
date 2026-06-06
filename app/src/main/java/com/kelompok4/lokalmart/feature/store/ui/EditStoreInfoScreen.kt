package com.kelompok4.lokalmart.feature.store.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import com.kelompok4.lokalmart.feature.store.data.kategoriUsahaList
import com.kelompok4.lokalmart.feature.store.viewmodel.EditStoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStoreInfoScreen(
    storeId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditStoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var nameState by remember { mutableStateOf("") }
    var categoryState by remember { mutableStateOf("") }
    var descriptionState by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }
    var phoneState by remember { mutableStateOf("") }

    var isKategoriExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(storeId) {
        viewModel.loadStore(storeId)
    }

    LaunchedEffect(state.store) {
        state.store?.let { store ->
            if (nameState.isEmpty()) nameState = store.name
            if (categoryState.isEmpty()) categoryState = store.category
            if (descriptionState.isEmpty()) descriptionState = store.description ?: ""
            if (addressState.isEmpty()) addressState = store.address
            if (phoneState.isEmpty()) phoneState = store.contactPhone ?: ""
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            onNavigateBack()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                selectedImageUri = uri
                val inputStream = context.contentResolver.openInputStream(uri)
                selectedImageBytes = inputStream?.readBytes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Edit Profil Toko",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(end = 40.dp)
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
                        onClick = {
                            viewModel.updateStore(
                                storeId = storeId,
                                name = nameState,
                                category = categoryState,
                                description = descriptionState.takeIf { it.isNotBlank() },
                                address = addressState,
                                contactPhone = phoneState.takeIf { it.isNotBlank() },
                                logoBytes = selectedImageBytes
                            )
                        },
                        enabled = !state.isLoading && nameState.isNotBlank() && categoryState.isNotBlank() && addressState.isNotBlank()
                    ) {
                        Text(
                            text = "Simpan",
                            color = if (nameState.isNotBlank() && categoryState.isNotBlank() && addressState.isNotBlank()) Green600 else Color(0xFFCBD5E1),
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(10.dp))

                // ===== Logo Edit Section =====
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (!state.store?.logoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = state.store?.logoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "🏪", fontSize = 48.sp)
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
                            contentDescription = "Edit Logo",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Ganti logo toko",
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
                    // Field 1: Nama Toko
                    Column {
                        Text(
                            text = "Nama Usaha",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameState,
                            onValueChange = { nameState = it },
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

                    // Field 2: Kategori Usaha
                    Column {
                        Text(
                            text = "Kategori Usaha",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = isKategoriExpanded,
                            onExpandedChange = { isKategoriExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = categoryState,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
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
                            ExposedDropdownMenu(
                                expanded = isKategoriExpanded,
                                onDismissRequest = { isKategoriExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                kategoriUsahaList.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = category,
                                                fontSize = 14.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                        },
                                        onClick = {
                                            categoryState = category
                                            isKategoriExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Field 3: Deskripsi
                    Column {
                        Text(
                            text = "Deskripsi Toko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = descriptionState,
                            onValueChange = {
                                if (it.length <= 200) descriptionState = it
                            },
                            minLines = 3,
                            maxLines = 5,
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
                            text = "${descriptionState.length} / 200 karakter",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Field 4: Alamat Lengkap
                    Column {
                        Text(
                            text = "Alamat Lengkap Toko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = addressState,
                            onValueChange = { addressState = it },
                            minLines = 2,
                            maxLines = 3,
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

                    // Field 5: WhatsApp
                    Column {
                        Text(
                            text = "Nomor WhatsApp Toko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phoneState,
                            onValueChange = {
                                val filtered = it.filter { c -> c.isDigit() || c == '+' || c == '-' }
                                phoneState = filtered
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                Spacer(Modifier.height(40.dp))
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green600)
                }
            }
        }
    }
}
