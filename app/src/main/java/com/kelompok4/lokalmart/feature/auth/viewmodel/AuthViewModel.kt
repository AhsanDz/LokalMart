package com.kelompok4.lokalmart.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk LoginScreen.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * State untuk RegisterScreen.
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val agreedToTerms: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Dipakai SplashScreen untuk cek session. */
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    // ================= LOGIN =================

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _loginState.update { it.copy(email = value, error = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val s = _loginState.value
        when {
            s.email.isBlank() || s.password.isBlank() -> {
                _loginState.update { it.copy(error = "Email dan password wajib diisi") }
                return
            }
            !isValidEmail(s.email) -> {
                _loginState.update { it.copy(error = "Format email tidak valid") }
                return
            }
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signIn(s.email.trim(), s.password)
                _loginState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _loginState.update {
                    it.copy(isLoading = false, error = mapAuthError(e))
                }
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState()
    }

    // ================= REGISTER =================

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    fun onRegisterFullNameChange(v: String) =
        _registerState.update { it.copy(fullName = v, error = null) }

    fun onRegisterEmailChange(v: String) =
        _registerState.update { it.copy(email = v, error = null) }

    fun onRegisterPhoneChange(v: String) =
        _registerState.update { it.copy(phone = v, error = null) }

    fun onRegisterPasswordChange(v: String) =
        _registerState.update { it.copy(password = v, error = null) }

    fun onAgreeTermsChange(v: Boolean) =
        _registerState.update { it.copy(agreedToTerms = v, error = null) }

    fun register() {
        val s = _registerState.value
        when {
            s.fullName.isBlank() -> {
                _registerState.update { it.copy(error = "Nama lengkap wajib diisi") }
                return
            }
            s.email.isBlank() -> {
                _registerState.update { it.copy(error = "Email wajib diisi") }
                return
            }
            !isValidEmail(s.email) -> {
                _registerState.update { it.copy(error = "Format email tidak valid") }
                return
            }
            s.password.length < 8 -> {
                _registerState.update { it.copy(error = "Password minimal 8 karakter") }
                return
            }
            !s.agreedToTerms -> {
                _registerState.update {
                    it.copy(error = "Centang dulu syarat & ketentuan")
                }
                return
            }
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signUp(
                    email = s.email.trim(),
                    password = s.password,
                    fullName = s.fullName.trim(),
                    phone = s.phone.trim().takeIf { it.isNotBlank() }
                )
                _registerState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _registerState.update {
                    it.copy(isLoading = false, error = mapAuthError(e))
                }
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterUiState()
    }

    // ================= HELPERS =================

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    /**
     * Map error dari Supabase Auth ke pesan bahasa Indonesia yang user-friendly.
     */
    private fun mapAuthError(e: Exception): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            "invalid login credentials" in msg -> "Email atau password salah"
            "already registered" in msg || "already been registered" in msg ->
                "Email sudah terdaftar"
            "weak password" in msg || "password should be at least" in msg ->
                "Password terlalu lemah"
            "email not confirmed" in msg ->
                "Email belum dikonfirmasi. Cek inbox-mu (atau hubungi admin)."
            "rate limit" in msg -> "Terlalu banyak percobaan. Coba lagi nanti."
            "network" in msg || "unable to resolve" in msg ->
                "Tidak ada koneksi internet"
            else -> e.message ?: "Terjadi kesalahan, coba lagi"
        }
    }
}
