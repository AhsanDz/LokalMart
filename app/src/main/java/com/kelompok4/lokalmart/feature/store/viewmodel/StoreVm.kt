package com.kelompok4.lokalmart.feature.store.viewmodel


import android.net.Uri
import androidx.lifecycle.ViewModel
import com.kelompok4.lokalmart.feature.store.data.StoreFormConstants
import com.kelompok4.lokalmart.feature.store.data.StoreFormModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// Sealed class untuk one-time navigation event\
sealed class NavigationEvent {
    object NavigateToNext : NavigationEvent()
    object NavigateBack   : NavigationEvent()
}

// ViewModel — mengelola state form, validasi, dan navigasi
@HiltViewModel
class StoreVm @Inject constructor() : ViewModel() {

    // State form utama
    private val _formState = MutableStateFlow(StoreFormModel())
    val formState: StateFlow<StoreFormModel> = _formState.asStateFlow()

    // State dropdown kategori
    private val _isKategoriExpanded = MutableStateFlow(false)
    val isKategoriExpanded: StateFlow<Boolean> = _isKategoriExpanded.asStateFlow()

    // State error per field
    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    // One-time navigation event
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    // Update handlers — dipanggil dari UI

    fun onLogoSelected(uri: Uri?) {
        _formState.update { it.copy(logoUri = uri) }
    }

    fun onNamaUsahaChanged(value: String) {
        _formState.update { it.copy(namaUsaha = value) }
        clearError("namaUsaha")
    }

    fun onKategoriToggle() {
        _isKategoriExpanded.update { !it }
    }

    fun onKategoriDismiss() {
        _isKategoriExpanded.value = false
    }

    fun onKategoriSelected(value: String) {
        _formState.update { it.copy(kategoriUsaha = value) }
        _isKategoriExpanded.value = false
        clearError("kategoriUsaha")
    }

    fun onDeskripsiChanged(value: String) {
        if (value.length <= StoreFormConstants.MAX_DESKRIPSI_LENGTH) {
            _formState.update { it.copy(deskripsiSingkat = value) }
        }
    }

    fun onAlamatChanged(value: String) {
        _formState.update { it.copy(alamatLengkap = value) }
        clearError("alamatLengkap")
    }

    fun onNomorWAChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '+' || it == '-' }
        _formState.update { it.copy(nomorWhatsApp = filtered) }
        clearError("nomorWhatsApp")
    }

    // Aksi tombol

    fun onLanjutClicked() {
        if (validateForm()) {
            _navigationEvent.value = NavigationEvent.NavigateToNext
        }
    }

    fun onKembaliClicked() {
        _navigationEvent.value = NavigationEvent.NavigateBack
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.value = null
    }

    // Validasi form

    private fun validateForm(): Boolean {
        val s      = _formState.value
        val errors = mutableMapOf<String, String>()

        if (s.namaUsaha.trim().length < StoreFormConstants.MIN_NAMA_LENGTH)
            errors["namaUsaha"] = "Nama usaha minimal ${StoreFormConstants.MIN_NAMA_LENGTH} karakter"

        if (s.kategoriUsaha.isBlank())
            errors["kategoriUsaha"] = "Pilih kategori usaha"

        if (s.alamatLengkap.isBlank())
            errors["alamatLengkap"] = "Alamat tidak boleh kosong"

        if (s.nomorWhatsApp.length < StoreFormConstants.MIN_NOMOR_WA_LENGTH)
            errors["nomorWhatsApp"] = "Nomor WhatsApp tidak valid"

        _fieldErrors.value = errors
        return errors.isEmpty()
    }

    private fun clearError(field: String) {
        _fieldErrors.update { it - field }
    }
}