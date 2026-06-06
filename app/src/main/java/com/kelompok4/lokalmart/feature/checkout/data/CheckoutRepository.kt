package com.kelompok4.lokalmart.feature.checkout.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.data.model.OrderItem
import com.kelompok4.lokalmart.data.model.Payment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class CheckoutRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun placeOrder(order: Order): Resource<Order> {
        return try {
            val result = supabase.postgrest[SupabaseTables.ORDERS]
                .insert(order) { select() }
                .decodeSingle<Order>()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal membuat pesanan")
        }
    }

    suspend fun insertOrderItems(items: List<OrderItem>): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.ORDER_ITEMS].insert(items)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal menyimpan item pesanan")
        }
    }

    suspend fun insertPayment(payment: Payment): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.PAYMENTS].insert(payment)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal menyimpan pembayaran")
        }
    }

    suspend fun getOrders(buyerId: String): Resource<List<OrderWithDetailsDto>> {
        return try {
            val result = supabase.postgrest[SupabaseTables.ORDERS]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        *,
                        stores ( name, address ),
                        order_items (
                            id,
                            order_id,
                            product_id,
                            quantity,
                            price_at_order,
                            products (
                                name,
                                product_images ( image_url, is_primary )
                            )
                        )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("buyer_id", buyerId) }
                }
                .decodeList<OrderWithDetailsDto>()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil pesanan")
        }
    }

    suspend fun getOrderDetail(orderId: String): Resource<OrderDetailWithStoreAndPaymentDto> {
        return try {
            val result = supabase.postgrest[SupabaseTables.ORDERS]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        *,
                        stores ( name, address, status ),
                        payments ( method, amount, status )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("id", orderId) }
                }
                .decodeSingle<OrderDetailWithStoreAndPaymentDto>()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil detail pesanan")
        }
    }

    suspend fun getOrderItemsDetail(orderId: String): Resource<List<OrderItemDetailDto>> {
        return try {
            val result = supabase.postgrest[SupabaseTables.ORDER_ITEMS]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        *,
                        products (
                            name,
                            product_images ( image_url, is_primary )
                        )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("order_id", orderId) }
                }
                .decodeList<OrderItemDetailDto>()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil item pesanan")
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.ORDERS]
                .update(mapOf("status" to newStatus)) {
                    filter { eq("id", orderId) }
                }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal memperbarui status pesanan")
        }
    }

    suspend fun updatePaymentStatus(orderId: String, newStatus: String): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.PAYMENTS]
                .update(mapOf("status" to newStatus)) {
                    filter { eq("order_id", orderId) }
                }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal memperbarui status pembayaran")
        }
    }

    suspend fun decrementProductStock(productId: String, quantity: Int): Resource<Unit> {
        return try {
            val product = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select {
                    filter { eq("id", productId) }
                }
                .decodeSingleOrNull<com.kelompok4.lokalmart.data.model.Product>()
                ?: throw Exception("Produk tidak ditemukan")

            val newStock = (product.stock - quantity).coerceAtLeast(0)

            supabase.postgrest[SupabaseTables.PRODUCTS]
                .update(mapOf("stock" to newStock)) {
                    filter { eq("id", productId) }
                }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal memperbarui stok produk")
        }
    }
}

@Serializable
data class OrderItemDetailDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    @SerialName("price_at_order") val priceAtOrder: Double,
    val products: OrderItemProductDto? = null
)

@Serializable
data class OrderItemProductDto(
    val name: String,
    @SerialName("product_images") val productImages: List<OrderItemImageDto>? = null
)

@Serializable
data class OrderItemImageDto(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

@Serializable
data class OrderWithDetailsDto(
    val id: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("shipping_address") val shippingAddress: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    val stores: OrderStoreDto? = null,
    @SerialName("order_items") val orderItems: List<OrderItemWithProductDto> = emptyList()
)

@Serializable
data class OrderStoreDto(
    val name: String,
    val address: String? = null
)

@Serializable
data class OrderItemWithProductDto(
    val quantity: Int,
    @SerialName("price_at_order") val priceAtOrder: Double,
    val products: OrderItemProductDto? = null
)

@Serializable
data class OrderDetailWithStoreAndPaymentDto(
    val id: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("shipping_address") val shippingAddress: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    val stores: OrderStoreDto? = null,
    val payments: OrderPaymentDto? = null
)

@Serializable
data class OrderPaymentDto(
    val method: String,
    val amount: Double,
    val status: String
)