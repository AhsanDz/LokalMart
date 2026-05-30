package com.kelompok4.lokalmart.feature.search.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.search.data.SortOption
import com.kelompok4.lokalmart.feature.search.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onProductClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            SearchTopBar(
                query          = uiState.query,
                onQueryChange  = viewModel::onQueryChange,
                onSearch       = { viewModel.onSearch(); focusManager.clearFocus() },
                onClear        = viewModel::clearQuery,
                onNavigateBack = onNavigateBack,
                onFilterClick  = viewModel::openFilterSheet
            )

            FilterChipsRow(
                totalResults     = uiState.totalResults,
                hasSearched      = uiState.hasSearched,
                selectedCategory = uiState.selectedCategory,
                minPrice         = uiState.minPrice,
                maxPrice         = uiState.maxPrice,
                selectedLocation = uiState.selectedLocation,
                onChipClick      = viewModel::openFilterSheet
            )

            if (uiState.hasSearched) {
                ResultsHeader(
                    total       = uiState.totalResults,
                    sortBy      = uiState.sortBy,
                    onSortClick = viewModel::openFilterSheet
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading         -> SearchLoadingState()
                    uiState.error != null     -> SearchErrorState(uiState.error!!)
                    !uiState.hasSearched      -> SearchEmptyPrompt()
                    uiState.results.isEmpty() -> SearchNoResults(uiState.query)
                    else -> SearchResultsList(
                        results        = uiState.results,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }

    if (uiState.showFilterSheet) {
        FilterBottomSheet(
            sheetState           = sheetState,
            categories           = viewModel.categories,
            selectedCategory     = uiState.draftCategory,
            minPrice             = uiState.draftMinPrice,
            maxPrice             = uiState.draftMaxPrice,
            selectedLocation     = uiState.draftLocation,
            selectedRadiusKm     = uiState.draftRadiusKm,
            selectedSort         = uiState.draftSortBy,
            onCategorySelected   = viewModel::onDraftCategorySelected,
            onPriceRangeSelected = viewModel::onDraftPriceRangeSelected,
            onLocationSelected   = viewModel::onDraftLocationSelected,
            onRadiusSelected     = viewModel::onDraftRadiusSelected,
            onSortSelected       = viewModel::onDraftSortSelected,
            onResetAll           = viewModel::resetDraftFilters,
            onApply              = viewModel::applyFilters,
            onDismiss            = viewModel::dismissFilterSheet
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onNavigateBack: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF0F172A))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            BasicSearchField(query = query, onQueryChange = onQueryChange, onSearch = onSearch, modifier = Modifier.weight(1f))
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF0F172A))
        }
    }
}

@Composable
private fun BasicSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(); focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            Box {
                if (query.isEmpty()) Text("Cari produk atau toko...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                innerTextField()
            }
        },
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter Chips Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FilterChipsRow(
    totalResults: Int,
    hasSearched: Boolean,
    selectedCategory: String?,
    minPrice: Double?,
    maxPrice: Double?,
    selectedLocation: String?,
    onChipClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (hasSearched) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Semua · $totalResults", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        FilterChip(label = selectedCategory ?: "Kategori", isActive = selectedCategory != null, onClick = onChipClick)
        Spacer(Modifier.width(6.dp))

        val priceLabel = when {
            minPrice != null && maxPrice != null -> "Rp ${formatPrice(minPrice)}–${formatPrice(maxPrice)}"
            minPrice != null -> "> Rp ${formatPrice(minPrice)}"
            maxPrice != null -> "< Rp ${formatPrice(maxPrice)}"
            else -> "Harga"
        }
        FilterChip(label = priceLabel, isActive = minPrice != null || maxPrice != null, onClick = onChipClick)
        Spacer(Modifier.width(6.dp))

        FilterChip(label = selectedLocation ?: "Lokasi", isActive = selectedLocation != null, onClick = onChipClick)
    }
}

