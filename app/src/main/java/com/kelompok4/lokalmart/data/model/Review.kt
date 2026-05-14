package com.kelompok4.lokalmart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("order_id") val orderId: String,
    val rating: Int,            // 1..5
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AnalyticsSummary(
    val id: String,
    @SerialName("store_id") val storeId: String,
    val period: String,          // tanggal ISO
    @SerialName("total_orders") val totalOrders: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("total_items_sold") val totalItemsSold: Int = 0
)
