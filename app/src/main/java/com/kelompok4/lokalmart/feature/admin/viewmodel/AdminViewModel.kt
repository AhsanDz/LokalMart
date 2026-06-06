package com.kelompok4.lokalmart.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.User
import com.kelompok4.lokalmart.feature.admin.data.AdminRepository
import com.kelompok4.lokalmart.feature.admin.data.ProductWithStoreDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminState(
    val isLoading: Boolean = false,
    val pendingStores: List<Store> = emptyList(),
    val products: List<ProductWithStoreDto> = emptyList(),
    val users: List<User> = emptyList(),
    val error: String? = null,
    val actionSuccess: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    /**
     * Memuat toko-toko yang menunggu verifikasi.
     */
    fun loadPendingStores() {
        viewModelScope.launch {
            adminRepository.getPendingStores().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, pendingStores = resource.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = resource.message ?: "Gagal memuat toko pending") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Memverifikasi pendaftaran toko (approve / reject).
     */
    fun verifyStore(storeId: String, status: String, notes: String?) {
        viewModelScope.launch {
            adminRepository.verifyStore(storeId, status, notes).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null, actionSuccess = false) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, actionSuccess = true) }
                        loadPendingStores() // Reload list
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = resource.message ?: "Gagal memverifikasi toko") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Memuat daftar seluruh produk untuk dimoderasi.
     */
    fun loadProducts() {
        viewModelScope.launch {
            adminRepository.getAllProducts().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, products = resource.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = resource.message ?: "Gagal memuat produk") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Menangguhkan atau mengaktifkan kembali produk.
     */
    fun toggleProductStatus(productId: String, isActive: Boolean) {
        viewModelScope.launch {
            adminRepository.toggleProductStatus(productId, isActive).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        loadProducts() // Reload list
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = resource.message ?: "Gagal mengubah status produk") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Memuat daftar seluruh pengguna di platform.
     */
    fun loadUsers() {
        viewModelScope.launch {
            adminRepository.getAllUsers().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, users = resource.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = resource.message ?: "Gagal memuat daftar pengguna") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Memblokir atau mengaktifkan kembali akun pengguna.
     */
    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            adminRepository.toggleUserStatus(userId, isActive).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        loadUsers() // Reload list
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = resource.message ?: "Gagal mengubah status pengguna") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Mereset status flag aksi sukses.
     */
    fun resetActionStatus() {
        _state.update { it.copy(actionSuccess = false) }
    }
}