@Composable
private fun FilterChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
            .border(1.dp, if (isActive) Color(0xFF16A34A) else Color.Transparent, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color      = if (isActive) Color(0xFF16A34A) else Color(0xFF64748B),
            fontSize   = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint     = if (isActive) Color(0xFF16A34A) else Color(0xFF64748B),
            modifier = Modifier.size(14.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Results Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResultsHeader(total: Int, sortBy: SortOption, onSortClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text("$total hasil ditemukan", color = Color(0xFF64748B), fontSize = 11.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onSortClick).padding(4.dp)
        ) {
            Text("${sortBy.label} ▾", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Results List & Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(results: List<Product>, onProductClick: (String) -> Unit) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        items(results, key = { it.id }) { product ->
            ProductResultCard(product = product, onClick = { onProductClick(product.id) })
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ProductResultCard(product: Product, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(11.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(79.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFDCFCE7))
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 4.dp))
            Text("Rp ${formatPrice(product.price)}", color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Text("★ ${product.rating}", color = Color(0xFFF59E0B), fontSize = 10.sp)
                Text(" · ", color = Color(0xFF64748B), fontSize = 10.sp)
                Text("${product.soldCount} terjual", color = Color(0xFF64748B), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(3.dp))
                Text(product.storeName ?: "-", color = Color(0xFF64748B), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty / Loading / Error States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchEmptyPrompt() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Cari produk UMKM lokal", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text("Ketik nama produk atau nama toko", color = Color(0xFFCBD5E1), fontSize = 12.sp)
    }
}

@Composable
private fun SearchNoResults(query: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Tidak ditemukan hasil untuk", color = Color(0xFF94A3B8), fontSize = 14.sp)
        Text("\"$query\"", color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Coba kata kunci lain atau hapus filter", color = Color(0xFFCBD5E1), fontSize = 12.sp)
    }
}

@Composable
private fun SearchLoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(color = Color(0xFF16A34A))
        Spacer(Modifier.height(12.dp))
        Text("Mencari produk...", color = Color(0xFF64748B), fontSize = 13.sp)
    }
}

@Composable
private fun SearchErrorState(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color(0xFF64748B), fontSize = 13.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    categories: List<String>,
    selectedCategory: String?,
    minPrice: Double?,
    maxPrice: Double?,
    selectedLocation: String?,
    selectedRadiusKm: Int?,
    selectedSort: SortOption,
    onCategorySelected: (String?) -> Unit,
    onPriceRangeSelected: (Double?, Double?) -> Unit,
    onLocationSelected: (String?) -> Unit,
    onRadiusSelected: (Int?) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onResetAll: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val priceRanges = listOf(
        Triple("Semua",         null as Double?, null as Double?),
        Triple("< Rp 20rb",     null,            20_000.0),
        Triple("Rp 20–50rb",    20_000.0,        50_000.0),
        Triple("Rp 50–100rb",   50_000.0,        100_000.0),
        Triple("> Rp 100rb",    100_000.0,       null),
    )
    val locations = listOf("Kota Malang", "Malang Kabupaten", "Batu")
    val radiusOptions = listOf(1, 5, 10, 25)   // km

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // ── Handle + Header ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Text("Filter Pencarian", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                TextButton(onClick = onResetAll) {
                    Text("Reset", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ── Kategori ─────────────────────────────────────────────────────
            SectionLabel("KATEGORI")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val allCats = listOf("Tas", "Pakaian", "Sepatu", "Aksesoris", "Batik & Tenun", "Kerajinan")
                allCats.forEach { cat ->
                    FilterPill(
                        label      = cat,
                        isSelected = selectedCategory == cat,
                        onClick    = {
                            onCategorySelected(if (selectedCategory == cat) null else cat)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Rentang Harga ─────────────────────────────────────────────────
            SectionLabel("RENTANG HARGA")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                priceRanges.forEach { (label, min, max) ->
                    FilterPill(
                        label      = label,
                        isSelected = minPrice == min && maxPrice == max,
                        onClick    = { onPriceRangeSelected(min, max) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Lokasi ────────────────────────────────────────────────────────
            SectionLabel("LOKASI")
            Spacer(Modifier.height(8.dp))

            // Dropdown kota
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded         = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier         = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                OutlinedTextField(
                    value         = selectedLocation ?: "Pilih kota",
                    onValueChange = {},
                    readOnly      = true,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor   = Color(0xFF16A34A)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color(0xFF0F172A)),
                    modifier  = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    locations.forEach { loc ->
                        DropdownMenuItem(
                            text    = { Text(loc, fontSize = 12.sp) },
                            onClick = { onLocationSelected(loc); expanded = false }
                        )
                    }
                    DropdownMenuItem(
                        text    = { Text("Semua Lokasi", fontSize = 12.sp) },
                        onClick = { onLocationSelected(null); expanded = false }
                    )
                }
            }

            // Radius km
            Text("Jarak", fontSize = 12.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                radiusOptions.forEach { km ->
                    FilterPill(
                        label      = if (km == 25) "25 km+" else "$km km",
                        isSelected = selectedRadiusKm == km,
                        onClick    = { onRadiusSelected(if (selectedRadiusKm == km) null else km) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Urutkan ───────────────────────────────────────────────────────
            SectionLabel("URUTKAN")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SortOption.values().forEach { sort ->
                    FilterPill(
                        label      = sort.label,
                        isSelected = selectedSort == sort,
                        onClick    = { onSortSelected(sort) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Tombol Terapkan ───────────────────────────────────────────────
            Button(
                onClick  = onApply,
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text("Terapkan Filter", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared Small Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
}

@Composable
private fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9))
            .border(1.dp, if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize   = 12.sp,
            color      = if (isSelected) Color.White else Color(0xFF334155),
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Util
// ─────────────────────────────────────────────────────────────────────────────

private fun formatPrice(price: Double): String = when {
    price >= 1_000_000 -> "${(price / 1_000_000).toInt()}jt"
    price >= 1_000     -> "${(price / 1_000).toInt()}rb"
    else               -> price.toInt().toString()
}