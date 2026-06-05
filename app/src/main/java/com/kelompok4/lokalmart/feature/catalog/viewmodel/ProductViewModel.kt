package com.kelompok4.lokalmart.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Category
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.catalog.data.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCategoryId: Int? = null,
    val isLoading: Boolean = true,
    val isCategoriesLoading: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    /**
     * Muat kategori dan produk saat pertama kali masuk HomeScreen.
     */
    fun loadHome() {
        loadCategories()
        loadProducts()
    }

    /**
     * Pilih kategori — null berarti "Semua".
     */
    fun selectCategory(categoryId: Int?) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadProducts()
    }

    /**
     * Refresh produk (pull-to-refresh).
     */
    fun refreshProducts() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { resource ->
                when (resource) {
                    Resource.Loading -> _uiState.update {
                        it.copy(isCategoriesLoading = true)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isCategoriesLoading = false,
                            categories = resource.data,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isCategoriesLoading = false,
                            error = resource.message,
                        )
                    }
                }
            }
        }
    }

    private fun loadProducts() {
        val categoryId = _uiState.value.selectedCategoryId
        viewModelScope.launch {
            repository.getProducts(categoryId).collect { resource ->
                when (resource) {
                    Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            products = resource.data,
                            error = null,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = resource.message,
                        )
                    }
                }
            }
        }
    }
}
