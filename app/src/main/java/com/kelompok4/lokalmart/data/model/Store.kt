package com.kelompok4.lokalmart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val description: String? = null,
    val address: String,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val category: String,
    val status: String = StoreStatus.PENDING,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

object StoreStatus {
    const val PENDING = "pending"
    const val ACTIVE = "active"
    const val SUSPENDED = "suspended"
    const val REJECTED = "rejected"
}

@Serializable
data class StoreVerification(
    val id: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("admin_id") val adminId: String,
    val status: String,
    val notes: String? = null,
    @SerialName("verified_at") val verifiedAt: String? = null
)
