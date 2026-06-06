package com.kelompok4.lokalmart.feature.review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.AnalyticsSummary
import com.kelompok4.lokalmart.feature.review.data.AnalyticsRepository
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val analyticsData: List<AnalyticsSummary> = emptyList(),
    val error: String? = null,
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val totalItemsSold: Int = 0,
    val currentPeriodDays: Int = 7
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    /**
     * Memuat analitik toko berdasarkan jumlah hari terakhir.
     */
    fun loadAnalytics(days: Int = 7) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, currentPeriodDays = days) }
            try {
                val store = storeRepository.getMyStore()
                if (store != null) {
                    analyticsRepository.getStoreAnalytics(store.id, days).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                _state.update { it.copy(isLoading = true) }
                            }
                            is Resource.Success -> {
                                val data = resource.data ?: emptyList()
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        analyticsData = data,
                                        totalRevenue = data.sumOf { item -> item.totalRevenue },
                                        totalOrders = data.sumOf { item -> item.totalOrders },
                                        totalItemsSold = data.sumOf { item -> item.totalItemsSold }
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        error = resource.message ?: "Gagal memuat analitik penjualan"
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Toko Anda belum terdaftar"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Gagal mengakses data toko"
                    )
                }
            }
        }
    }
}
