package com.kelompok4.lokalmart.feature.catalog.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.ProductImage
import com.kelompok4.lokalmart.data.model.Review
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.catalog.data.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: Product? = null,
    val images: List<ProductImage> = emptyList(),
    val store: Store? = null,
    val reviews: List<Review> = emptyList(),
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val quantity: Int = 1,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: String = savedStateHandle["productId"] ?: ""

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        if (productId.isNotBlank()) {
            loadProductDetail()
        } else {
            _uiState.update {
                it.copy(isLoading = false, error = "ID produk tidak valid")
            }
        }
    }

    fun loadProductDetail() {
        viewModelScope.launch {
            repository.getProductDetail(productId).collect { resource ->
                when (resource) {
                    Resource.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        val data = resource.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                product = data.product,
                                images = data.images,
                                store = data.store,
                                reviews = data.reviews,
                                averageRating = data.averageRating,
                                totalReviews = data.totalReviews,
                                error = null,
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                }
            }
        }
    }

    fun incrementQuantity() {
        val current = _uiState.value
        val maxStock = current.product?.stock ?: 1
        if (current.quantity < maxStock) {
            _uiState.update { it.copy(quantity = it.quantity + 1) }
        }
    }

    fun decrementQuantity() {
        if (_uiState.value.quantity > 1) {
            _uiState.update { it.copy(quantity = it.quantity - 1) }
        }
    }
}
