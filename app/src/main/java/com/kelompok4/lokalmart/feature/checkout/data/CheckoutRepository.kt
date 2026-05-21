package com.kelompok4.lokalmart.feature.checkout.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.data.model.OrderItem
import com.kelompok4.lokalmart.data.model.Payment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
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

    suspend fun getOrders(buyerId: String): Resource<List<Order>> {
        return try {
            val result = supabase.postgrest[SupabaseTables.ORDERS]
                .select { filter { eq("buyer_id", buyerId) } }
                .decodeList<Order>()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil pesanan")
        }
    }

    suspend fun getOrderDetail(orderId: String): Resource<Order> {
        return try {
            val result = supabase.postgrest[SupabaseTables.ORDERS]
                .select { filter { eq("id", orderId) } }
                .decodeSingle<Order>()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil detail pesanan")
        }
    }
}