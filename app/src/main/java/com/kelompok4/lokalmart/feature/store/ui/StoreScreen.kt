package com.kelompok4.lokalmart.feature.store.ui


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kelompok4.lokalmart.feature.store.data.StoreFormModel
import com.kelompok4.lokalmart.feature.store.data.kategoriUsahaList
import com.kelompok4.lokalmart.feature.store.viewmodel.NavigationEvent
import com.kelompok4.lokalmart.feature.store.viewmodel.StoreVm

private val GreenPrimary   = Color(0xFF2D9B4F)
private val GreenLight     = Color(0xFFE8F5ED)
private val TextPrimary    = Color(0xFF111111)
private val TextSecondary  = Color(0xFF757575)
private val TextHint       = Color(0xFFAAAAAA)
private val BorderColor    = Color(0xFFDDDDDD)
private val BgPage         = Color(0xFFFFFFFF)
private val ErrorColor     = Color(0xFFD32F2F)

@Composable
fun StoreScreen(
    vm             : StoreVm = hiltViewModel(),
    onNavigateBack : () -> Unit = {},
    onNavigateNext : () -> Unit = {}
) {
    val formState          by vm.formState.collectAsState()
    val isKategoriExpanded by vm.isKategoriExpanded.collectAsState()
    val fieldErrors        by vm.fieldErrors.collectAsState()
    val navigationEvent    by vm.navigationEvent.collectAsState()
    val isRegistering      by vm.isRegistering.collectAsState()
    val registerError      by vm.registerError.collectAsState()

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateBack -> { onNavigateBack(); vm.onNavigationEventConsumed() }
            is NavigationEvent.NavigateToNext -> { onNavigateNext(); vm.onNavigationEventConsumed() }
            null -> Unit
        }
    }

    StoreContent(
        formState          = formState,
        isKategoriExpanded = isKategoriExpanded,
        fieldErrors        = fieldErrors,
        isRegistering      = isRegistering,
        registerError      = registerError,
        onLogoSelected     = vm::onLogoSelected,
        onNamaChanged      = vm::onNamaUsahaChanged,
        onKategoriToggle   = vm::onKategoriToggle,
        onKategoriDismiss  = vm::onKategoriDismiss,
        onKategoriSelected = vm::onKategoriSelected,
        onDeskripsiChanged = vm::onDeskripsiChanged,
        onAlamatChanged    = vm::onAlamatChanged,
        onNomorWAChanged   = vm::onNomorWAChanged,
        onKembali          = vm::onKembaliClicked,
        onLanjut           = vm::onLanjutClicked
    )
}

@Composable
fun StoreContent(
    formState          : StoreFormModel,
    isKategoriExpanded : Boolean,
    fieldErrors        : Map<String, String>,
    isRegistering      : Boolean,
    registerError      : String?,
    onLogoSelected     : (Uri?, ByteArray?) -> Unit,
    onNamaChanged      : (String) -> Unit,
    onKategoriToggle   : () -> Unit,
    onKategoriDismiss  : () -> Unit,
    onKategoriSelected : (String) -> Unit,
    onDeskripsiChanged : (String) -> Unit,
    onAlamatChanged    : (String) -> Unit,
    onNomorWAChanged   : (String) -> Unit,
    onKembali          : () -> Unit,
    onLanjut           : () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .padding(horizontal = 16.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            StoreTopBar()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(top = 16.dp, bottom = 26.dp)
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    StoreInfoBanner()

                    StoreLogoSection(
                        logoUri        = formState.logoUri,
                        onLogoSelected = onLogoSelected
                    )

                    StoreTextField(
                        label        = "Nama usaha",
                        value        = formState.namaUsaha,
                        onChange     = onNamaChanged,
                        placeholder  = "Masukkan nama usaha",
                        errorMessage = fieldErrors["namaUsaha"]
                    )

                    StoreKategoriDropdown(
                        selected       = formState.kategoriUsaha,
                        isExpanded     = isKategoriExpanded,
                        onToggle       = onKategoriToggle,
                        onDismiss      = onKategoriDismiss,
                        onItemSelected = onKategoriSelected,
                        errorMessage   = fieldErrors["kategoriUsaha"]
                    )

                    StoreDeskripsiField(
                        value    = formState.deskripsiSingkat,
                        onChange = onDeskripsiChanged
                    )

                    StoreTextField(
                        label        = "Alamat lengkap",
                        value        = formState.alamatLengkap,
                        onChange     = onAlamatChanged,
                        placeholder  = "Masukkan alamat lengkap toko",
                        errorMessage = fieldErrors["alamatLengkap"]
                    )

                    StoreTextField(
                        label        = "Nomor WhatsApp",
                        value        = formState.nomorWhatsApp,
                        onChange     = onNomorWAChanged,
                        placeholder  = "Contoh: 0812-3456-7890",
                        keyboardType = KeyboardType.Phone,
                        errorMessage = fieldErrors["nomorWhatsApp"]
                    )

                    if (registerError != null) {
                        Text(
                            text = registerError,
                            color = ErrorColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                    }

                    StoreBottomBar(
                        onKembali = onKembali,
                        onLanjut  = onLanjut,
                        enabled   = !isRegistering
                    )
                }
            }
        }

        if (isRegistering) {
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

// Top Bar
@Composable
private fun StoreTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 34.dp)
    ) {
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Kembali",
            tint               = TextPrimary,
            modifier           = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
        )
        Text(
            text       = "Buka Toko",
            fontSize   = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary,
            modifier   = Modifier.align(Alignment.Center)
        )
    }
    HorizontalDivider(color = BorderColor, thickness = 1.dp)
}

