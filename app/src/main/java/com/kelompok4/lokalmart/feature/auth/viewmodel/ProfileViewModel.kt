package com.kelompok4.lokalmart.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.storage.StorageHelper
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.User
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import com.kelompok4.lokalmart.feature.checkout.data.CheckoutRepository
import com.kelompok4.lokalmart.feature.review.data.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val storeName: String? = null,
    val totalOrders: Int = 0,
    val totalReviews: Int = 0,
    val totalWishlist: Int = 0,
    val newOrdersCount: Int = 0,
    val todayRevenue: Double = 0.0
)

data class EditProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val newAvatarBytes: ByteArray? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageHelper: StorageHelper,
    private val storeRepository: StoreRepository,
    private val checkoutRepository: CheckoutRepository,
    private val reviewRepository: ReviewRepository,
    private val wishlistRepository: com.kelompok4.lokalmart.feature.catalog.data.WishlistRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _editProfileState = MutableStateFlow(EditProfileUiState())
    val editProfileState: StateFlow<EditProfileUiState> = _editProfileState.asStateFlow()

    init {
        loadProfile()
        observeWishlist()
    }

    private fun observeWishlist() {
        viewModelScope.launch {
            wishlistRepository.wishlistProductIds.collect { ids ->
                _profileState.update { it.copy(totalWishlist = ids.size) }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val userId = user.id
                    var storeName: String? = null
                    var newOrdersCount = 0
                    var todayRevenue = 0.0

                    // 1. Fetch store info if user is a SELLER
                    if (user.role == com.kelompok4.lokalmart.data.model.UserRole.SELLER) {
                        try {
                            val store = storeRepository.getMyStore()
                            if (store != null) {
                                storeName = store.name
                                val orders = storeRepository.getStoreOrders(store.id)
                                val todayString = java.time.LocalDate.now().toString()
                                todayRevenue = orders
                                    .filter { it.createdAt?.startsWith(todayString) == true && it.status != "pending" && it.status != "cancelled" }
                                    .sumOf { it.totalPrice }
                                newOrdersCount = orders.count { it.status == "confirmed" }
                            }
                        } catch (e: Exception) {
                            // Ignore store fetch errors to not block profile loading
                        }
                    }

                    // 2. Fetch order stats for the buyer
                    var totalOrdersCount = 0
                    try {
                        val ordersRes = checkoutRepository.getOrders(userId)
                        if (ordersRes is com.kelompok4.lokalmart.core.util.Resource.Success) {
                            totalOrdersCount = ordersRes.data.size
                        }
                    } catch (e: Exception) {
                        // Ignore order fetch errors
                    }

                    // 3. Fetch review stats for the buyer
                    var totalReviewsCount = 0
                    try {
                        val reviewsList = reviewRepository.getUserReviews(userId)
                        totalReviewsCount = reviewsList.size
                    } catch (e: Exception) {
                        // Ignore review fetch errors
                    }

                    _profileState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            storeName = storeName,
                            totalOrders = totalOrdersCount,
                            totalReviews = totalReviewsCount,
                            totalWishlist = 0,
                            newOrdersCount = newOrdersCount,
                            todayRevenue = todayRevenue
                        )
                    }

                    // Inisialisasi input form edit profil
                    _editProfileState.update {
                        it.copy(
                            fullName = user.fullName,
                            phone = user.phone.orEmpty(),
                            bio = user.bio.orEmpty(),
                            avatarUrl = user.avatarUrl,
                            newAvatarBytes = null,
                            isSuccess = false,
                            error = null
                        )
                    }
                } else {
                    _profileState.update { it.copy(isLoading = false, error = "Pengguna tidak ditemukan") }
                }
            } catch (e: Exception) {
                _profileState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Gagal memuat profil"
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _editProfileState.update { it.copy(fullName = value, error = null) }
    }

    fun onPhoneChange(value: String) {
        _editProfileState.update { it.copy(phone = value, error = null) }
    }

    fun onBioChange(value: String) {
        _editProfileState.update { it.copy(bio = value, error = null) }
    }

    fun onAvatarChanged(bytes: ByteArray?) {
        _editProfileState.update { it.copy(newAvatarBytes = bytes, error = null) }
    }

    fun updateProfile() {
        val s = _editProfileState.value
        if (s.fullName.isBlank()) {
            _editProfileState.update { it.copy(error = "Nama lengkap wajib diisi") }
            return
        }

        viewModelScope.launch {
            _editProfileState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = authRepository.currentUserId() ?: throw Exception("Sesi berakhir, silakan login kembali")

                var avatarUrl = s.avatarUrl
                if (s.newAvatarBytes != null) {
                    avatarUrl = storageHelper.uploadAvatar(userId, s.newAvatarBytes)
                }

                val updatedUser = authRepository.updateProfile(
                    fullName = s.fullName.trim(),
                    phone = s.phone.trim().takeIf { it.isNotBlank() },
                    bio = s.bio.trim().takeIf { it.isNotBlank() },
                    avatarUrl = avatarUrl
                )

                if (updatedUser != null) {
                    _profileState.update { it.copy(user = updatedUser) }
                    _editProfileState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _editProfileState.update { it.copy(isLoading = false, error = "Gagal memperbarui data profil") }
                }
            } catch (e: Exception) {
                _editProfileState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Terjadi kesalahan saat memperbarui profil"
                    )
                }
            }
        }
    }

    fun resetEditSuccess() {
        _editProfileState.update { it.copy(isSuccess = false) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                onSuccess()
            } catch (e: Exception) {
                _profileState.update { it.copy(error = "Gagal keluar sesi: ${e.localizedMessage}") }
            }
        }
    }
}
