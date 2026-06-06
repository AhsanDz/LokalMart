package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.storage.StorageHelper
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditStoreState(
    val isLoading: Boolean = false,
    val store: Store? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val storageHelper: StorageHelper
) : ViewModel() {

    private val _state = MutableStateFlow(EditStoreState())
    val state: StateFlow<EditStoreState> = _state.asStateFlow()

    fun loadStore(storeId: String) {
        viewModelScope.launch {
            _state.value = EditStoreState(isLoading = true)
            try {
                // We can fetch our store
                val store = storeRepository.getMyStore()
                if (store != null && store.id == storeId) {
                    _state.value = EditStoreState(store = store)
                } else {
                    _state.value = EditStoreState(error = "Toko tidak ditemukan")
                }
            } catch (e: Exception) {
                _state.value = EditStoreState(error = e.localizedMessage ?: "Gagal memuat data toko")
            }
        }
    }

    fun updateStore(
        storeId: String,
        name: String,
        category: String,
        description: String?,
        address: String,
        contactPhone: String?,
        logoBytes: ByteArray?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // 1. Upload logo if new one provided
                val newLogoUrl = if (logoBytes != null) {
                    storageHelper.uploadStoreLogo(storeId, logoBytes)
                } else {
                    _state.value.store?.logoUrl
                }

                // 2. Save info to Supabase
                val updatedStore = storeRepository.updateStoreInfo(
                    storeId = storeId,
                    name = name,
                    category = category,
                    description = description,
                    address = address,
                    contactPhone = contactPhone,
                    logoUrl = newLogoUrl
                )

                if (updatedStore != null) {
                    _state.value = EditStoreState(
                        store = updatedStore,
                        isSuccess = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Gagal memperbarui profil toko"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Gagal memperbarui profil toko"
                )
            }
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}
