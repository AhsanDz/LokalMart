package com.kelompok4.lokalmart.feature.catalog.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
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
import com.kelompok4.lokalmart.feature.catalog.viewmodel.ProductViewModel
import com.kelompok4.lokalmart.feature.store.data.kategoriUsahaList

private val GreenPrimary   = Color(0xFF2D9B4F)
private val TextPrimary    = Color(0xFF1E293B)
private val TextSecondary  = Color(0xFF64748B)
private val BorderColor    = Color(0xFFE2E8F0)
private val CardBg         = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var nameState by remember { mutableStateOf("") }
    var descriptionState by remember { mutableStateOf("") }
    var priceState by remember { mutableStateOf("") }
    var stockState by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var variantState by remember { mutableStateOf("") }

    var isCategoryExpanded by remember { mutableStateOf(false) }

    // Map business category to index/id helper
    val categoriesList = listOf(
        Pair(1, "Pakaian"),
        Pair(2, "Tas"),
        Pair(3, "Sepatu"),
        Pair(4, "Kerajinan"),
        Pair(5, "Aksesoris"),
        Pair(6, "Batik & Tenun"),
        Pair(7, "Dekor Rumah"),
        Pair(8, "Lainnya")
    )

    LaunchedEffect(Unit) {
        viewModel.loadMyStoreForForm()
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetFormSuccess()
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
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tambah Produk Baru",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(end = 40.dp)
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Photo Picker Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.2.dp, BorderColor, RoundedCornerShape(16.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Unggah Foto Produk",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "Gunakan foto rasio 1:1 untuk hasil terbaik",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // 2. Input Fields Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Field 1: Nama Produk
                    Column {
                        Text(
                            text = "Nama Produk",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameState,
                            onValueChange = { nameState = it },
                            placeholder = { Text("Masukkan nama produk") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 2: Kategori
                    Column {
                        Text(
                            text = "Kategori Produk",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = isCategoryExpanded,
                            onExpandedChange = { isCategoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategoryName,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Pilih kategori") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    unfocusedBorderColor = BorderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryExpanded,
                                onDismissRequest = { isCategoryExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categoriesList.forEach { (catId, catName) ->
                                    DropdownMenuItem(
                                        text = { Text(catName) },
                                        onClick = {
                                            selectedCategoryName = catName
                                            selectedCategoryId = catId
                                            isCategoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Field 3: Harga
                    Column {
                        Text(
                            text = "Harga Produk (Rp)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = priceState,
                            onValueChange = { priceState = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Masukkan harga dalam Rupiah") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 4: Stok
                    Column {
                        Text(
                            text = "Stok Produk",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stockState,
                            onValueChange = { stockState = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Masukkan jumlah stok") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 5: Varian
                    Column {
                        Text(
                            text = "Varian Produk (Opsional)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = variantState,
                            onValueChange = { variantState = it },
                            placeholder = { Text("Contoh: Natural, Cokelat, Hitam") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 6: Deskripsi
                    Column {
                        Text(
                            text = "Deskripsi Produk",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = descriptionState,
                            onValueChange = { descriptionState = it },
                            placeholder = { Text("Tuliskan deskripsi produk yang lengkap") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error ?: "Terjadi kesalahan",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                // Save button
                Button(
                    onClick = {
                        val priceVal = priceState.toDoubleOrNull() ?: 0.0
                        val stockVal = stockState.toIntOrNull() ?: 0
                        viewModel.addProduct(
                            name = nameState,
                            description = descriptionState.takeIf { it.isNotBlank() },
                            price = priceVal,
                            stock = stockVal,
                            categoryId = selectedCategoryId,
                            variant = variantState.takeIf { it.isNotBlank() },
                            imageBytes = selectedImageBytes
                        )
                    },
                    enabled = !state.isLoading && nameState.isNotBlank() && priceState.isNotBlank() && stockState.isNotBlank() && selectedCategoryId != null,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Produk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(Modifier.height(20.dp))
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
        }
    }
}
