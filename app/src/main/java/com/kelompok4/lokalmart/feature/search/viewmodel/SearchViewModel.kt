package com.kelompok4.lokalmart.feature.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.search.data.SearchRepository
import com.kelompok4.lokalmart.feature.search.data.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalResults: Int = 0,

    // ── Filter APPLIED ────────────────────────────────────────────────────────
    val selectedCategory: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val selectedLocation: String? = null,
    val radiusKm: Int? = null,
    val sortBy: SortOption = SortOption.TERLARIS,

    // ── Filter DRAFT (saat sheet terbuka) ─────────────────────────────────────
    val draftCategory: String? = null,
    val draftMinPrice: Double? = null,
    val draftMaxPrice: Double? = null,
    val draftLocation: String? = null,
    val draftRadiusKm: Int? = null,
    val draftSortBy: SortOption = SortOption.TERLARIS,

    val showFilterSheet: Boolean = false,
    val hasSearched: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val categories: List<String> = repository.categories

    private val _queryFlow = MutableStateFlow("")

    init {
        _queryFlow
            .debounce(400)
            .onEach { query ->
                if (query.isNotBlank() || _uiState.value.hasSearched) {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Query ─────────────────────────────────────────────────────────────────
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        _queryFlow.value = query
    }

    fun onSearch() { performSearch(_uiState.value.query) }

    fun clearQuery() {
        _uiState.update { it.copy(query = "", results = emptyList(), hasSearched = false, error = null) }
        _queryFlow.value = ""
    }

    // ── Sheet: buka → salin applied ke draft ─────────────────────────────────
    fun openFilterSheet() {
        _uiState.update {
            it.copy(
                showFilterSheet = true,
                draftCategory   = it.selectedCategory,
                draftMinPrice   = it.minPrice,
                draftMaxPrice   = it.maxPrice,
                draftLocation   = it.selectedLocation,
                draftRadiusKm   = it.radiusKm,
                draftSortBy     = it.sortBy,
            )
        }
    }

    // ── Sheet: tutup tanpa apply ──────────────────────────────────────────────
    fun dismissFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    // ── Draft setters ─────────────────────────────────────────────────────────
    fun onDraftCategorySelected(category: String?) =
        _uiState.update { it.copy(draftCategory = category) }

    fun onDraftPriceRangeSelected(min: Double?, max: Double?) =
        _uiState.update { it.copy(draftMinPrice = min, draftMaxPrice = max) }

    fun onDraftLocationSelected(location: String?) =
        _uiState.update { it.copy(draftLocation = location) }

    fun onDraftRadiusSelected(radius: Int?) =
        _uiState.update { it.copy(draftRadiusKm = radius) }

    fun onDraftSortSelected(sort: SortOption) =
        _uiState.update { it.copy(draftSortBy = sort) }

    // ── Terapkan: draft → applied, lalu search ────────────────────────────────
    fun applyFilters() {
        _uiState.update {
            it.copy(
                showFilterSheet  = false,
                selectedCategory = it.draftCategory,
                minPrice         = it.draftMinPrice,
                maxPrice         = it.draftMaxPrice,
                selectedLocation = it.draftLocation,
                radiusKm         = it.draftRadiusKm,
                sortBy           = it.draftSortBy,
            )
        }
        performSearch(_uiState.value.query)
    }

    // ── Reset draft ───────────────────────────────────────────────────────────
    fun resetDraftFilters() {
        _uiState.update {
            it.copy(
                draftCategory = null,
                draftMinPrice = null,
                draftMaxPrice = null,
                draftLocation = null,
                draftRadiusKm = null,
                draftSortBy   = SortOption.TERLARIS,
            )
        }
    }

    // ── Clear semua applied dari luar sheet ───────────────────────────────────
    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                minPrice         = null,
                maxPrice         = null,
                selectedLocation = null,
                radiusKm         = null,
                sortBy           = SortOption.TERLARIS,
            )
        }
        performSearch(_uiState.value.query)
    }

    private fun performSearch(query: String) {
        val state = _uiState.value
        viewModelScope.launch {
            repository.searchProducts(
                query    = query,
                category = state.selectedCategory,
                minPrice = state.minPrice,
                maxPrice = state.maxPrice,
                location = state.selectedLocation,
                sortBy   = state.sortBy
            ).collect { resource ->
                when (resource) {
                    Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null, hasSearched = true)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading    = false,
                            results      = resource.data,
                            totalResults = resource.data.size,
                            hasSearched  = true
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message, hasSearched = true)
                    }
                }
            }
        }
    }
}