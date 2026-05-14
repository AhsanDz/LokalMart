package com.kelompok4.lokalmart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int,
    val name: String,
    @SerialName("icon_url") val iconUrl: String? = null
)

@Serializable
data class Product(
    val id: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("category_id") val categoryId: Int? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int = 0,
    val variant: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ProductImage(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
