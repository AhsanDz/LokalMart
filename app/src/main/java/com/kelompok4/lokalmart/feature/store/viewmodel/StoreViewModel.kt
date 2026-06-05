package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk StoreRegisterScreen.
 */
data class StoreRegisterUiState(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val contactPhone: String = "",
    val category: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * State untuk MyStoreScreen (dashboard toko seller).
 */
data class MyStoreUiState(
    val store: Store? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    // ================= STORE REGISTER =================

    private val _registerState = MutableStateFlow(StoreRegisterUiState())
    val registerState: StateFlow<StoreRegisterUiState> = _registerState.asStateFlow()

    fun onNameChange(value: String) {
        _registerState.update { it.copy(name = value, error = null) }
    }

    fun onDescriptionChange(value: String) {
        _registerState.update { it.copy(description = value, error = null) }
    }

    fun onAddressChange(value: String) {
        _registerState.update { it.copy(address = value, error = null) }
    }

    fun onContactPhoneChange(value: String) {
        _registerState.update { it.copy(contactPhone = value, error = null) }
    }

    fun onCategoryChange(value: String) {
        _registerState.update { it.copy(category = value, error = null) }
    }

    fun registerStore() {
        val s = _registerState.value
        when {
            s.name.isBlank() -> {
                _registerState.update { it.copy(error = "Nama toko wajib diisi") }
                return
            }
            s.address.isBlank() -> {
                _registerState.update { it.copy(error = "Alamat toko wajib diisi") }
                return
            }
            s.category.isBlank() -> {
                _registerState.update { it.copy(error = "Kategori wajib dipilih") }
                return
            }
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }
            val result = storeRepository.registerStore(
                name         = s.name.trim(),
                description  = s.description.trim().takeIf { it.isNotBlank() },
                address      = s.address.trim(),
                contactPhone = s.contactPhone.trim().takeIf { it.isNotBlank() },
                category     = s.category
            )
            when (result) {
                is Resource.Success -> {
                    _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _registerState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                Resource.Loading -> { /* tidak terjadi di sini */ }
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = StoreRegisterUiState()
    }

    // ================= MY STORE =================

    private val _myStoreState = MutableStateFlow(MyStoreUiState())
    val myStoreState: StateFlow<MyStoreUiState> = _myStoreState.asStateFlow()

    /**
     * Muat data toko milik user. Dipanggil saat MyStoreScreen pertama kali
     * tampil. Setelah store berhasil dimuat, otomatis muat produk-produknya.
     */
    fun loadMyStore() {
        viewModelScope.launch {
            storeRepository.getMyStore().collect { resource ->
                when (resource) {
                    Resource.Loading -> _myStoreState.update {
                        it.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _myStoreState.update {
                            it.copy(isLoading = false, store = resource.data)
                        }
                        // Jika store ada, muat produk-produknya
                        resource.data?.let { store ->
                            loadStoreProducts(store.id)
                        }
                    }
                    is Resource.Error -> _myStoreState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                }
            }
        }
    }

    /**
     * Muat daftar produk toko.
     */
    private fun loadStoreProducts(storeId: String) {
        viewModelScope.launch {
            storeRepository.getStoreProducts(storeId).collect { resource ->
                when (resource) {
                    Resource.Loading -> { /* store loading sudah dihandle */ }
                    is Resource.Success -> _myStoreState.update {
                        it.copy(products = resource.data)
                    }
                    is Resource.Error -> _myStoreState.update {
                        it.copy(error = resource.message)
                    }
                }
            }
        }
    }

    /**
     * Refresh data toko dan produk. Dipakai setelah edit produk atau
     * kembali dari halaman tambah produk.
     */
    fun refreshMyStore() {
        loadMyStore()
    }

    /**
     * Update stok produk secara cepat dari dashboard.
     */
    fun updateProductStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            val result = storeRepository.updateProductStock(productId, newStock)
            when (result) {
                is Resource.Success -> {
                    // Update stok di list lokal tanpa re-fetch
                    _myStoreState.update { state ->
                        state.copy(
                            products = state.products.map { product ->
                                if (product.id == productId) product.copy(stock = newStock)
                                else product
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    _myStoreState.update { it.copy(error = result.message) }
                }
                Resource.Loading -> { /* tidak terjadi */ }
            }
        }
    }

    // ================= KATEGORI TOKO =================

    val storeCategories = listOf(
        "Kerajinan",
        "Fashion",
        "Alas Kaki",
        "Kuliner",
        "Elektronik",
        "Kecantikan",
        "Furnitur",
        "Pertanian",
        "Jasa",
        "Lainnya"
    )
}
