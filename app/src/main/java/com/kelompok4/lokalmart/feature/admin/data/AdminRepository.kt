package com.kelompok4.lokalmart.feature.admin.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.StoreStatus
import com.kelompok4.lokalmart.data.model.StoreVerification
import com.kelompok4.lokalmart.data.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ── Dashboard stats ──────────────────────────────────────────────────────────
data class AdminStats(
    val pendingStores: Int = 0,
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val totalOrders: Int = 0
)

// ── DTO untuk count query ────────────────────────────────────────────────────
@Serializable
private data class CountDto(val count: Int)

// ── Repository ───────────────────────────────────────────────────────────────
@Singleton
class AdminRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    /**
     * Ambil semua toko dengan status 'pending' (menunggu verifikasi).
     */
    suspend fun getPendingStores(): Resource<List<Store>> {
        return try {
            val stores = supabase.postgrest[SupabaseTables.STORES]
                .select {
                    filter { eq("status", StoreStatus.PENDING) }
                }
                .decodeList<Store>()

            Resource.Success(stores)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengambil daftar toko pending")
        }
    }

    /**
     * Approve toko: update status ke 'active' + insert record verifikasi.
     */
    suspend fun approveStore(
        storeId: String,
        adminId: String,
        notes: String? = null
    ): Resource<Unit> {
        return try {
            // 1. Update status toko
            supabase.postgrest[SupabaseTables.STORES]
                .update(mapOf("status" to StoreStatus.ACTIVE)) {
                    filter { eq("id", storeId) }
                }

            // 2. Insert record verifikasi
            val verification = StoreVerification(
                id         = UUID.randomUUID().toString(),
                storeId    = storeId,
                adminId    = adminId,
                status     = StoreStatus.ACTIVE,
                notes      = notes
            )
            supabase.postgrest[SupabaseTables.STORE_VERIFICATIONS]
                .insert(verification)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menyetujui toko")
        }
    }

    /**
     * Reject toko: update status ke 'rejected' + insert record verifikasi.
     */
    suspend fun rejectStore(
        storeId: String,
        adminId: String,
        notes: String? = null
    ): Resource<Unit> {
        return try {
            // 1. Update status toko
            supabase.postgrest[SupabaseTables.STORES]
                .update(mapOf("status" to StoreStatus.REJECTED)) {
                    filter { eq("id", storeId) }
                }

            // 2. Insert record verifikasi
            val verification = StoreVerification(
                id         = UUID.randomUUID().toString(),
                storeId    = storeId,
                adminId    = adminId,
                status     = StoreStatus.REJECTED,
                notes      = notes
            )
            supabase.postgrest[SupabaseTables.STORE_VERIFICATIONS]
                .insert(verification)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menolak toko")
        }
    }

    /**
     * Ambil semua produk untuk moderasi admin.
     */
    suspend fun getAllProducts(): Resource<List<Product>> {
        return try {
            val products = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select()
                .decodeList<Product>()

            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengambil daftar produk")
        }
    }

    /**
     * Ambil semua user/profil.
     */
    suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val users = supabase.postgrest[SupabaseTables.PROFILES]
                .select()
                .decodeList<User>()

            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengambil daftar pengguna")
        }
    }

    /**
     * Toggle status aktif/nonaktif produk.
     */
    suspend fun toggleProductActive(productId: String, isActive: Boolean): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.PRODUCTS]
                .update(mapOf("is_active" to isActive)) {
                    filter { eq("id", productId) }
                }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengubah status produk")
        }
    }

    /**
     * Toggle status aktif/nonaktif user.
     */
    suspend fun toggleUserActive(userId: String, isActive: Boolean): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.PROFILES]
                .update(mapOf("is_active" to isActive)) {
                    filter { eq("id", userId) }
                }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengubah status pengguna")
        }
    }

    /**
     * Ambil statistik dashboard admin:
     * jumlah toko pending, total user, total produk, total pesanan.
     */
    suspend fun getDashboardStats(): Resource<AdminStats> {
        return try {
            val pendingStores = supabase.postgrest[SupabaseTables.STORES]
                .select {
                    filter { eq("status", StoreStatus.PENDING) }
                }
                .decodeList<Store>()
                .size

            val totalUsers = supabase.postgrest[SupabaseTables.PROFILES]
                .select()
                .decodeList<User>()
                .size

            val totalProducts = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select()
                .decodeList<Product>()
                .size

            val totalOrders = supabase.postgrest[SupabaseTables.ORDERS]
                .select()
                .decodeList<OrderCountDto>()
                .size

            Resource.Success(
                AdminStats(
                    pendingStores = pendingStores,
                    totalUsers    = totalUsers,
                    totalProducts = totalProducts,
                    totalOrders   = totalOrders
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal memuat statistik")
        }
    }
}

// ── Minimal DTO untuk menghitung order ────────────────────────────────────────
@Serializable
private data class OrderCountDto(val id: String)
