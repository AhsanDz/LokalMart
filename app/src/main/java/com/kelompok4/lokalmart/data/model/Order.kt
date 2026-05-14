package com.kelompok4.lokalmart.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("shipping_address") val shippingAddress: String,
    val status: String = OrderStatus.PENDING,
    @SerialName("created_at") val createdAt: String? = null
)

object OrderStatus {
    const val PENDING = "pending"
    const val CONFIRMED = "confirmed"
    const val SHIPPED = "shipped"
    const val DELIVERED = "delivered"
    const val CANCELLED = "cancelled"
}

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    @SerialName("price_at_order") val priceAtOrder: Double
)

@Serializable
data class Payment(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val method: String,
    val amount: Double,
    val status: String = PaymentStatus.PENDING,
    @SerialName("paid_at") val paidAt: String? = null
)

object PaymentMethod {
    const val BANK_TRANSFER = "bank_transfer"
    const val EWALLET = "ewallet"
    const val QRIS = "qris"
}

object PaymentStatus {
    const val PENDING = "pending"
    const val PAID = "paid"
    const val FAILED = "failed"
    const val REFUNDED = "refunded"
}
