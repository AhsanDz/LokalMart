package com.kelompok4.lokalmart.core.util

/**
 * Wrapper umum untuk hasil operasi async (network/DB).
 * Pakai ini di Repository dan ViewModel agar state Loading/Success/Error konsisten.
 *
 * Contoh:
 *   val state: StateFlow<Resource<User>> = ...
 *   when (state.value) {
 *       is Resource.Loading -> { ... }
 *       is Resource.Success -> { ... }
 *       is Resource.Error   -> { ... }
 *   }
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>
}
