package com.kelompok4.lokalmart.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.User
import com.kelompok4.lokalmart.feature.admin.data.AdminRepository
import com.kelompok4.lokalmart.feature.admin.data.AdminStats
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val pendingStores: List<Store> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val stats: AdminStats = AdminStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0,
    val actionSuccess: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load stats
            when (val statsResult = repository.getDashboardStats()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(stats = statsResult.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = statsResult.message) }
                }
                else -> {}
            }

            // Load pending stores
            when (val storesResult = repository.getPendingStores()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(pendingStores = storesResult.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = storesResult.message) }
                }
                else -> {}
            }

            // Load all products
            when (val productsResult = repository.getAllProducts()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(allProducts = productsResult.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = productsResult.message) }
                }
                else -> {}
            }

            // Load all users
            when (val usersResult = repository.getAllUsers()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(allUsers = usersResult.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = usersResult.message) }
                }
                else -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun approveStore(storeId: String, notes: String? = null) {
        val adminId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.approveStore(storeId, adminId, notes)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            pendingStores = it.pendingStores.filter { s -> s.id != storeId },
                            stats = it.stats.copy(
                                pendingStores = (it.stats.pendingStores - 1).coerceAtLeast(0)
                            ),
                            isLoading = false,
                            actionSuccess = "Toko berhasil disetujui"
                        )
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

    fun rejectStore(storeId: String, notes: String? = null) {
        val adminId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.rejectStore(storeId, adminId, notes)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            pendingStores = it.pendingStores.filter { s -> s.id != storeId },
                            stats = it.stats.copy(
                                pendingStores = (it.stats.pendingStores - 1).coerceAtLeast(0)
                            ),
                            isLoading = false,
                            actionSuccess = "Toko berhasil ditolak"
                        )
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

    fun toggleProduct(productId: String, isActive: Boolean) {
        viewModelScope.launch {
            when (val result = repository.toggleProductActive(productId, isActive)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            allProducts = state.allProducts.map {
                                if (it.id == productId) it.copy(isActive = isActive) else it
                            },
                            actionSuccess = if (isActive) "Produk diaktifkan" else "Produk dinonaktifkan"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun toggleUser(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            when (val result = repository.toggleUserActive(userId, isActive)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            allUsers = state.allUsers.map {
                                if (it.id == userId) it.copy(isActive = isActive) else it
                            },
                            actionSuccess = if (isActive) "Pengguna diaktifkan" else "Pengguna dinonaktifkan"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearActionSuccess() {
        _uiState.update { it.copy(actionSuccess = null) }
    }
}
