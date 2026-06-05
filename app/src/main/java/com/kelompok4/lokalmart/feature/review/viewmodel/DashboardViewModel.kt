package com.kelompok4.lokalmart.feature.review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.feature.review.data.DashboardData
import com.kelompok4.lokalmart.feature.review.data.ReviewItem
import com.kelompok4.lokalmart.feature.review.data.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalItemsSold: Int = 0,
    val averageRating: Float = 0f,
    val recentReviews: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Ambil toko milik user yang login
            when (val storeResult = repository.getMyStore()) {
                is Resource.Success -> {
                    val store = storeResult.data
                    if (store == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Kamu belum memiliki toko"
                            )
                        }
                        return@launch
                    }

                    // 2. Ambil data dashboard
                    when (val dashResult = repository.getStoreDashboard(store.id)) {
                        is Resource.Success -> {
                            val data = dashResult.data
                            _uiState.update {
                                it.copy(
                                    isLoading      = false,
                                    totalOrders    = data.totalOrders,
                                    totalRevenue   = data.totalRevenue,
                                    totalItemsSold = data.totalItemsSold,
                                    averageRating  = data.averageRating,
                                    recentReviews  = data.recentReviews
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(isLoading = false, error = dashResult.message)
                            }
                        }
                        else -> {}
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = storeResult.message)
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
