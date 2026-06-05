package com.kelompok4.lokalmart.feature.review.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Review
import com.kelompok4.lokalmart.feature.review.data.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ReviewFormUiState(
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val hasAlreadyReviewed: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository,
    private val supabase: SupabaseClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: ""
    private val productId: String = savedStateHandle["productId"] ?: ""

    private val _uiState = MutableStateFlow(ReviewFormUiState())
    val uiState: StateFlow<ReviewFormUiState> = _uiState.asStateFlow()

    init {
        checkIfAlreadyReviewed()
    }

    private fun checkIfAlreadyReviewed() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            when (val result = repository.hasUserReviewed(userId, orderId, productId)) {
                is Resource.Success -> {
                    if (result.data) {
                        _uiState.update {
                            it.copy(hasAlreadyReviewed = true)
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun setRating(rating: Int) {
        _uiState.update { it.copy(rating = rating, error = null) }
    }

    fun setComment(comment: String) {
        if (comment.length <= 500) {
            _uiState.update { it.copy(comment = comment, error = null) }
        }
    }

    fun submitReview() {
        val state = _uiState.value
        val userId = supabase.auth.currentUserOrNull()?.id

        when {
            userId == null -> {
                _uiState.update { it.copy(error = "Kamu harus login terlebih dahulu") }
                return
            }
            state.rating == 0 -> {
                _uiState.update { it.copy(error = "Berikan rating terlebih dahulu") }
                return
            }
            state.hasAlreadyReviewed -> {
                _uiState.update { it.copy(error = "Kamu sudah memberikan ulasan untuk produk ini") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val review = Review(
                id        = UUID.randomUUID().toString(),
                productId = productId,
                userId    = userId!!,
                orderId   = orderId,
                rating    = state.rating,
                comment   = state.comment.trim().ifBlank { null }
            )

            when (val result = repository.submitReview(review)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
