package com.kelompok4.lokalmart.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.User
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk ProfileScreen — menampilkan data profil user.
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * State untuk EditProfileScreen — form edit profil.
 */
data class EditProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val bio: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ================= PROFILE =================

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    // ================= EDIT PROFILE =================

    private val _editProfileState = MutableStateFlow(EditProfileUiState())
    val editProfileState: StateFlow<EditProfileUiState> = _editProfileState.asStateFlow()

    init {
        loadProfile()
    }

    // ================= LOAD PROFILE =================

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = authRepository.getCurrentUser()
                _profileState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        error = if (user == null) "Gagal memuat profil" else null
                    )
                }
                // Pre-fill edit form dari data profil terbaru
                if (user != null) {
                    _editProfileState.update {
                        it.copy(
                            fullName = user.fullName,
                            phone = user.phone.orEmpty(),
                            bio = user.bio.orEmpty()
                        )
                    }
                }
            } catch (e: Exception) {
                _profileState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Terjadi kesalahan"
                    )
                }
            }
        }
    }

    // ================= EDIT FIELD CHANGES =================

    fun onFullNameChange(value: String) {
        _editProfileState.update { it.copy(fullName = value, error = null) }
    }

    fun onPhoneChange(value: String) {
        _editProfileState.update { it.copy(phone = value, error = null) }
    }

    fun onBioChange(value: String) {
        _editProfileState.update { it.copy(bio = value, error = null) }
    }

    // ================= SAVE PROFILE =================

    fun saveProfile() {
        val s = _editProfileState.value
        if (s.fullName.isBlank()) {
            _editProfileState.update { it.copy(error = "Nama lengkap wajib diisi") }
            return
        }

        viewModelScope.launch {
            _editProfileState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.updateProfile(
                fullName = s.fullName.trim(),
                phone = s.phone.trim().takeIf { it.isNotBlank() },
                bio = s.bio.trim().takeIf { it.isNotBlank() }
            )
            when (result) {
                is Resource.Success<*> -> {
                    _editProfileState.update {
                        it.copy(isLoading = false, isSuccess = true)
                    }
                    // Refresh profil setelah simpan
                    loadProfile()
                }
                is Resource.Error -> {
                    _editProfileState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun resetEditState() {
        val user = _profileState.value.user
        _editProfileState.value = EditProfileUiState(
            fullName = user?.fullName.orEmpty(),
            phone = user?.phone.orEmpty(),
            bio = user?.bio.orEmpty()
        )
    }

    // ================= SIGN OUT =================

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (_: Exception) {
                // Tetap logout meskipun gagal clear session di server
            }
        }
    }
}
