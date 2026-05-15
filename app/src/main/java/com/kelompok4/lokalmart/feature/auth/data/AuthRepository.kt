package com.kelompok4.lokalmart.feature.auth.data

import android.util.Log
import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.data.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk semua operasi terkait Supabase Auth dan profil pengguna.
 *
 * Pakai @Singleton supaya satu instance di-share oleh semua ViewModel yang
 * butuh akses auth (Login, Register, Splash, Profile, dst).
 */
@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    /**
     * Register akun baru. Trigger `on_auth_user_created` di Supabase akan
     * otomatis bikin row di public.profiles dengan id + full_name + email.
     *
     * Phone (opsional) di-update terpisah setelah signup karena trigger
     * tidak meng-handle kolom phone.
     *
     * Penting: di Supabase Dashboard, matikan "Confirm email" untuk dev
     * (Authentication > Providers > Email > Confirm email = OFF), supaya
     * user langsung login otomatis tanpa harus verifikasi email dulu.
     */
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String? = null
    ) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
        // Update phone jika diisi. Wrap try-catch supaya signup tidak gagal
        // gara-gara phone update gagal (mis. RLS atau race condition).
        if (!phone.isNullOrBlank()) {
            try {
                updatePhone(phone)
            } catch (e: Exception) {
                Log.w(TAG, "Gagal update phone setelah signup", e)
            }
        }
    }

    private suspend fun updatePhone(phone: String) {
        val userId = currentUserId() ?: return
        supabase.postgrest
            .from(SupabaseTables.PROFILES)
            .update(
                {
                    set("phone", phone)
                }
            ) {
                filter { eq("id", userId) }
            }
    }

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /**
     * Cek apakah ada session aktif. Dipakai Splash untuk decide
     * navigasi ke Login atau Home.
     */
    fun isLoggedIn(): Boolean = supabase.auth.currentSessionOrNull() != null

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    /**
     * Ambil data profil user yang sedang login dari tabel `profiles`.
     */
    suspend fun getCurrentUser(): User? {
        val userId = currentUserId() ?: return null
        return supabase.postgrest
            .from(SupabaseTables.PROFILES)
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<User>()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
