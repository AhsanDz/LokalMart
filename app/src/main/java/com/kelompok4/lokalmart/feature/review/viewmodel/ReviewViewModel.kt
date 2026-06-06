package com.kelompok4.lokalmart.feature.review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.catalog.data.ProductRepository
import com.kelompok4.lokalmart.feature.review.data.ReviewRepository
import com.kelompok4.lokalmart.feature.review.data.ReviewWithUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    // Fetch product reviews state
    val isLoadingReviews: Boolean = false,
    val reviews: List<ReviewWithUserDto> = emptyList(),
    val reviewsError: String? = null,
    val averageRating: Double = 0.0,
    val star5Count: Int = 0,
    val star4Count: Int = 0,
    val star3Count: Int = 0,
    val star2Count: Int = 0,
    val star1Count: Int = 0,

    // Submit review state
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null,

    // Selected product detail for review form
    val isLoadingProduct: Boolean = false,
    val selectedProduct: Product? = null,
    val productError: String? = null,

    // Already reviewed product IDs for a specific order
    val reviewedProductIds: List<String> = emptyList()
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail produk untuk ditampilkan di form ulasan.
     */
    fun fetchProductDetail(productId: String) {
        viewModelScope.launch {
            productRepository.getProductById(productId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingProduct = true, productError = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoadingProduct = false,
                                selectedProduct = resource.data?.first
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingProduct = false,
                                productError = resource.message ?: "Gagal memuat detail produk"
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Memuat daftar ulasan untuk produk tertentu dan mengagregasi statistik bintang.
     */
    fun fetchProductReviews(productId: String) {
        viewModelScope.launch {
            reviewRepository.getProductReviews(productId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingReviews = true, reviewsError = null) }
                    }
                    is Resource.Success -> {
                        val reviewList = resource.data ?: emptyList()
                        val total = reviewList.size
                        val avg = if (total > 0) reviewList.map { it.rating }.average() else 0.0
                        
                        _uiState.update {
                            it.copy(
                                isLoadingReviews = false,
                                reviews = reviewList,
                                averageRating = avg,
                                star5Count = reviewList.count { r -> r.rating == 5 },
                                star4Count = reviewList.count { r -> r.rating == 4 },
                                star3Count = reviewList.count { r -> r.rating == 3 },
                                star2Count = reviewList.count { r -> r.rating == 2 },
                                star1Count = reviewList.count { r -> r.rating == 1 }
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingReviews = false,
                                reviewsError = resource.message ?: "Gagal memuat ulasan"
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Mengirimkan ulasan baru.
     */
    fun submitReview(productId: String, orderId: String, rating: Int, comment: String?) {
        viewModelScope.launch {
            reviewRepository.submitReview(productId, orderId, rating, comment).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSubmitting = true, submitSuccess = false, submitError = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                submitError = resource.message ?: "Gagal mengirim ulasan"
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Memuat daftar product ID yang sudah diulas pada order tertentu.
     */
    fun fetchReviewedProductIds(orderId: String) {
        viewModelScope.launch {
            val productIds = reviewRepository.getReviewedProductIdsForOrder(orderId)
            _uiState.update { it.copy(reviewedProductIds = productIds) }
        }
    }

    /**
     * Mereset status submisi ulasan (biasa dipanggil saat meninggalkan screen form).
     */
    fun resetSubmitStatus() {
        _uiState.update { it.copy(isSubmitting = false, submitSuccess = false, submitError = null, selectedProduct = null) }
    }
}
