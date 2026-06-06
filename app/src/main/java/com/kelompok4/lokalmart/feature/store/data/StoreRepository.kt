package com.kelompok4.lokalmart.feature.store.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.storage.StorageHelper
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Singleton
class StoreRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val storageHelper: StorageHelper
) {
    /**
     * Mendaftar toko baru:
     * 1. Menghasilkan UUID toko baru.
     * 2. Jika ada logoBytes, mengunggah logo ke Supabase Storage menggunakan StorageHelper.
     * 3. Memasukkan data toko ke tabel `stores` dengan status 'pending'.
     * 4. Memperbarui peran akun (`role`) menjadi 'seller' di tabel `profiles`.
     */
    fun registerStore(
        name: String,
        category: String,
        description: String?,
        address: String,
        contactPhone: String?,
        logoBytes: ByteArray?
    ): Flow<Resource<Store>> = flow {
        emit(Resource.Loading)
        try {
            val ownerId = authRepository.currentUserId()
                ?: throw IllegalStateException("Pengguna tidak terautentikasi")

            val storeId = UUID.randomUUID().toString()

            // Upload logo jika ada
            val logoUrl = if (logoBytes != null) {
                storageHelper.uploadStoreLogo(storeId, logoBytes)
            } else {
                null
            }

            val newStore = Store(
                id = storeId,
                ownerId = ownerId,
                name = name,
                description = description,
                address = address,
                contactPhone = contactPhone,
                category = category,
                status = "active", // Automatically activate in dev/testing
                logoUrl = logoUrl
            )

            // Insert ke tabel stores
            supabase.postgrest
                .from(SupabaseTables.STORES)
                .insert(newStore)

            // Update user role ke seller agar user bisa langsung masuk ke dashboard toko miliknya
            authRepository.updateRole("seller")

            emit(Resource.Success(newStore))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mendaftarkan toko baru"))
        }
    }

    /**
     * Mengambil data toko yang dimiliki oleh user yang sedang aktif.
     */
    suspend fun getMyStore(): Store? {
        val ownerId = authRepository.currentUserId() ?: return null
        val store = supabase.postgrest
            .from(SupabaseTables.STORES)
            .select {
                filter { eq("owner_id", ownerId) }
            }
            .decodeSingleOrNull<Store>()

        if (store != null && store.status == "pending") {
            try {
                val activeStore = supabase.postgrest
                    .from(SupabaseTables.STORES)
                    .update(
                        {
                            set("status", "active")
                        }
                    ) {
                        filter { eq("id", store.id) }
                        select()
                    }
                    .decodeSingleOrNull<Store>()
                return activeStore ?: store
            } catch (e: Exception) {
                // Ignore and return original store
            }
        }
        return store
    }

    /**
     * Mengupdate informasi toko.
     */
    suspend fun updateStoreInfo(
        storeId: String,
        name: String,
        category: String,
        description: String?,
        address: String,
        contactPhone: String?,
        logoUrl: String?
    ): Store? {
        return supabase.postgrest
            .from(SupabaseTables.STORES)
            .update(
                {
                    set("name", name)
                    set("category", category)
                    set("description", description)
                    set("address", address)
                    set("contact_phone", contactPhone)
                    if (logoUrl != null) {
                        set("logo_url", logoUrl)
                    }
                }
            ) {
                filter { eq("id", storeId) }
                select()
            }
            .decodeSingleOrNull<Store>()
    }

    suspend fun getActiveProductsCount(storeId: String): Int {
        return try {
            val result = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select {
                    filter {
                        eq("store_id", storeId)
                        eq("is_active", true)
                    }
                }
                .decodeList<com.kelompok4.lokalmart.feature.store.viewmodel.ProductDto>()
            result.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getStoreOrders(storeId: String): List<StoreOrderDto> {
        return try {
            supabase.postgrest[SupabaseTables.ORDERS]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        *,
                        profiles ( full_name ),
                        order_items (
                            quantity,
                            products ( name )
                        )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("store_id", storeId) }
                }
                .decodeList<StoreOrderDto>()
        } catch (e: Exception) {
            emptyList()
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

    suspend fun getActiveStores(): List<Store> {
        return try {
            supabase.postgrest[SupabaseTables.STORES]
                .select {
                    filter { eq("status", "active") }
                }
                .decodeList<Store>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Serializable
data class StoreOrderDto(
    val id: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("total_price") val totalPrice: Double,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    val profiles: StoreOrderBuyerDto? = null,
    @SerialName("order_items") val orderItems: List<StoreOrderItemDto> = emptyList()
)

@Serializable
data class StoreOrderBuyerDto(
    @SerialName("full_name") val fullName: String
)

@Serializable
data class StoreOrderItemDto(
    val quantity: Int,
    val products: StoreOrderProductDto? = null
)

@Serializable
data class StoreOrderProductDto(
    val name: String
)