// ─────────────────────────────────────────────────────────────────────────────
// Info Banner hijau
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StoreInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GreenLight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier         = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(14.dp)
            )
        }
        Text(
            text       = "Toko akan diverifikasi admin (1–2 hari kerja). Kamu tetap bisa belanja selama menunggu.",
            fontSize   = 13.sp,
            lineHeight = 19.sp,
            color      = Color(0xFF2A7A40),
            modifier   = Modifier.weight(1f)
        )
    }
}

// Logo Section
@Composable
private fun StoreLogoSection(
    logoUri        : Uri?,
    onLogoSelected : (Uri?, ByteArray?) -> Unit
) {
    val context  = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                onLogoSelected(uri, bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text       = "Logo toko",
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEEEEE))
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (logoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(logoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Logo toko",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "🏪", fontSize = 28.sp)
                }
            }

            OutlinedButton(
                onClick        = { launcher.launch("image/*") },
                shape          = RoundedCornerShape(8.dp),
                border         = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                colors         = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = "Ganti foto",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Generic OutlinedTextField
@Composable
fun StoreTextField(
    label        : String,
    value        : String,
    onChange     : (String) -> Unit,
    placeholder  : String = "",
    keyboardType : KeyboardType = KeyboardType.Text,
    errorMessage : String? = null,
    singleLine   : Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary
        )
        OutlinedTextField(
            value           = value,
            onValueChange   = onChange,
            placeholder     = { Text(placeholder, fontSize = 14.sp, color = TextHint) },
            singleLine      = singleLine,
            isError         = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape           = RoundedCornerShape(10.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                errorBorderColor     = ErrorColor,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary
            ),
            textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
            modifier  = Modifier.fillMaxWidth()
        )
        if (errorMessage != null) {
            Text(errorMessage, fontSize = 12.sp, color = ErrorColor)
        }
    }
}

// Kategori Dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreKategoriDropdown(
    selected       : String,
    isExpanded     : Boolean,
    onToggle       : () -> Unit,
    onDismiss      : () -> Unit,
    onItemSelected : (String) -> Unit,
    errorMessage   : String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = "Kategori usaha",
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary
        )
        ExposedDropdownMenuBox(
            expanded         = isExpanded,
            onExpandedChange = { onToggle() }
        ) {
            OutlinedTextField(
                value         = selected,
                onValueChange = {},
                readOnly      = true,
                placeholder   = { Text("Pilih kategori", fontSize = 14.sp, color = TextHint) },
                trailingIcon  = {
                    Icon(
                        imageVector        = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint               = TextSecondary
                    )
                },
                isError   = errorMessage != null,
                shape     = RoundedCornerShape(10.dp),
                colors    = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GreenPrimary,
                    unfocusedBorderColor = BorderColor,
                    errorBorderColor     = ErrorColor,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
                modifier  = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded         = isExpanded,
                onDismissRequest = onDismiss,
                modifier         = Modifier.background(Color.White)
            ) {
                kategoriUsahaList.forEach { item ->
                    DropdownMenuItem(
                        text    = {
                            Text(
                                text  = item,
                                fontSize = 14.sp,
                                color = if (item == selected) GreenPrimary else TextPrimary
                            )
                        },
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
        if (errorMessage != null) {
            Text(errorMessage, fontSize = 12.sp, color = ErrorColor)
        }
    }
}

// Deskripsi Field + karakter counter
@Composable
private fun StoreDeskripsiField(
    value    : String,
    onChange : (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = "Deskripsi singkat",
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onChange,
            placeholder   = {
                Text(
                    text     = "Ceritakan tentang toko kamu...",
                    fontSize = 14.sp,
                    color    = TextHint
                )
            },
            minLines  = 3,
            maxLines  = 5,
            shape     = RoundedCornerShape(10.dp),
            colors    = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary
            ),
            textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
            modifier  = Modifier.fillMaxWidth()
        )
        Text(
            text      = "${value.length}/200 karakter",
            fontSize  = 12.sp,
            color     = TextSecondary,
            textAlign = TextAlign.Start
        )
    }
}

// Bottom Action Bar — Kembali & Lanjut
@Composable
private fun StoreBottomBar(
    modifier  : Modifier = Modifier,
    onKembali : () -> Unit,
    onLanjut  : () -> Unit,
    enabled   : Boolean = true
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp), // Padding langsung nempel ke background halaman
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick        = onKembali,
            enabled        = enabled,
            shape          = RoundedCornerShape(50.dp),
            border         = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            colors         = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier       = Modifier.weight(1f)
        ) {
            Text(
                text       = "Kembali",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick        = onLanjut,
            enabled        = enabled,
            shape          = RoundedCornerShape(50.dp),
            colors         = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor   = Color.White
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier       = Modifier.weight(2f)
        ) {
            Text(
                text       = "Lanjut",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

// Preview (tanpa Hilt — pakai StoreContent langsung)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StoreScreenPreview() {
    MaterialTheme {
        StoreContent(
            formState = StoreFormModel(
                namaUsaha        = "Kriya Sari Craft",
                kategoriUsaha    = "Fashion & Aksesoris",
                deskripsiSingkat = "Tas anyaman rotan handmade. Pengrajin lokal Yogyakarta — bahan rotan alami pilihan.",
                alamatLengkap    = "Jl. Kriya Lokal No. 12, Yogyakarta",
                nomorWhatsApp    = "0812-3456-7890"
            ),
            isKategoriExpanded = false,
            fieldErrors        = emptyMap(),
            isRegistering      = false,
            registerError      = null,
            onLogoSelected     = { _, _ -> },
            onNamaChanged      = {},
            onKategoriToggle   = {},
            onKategoriDismiss  = {},
            onKategoriSelected = {},
            onDeskripsiChanged = {},
            onAlamatChanged    = {},
            onNomorWAChanged   = {},
            onKembali          = {},
            onLanjut           = {}
        )
    }
}

