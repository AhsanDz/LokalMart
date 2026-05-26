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

    // Filter state
    val selectedCategory: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val selectedLocation: String? = null,
    val sortBy: SortOption = SortOption.TERLARIS,
    val showFilterSheet: Boolean = false,

    // UI helper
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

    // Debounce search saat user mengetik
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

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        _queryFlow.value = query
    }

    fun onSearch() {
        performSearch(_uiState.value.query)
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "", results = emptyList(), hasSearched = false, error = null) }
        _queryFlow.value = ""
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        performSearch(_uiState.value.query)
    }

    fun onPriceRangeSelected(min: Double?, max: Double?) {
        _uiState.update { it.copy(minPrice = min, maxPrice = max) }
        performSearch(_uiState.value.query)
    }

    fun onLocationSelected(location: String?) {
        _uiState.update { it.copy(selectedLocation = location) }
        performSearch(_uiState.value.query)
    }

    fun onSortSelected(sort: SortOption) {
        _uiState.update { it.copy(sortBy = sort) }
        performSearch(_uiState.value.query)
    }

    fun toggleFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = !it.showFilterSheet) }
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                minPrice = null,
                maxPrice = null,
                selectedLocation = null,
                sortBy = SortOption.TERLARIS
            )
        }
        performSearch(_uiState.value.query)
    }

    private fun performSearch(query: String) {
        val state = _uiState.value
        viewModelScope.launch {
            repository.searchProducts(
                query = query,
                category = state.selectedCategory,
                minPrice = state.minPrice,
                maxPrice = state.maxPrice,
                location = state.selectedLocation,
                sortBy = state.sortBy
            ).collect { resource ->
                when (resource) {
                    Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null, hasSearched = true)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = resource.data,
                            totalResults = resource.data.size,
                            hasSearched = true
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