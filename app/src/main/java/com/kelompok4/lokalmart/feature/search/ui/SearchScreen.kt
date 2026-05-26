package com.kelompok4.lokalmart.feature.search.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.*
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

// ─────────────────────────────────────────────────────────────────────────────
// SearchScreen — Root Composable
// ─────────────────────────────────────────────────────────────────────────────

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

    Scaffold(
        containerColor = Color(0xFFFFFFFF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            SearchTopBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {
                    viewModel.onSearch()
                    focusManager.clearFocus()
                },
                onClear = viewModel::clearQuery,
                onNavigateBack = onNavigateBack,
                // FIX: tombol Tune kini membuka filter sheet
                onFilterClick = viewModel::toggleFilterSheet
            )

            FilterChipsRow(
                totalResults = uiState.totalResults,
                hasSearched = uiState.hasSearched,
                selectedCategory = uiState.selectedCategory,
                minPrice = uiState.minPrice,
                maxPrice = uiState.maxPrice,
                selectedLocation = uiState.selectedLocation,
                onCategoryClick = viewModel::toggleFilterSheet,
                onPriceClick = viewModel::toggleFilterSheet,
                onLocationClick = viewModel::toggleFilterSheet
            )

            if (uiState.hasSearched) {
                ResultsHeader(
                    total = uiState.totalResults,
                    sortBy = uiState.sortBy,
                    onSortClick = viewModel::toggleFilterSheet
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> SearchLoadingState()
                    uiState.error != null -> SearchErrorState(uiState.error!!)
                    !uiState.hasSearched -> SearchEmptyPrompt()
                    uiState.results.isEmpty() -> SearchNoResults(uiState.query)
                    else -> SearchResultsList(
                        results = uiState.results,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }

    // FIX: sheet hanya ditampilkan saat showFilterSheet == true
    if (uiState.showFilterSheet) {
        FilterBottomSheet(
            sheetState = sheetState,
            categories = viewModel.categories,
            selectedCategory = uiState.selectedCategory,
            minPrice = uiState.minPrice,
            maxPrice = uiState.maxPrice,
            selectedLocation = uiState.selectedLocation,
            selectedSort = uiState.sortBy,
            onCategorySelected = viewModel::onCategorySelected,
            onPriceRangeSelected = viewModel::onPriceRangeSelected,
            onLocationSelected = viewModel::onLocationSelected,
            onSortSelected = viewModel::onSortSelected,
            onClearAll = viewModel::clearAllFilters,
            onDismiss = viewModel::toggleFilterSheet
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
    // FIX: parameter onFilterClick ditambahkan (sebelumnya onClick kosong)
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
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kembali",
                tint = Color(0xFF0F172A)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            BasicSearchField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                modifier = Modifier.weight(1f)
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        // FIX: onClick sekarang memanggil onFilterClick, bukan lambda kosong
        IconButton(onClick = onFilterClick) {
            Icon(
                // FIX: ganti Tune (extended icon) dengan FilterList (core icon)
                // Jika sudah pakai material-icons-extended, boleh kembali ke Icons.Default.Tune
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = Color(0xFF0F172A)
            )
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
        textStyle = LocalTextStyle.current.copy(
            color = Color(0xFF0F172A),
            fontSize = 13.sp
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch()
            focusManager.clearFocus()
        }),
        decorationBox = { innerTextField ->
            Box {
                if (query.isEmpty()) {
                    Text(
                        "Cari produk atau toko...",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
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
    onCategoryClick: () -> Unit,
    onPriceClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    val activeGreen   = Color(0xFF16A34A)
    val activeBg      = Color(0xFFDCFCE7)
    val inactiveText  = Color(0xFF64748B)
    val inactiveBorder = Color(0xFFE2E8F0)

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
                Text(
                    "Semua · $totalResults",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // FIX: chip Kategori kini punya Spacer konsisten di kanan
        FilterChip(
            label = selectedCategory ?: "Kategori",
            isActive = selectedCategory != null,
            onClick = onCategoryClick,
            activeColor = activeGreen,
            activeBg = activeBg,
            inactiveText = inactiveText,
            inactiveBorder = inactiveBorder
        )
        Spacer(Modifier.width(6.dp))

        val priceLabel = when {
            minPrice != null && maxPrice != null -> "Rp ${formatPrice(minPrice)}–${formatPrice(maxPrice)}"
            minPrice != null -> "> Rp ${formatPrice(minPrice)}"
            maxPrice != null -> "< Rp ${formatPrice(maxPrice)}"
            else -> "Harga"
        }
        FilterChip(
            label = priceLabel,
            isActive = minPrice != null || maxPrice != null,
            onClick = onPriceClick,
            activeColor = activeGreen,
            activeBg = activeBg,
            inactiveText = inactiveText,
            inactiveBorder = inactiveBorder
        )
        Spacer(Modifier.width(6.dp))

        FilterChip(
            label = selectedLocation ?: "Lokasi",
            isActive = selectedLocation != null,
            onClick = onLocationClick,
            activeColor = activeGreen,
            activeBg = activeBg,
            inactiveText = inactiveText,
            inactiveBorder = inactiveBorder
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    activeBg: Color,
    inactiveText: Color,
    inactiveBorder: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isActive) activeBg else Color.Transparent)
            .border(1.dp, if (isActive) Color.Transparent else inactiveBorder, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (isActive) activeColor else inactiveText,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.width(4.dp))
        // FIX: ganti ExpandMore (extended icon) dengan KeyboardArrowDown (core icon)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (isActive) activeColor else inactiveText,
            modifier = Modifier.size(14.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Results Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResultsHeader(
    total: Int,
    sortBy: SortOption,
    onSortClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            "$total hasil ditemukan",
            color = Color(0xFF64748B),
            fontSize = 11.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onSortClick)
                .padding(4.dp)
        ) {
            Text(
                "${sortBy.label} ▾",
                color = Color(0xFF0F172A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Results List
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(
    results: List<Product>,
    onProductClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(results, key = { it.id }) { product ->
            ProductResultCard(
                product = product,
                onClick = { onProductClick(product.id) }
            )
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
            modifier = Modifier
                .size(79.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFDCFCE7))
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.name,
                color = Color(0xFF0F172A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Rp ${formatPrice(product.price)}",
                color = Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text("★ ${product.rating}", color = Color(0xFFF59E0B), fontSize = 10.sp)
                Text(" · ", color = Color(0xFF64748B), fontSize = 10.sp)
                Text("${product.soldCount} terjual", color = Color(0xFF64748B), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                // FIX: storeName nullable sudah di-handle dengan ?: "-"
                Text(
                    product.storeName ?: "-",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty / Loading / Error States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchEmptyPrompt() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Cari produk UMKM lokal",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ketik nama produk atau nama toko",
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SearchNoResults(query: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // FIX: ganti SearchOff (extended icon) dengan Search + tint lebih pudar (core icon)
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Tidak ditemukan hasil untuk",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )
        Text(
            "\"$query\"",
            color = Color(0xFF0F172A),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Coba kata kunci lain atau hapus filter",
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SearchLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(color = Color(0xFF16A34A))
        Spacer(Modifier.height(12.dp))
        Text(
            "Mencari produk...",
            color = Color(0xFF64748B),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SearchErrorState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            color = Color(0xFF64748B),
            fontSize = 13.sp
        )
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
    selectedSort: SortOption,
    onCategorySelected: (String?) -> Unit,
    onPriceRangeSelected: (Double?, Double?) -> Unit,
    onLocationSelected: (String?) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val priceRanges = listOf(
        Triple("Semua Harga",    null as Double?, null as Double?),
        Triple("< Rp 20rb",      null,            20_000.0),
        Triple("Rp 20rb–50rb",   20_000.0,        50_000.0),
        Triple("Rp 50rb–100rb",  50_000.0,        100_000.0),
        Triple("> Rp 100rb",     100_000.0,       null),
    )
    val locations = listOf("Malang Kota", "Malang Kabupaten", "Batu", "Semua Lokasi")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Filter & Urutkan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text("Reset", color = Color(0xFF16A34A))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Urutkan ──────────────────────────────────────────────────────
            SectionLabel("Urutkan")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // FIX: ganti .entries (Kotlin 1.9+) dengan .values() agar kompatibel lebih luas
                SortOption.values().forEach { sort ->
                    FilterPill(
                        label = sort.label,
                        isSelected = selectedSort == sort,
                        onClick = { onSortSelected(sort) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Kategori ─────────────────────────────────────────────────────
            SectionLabel("Kategori")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill(
                    label = "Semua",
                    isSelected = selectedCategory == null,
                    onClick = { onCategorySelected(null) }
                )
                categories.forEach { cat ->
                    FilterPill(
                        label = cat,
                        isSelected = selectedCategory == cat,
                        onClick = { onCategorySelected(cat) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Rentang Harga ────────────────────────────────────────────────
            SectionLabel("Rentang Harga")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priceRanges.forEach { (label, min, max) ->
                    FilterPill(
                        label = label,
                        isSelected = minPrice == min && maxPrice == max,
                        onClick = { onPriceRangeSelected(min, max) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Lokasi ───────────────────────────────────────────────────────
            SectionLabel("Lokasi")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                locations.forEach { loc ->
                    val isSemua = loc == "Semua Lokasi"
                    FilterPill(
                        label = loc,
                        isSelected = if (isSemua) selectedLocation == null else selectedLocation == loc,
                        onClick = { onLocationSelected(if (isSemua) null else loc) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tombol Terapkan
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
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
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF0F172A)
    )
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
            .border(
                1.dp,
                if (isSelected) Color(0xFF16A34A) else Color.Transparent,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF16A34A) else Color(0xFF64748B),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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