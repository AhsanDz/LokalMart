package com.kelompok4.lokalmart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cart(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int = 1,
    @SerialName("created_at") val createdAt: String? = null
)
