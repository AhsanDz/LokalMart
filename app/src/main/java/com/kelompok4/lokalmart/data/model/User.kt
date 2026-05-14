package com.kelompok4.lokalmart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirror tabel `profiles` di Supabase.
 * Pakai @SerialName karena kita pakai snake_case di DB tapi camelCase di Kotlin.
 */
@Serializable
data class User(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = UserRole.BUYER,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

object UserRole {
    const val BUYER = "buyer"
    const val SELLER = "seller"
    const val ADMIN = "admin"
}
