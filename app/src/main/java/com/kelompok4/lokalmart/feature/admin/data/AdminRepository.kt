package com.kelompok4.lokalmart.feature.admin.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.User
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    /**
     * Mengambil daftar toko yang statusnya 'pending' (menunggu verifikasi).
     */
    fun getPendingStores(): Flow<Resource<List<Store>>> = flow {
        emit(Resource.Loading)
        try {
            val result = supabase.postgrest[SupabaseTables.STORES]
                .select {
                    filter { eq("status", "pending") }
                }
                .decodeList<Store>()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat antrean verifikasi toko"))
        }
    }

    /**
     * Memverifikasi toko (approve / reject).
     */
    fun verifyStore(
        storeId: String,
        status: String, // 'approved' atau 'rejected'
        notes: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val adminId = authRepository.currentUserId()
                ?: throw IllegalStateException("Admin tidak terautentikasi")

            // 1. Update status toko di tabel stores
            val storeDbStatus = if (status == "approved") "active" else "rejected"
            supabase.postgrest[SupabaseTables.STORES].update(
                {
                    set("status", storeDbStatus)
                }
            ) {
                filter { eq("id", storeId) }
            }

            // 2. Catat keputusan verifikasi di tabel store_verifications
            val verification = StoreVerification(
                id = UUID.randomUUID().toString(),
                storeId = storeId,
                adminId = adminId,
                status = status,
                notes = notes
            )
            supabase.postgrest[SupabaseTables.STORE_VERIFICATIONS].insert(verification)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal menyimpan keputusan verifikasi"))
        }
    }

    /**
     * Mengambil seluruh produk yang ada di platform beserta nama tokonya untuk keperluan moderasi.
     */
    fun getAllProducts(): Flow<Resource<List<ProductWithStoreDto>>> = flow {
        emit(Resource.Loading)
        try {
            val result = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        stores ( name )
                        """.trimIndent()
                    )
                ) {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<ProductWithStoreDto>()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat daftar produk"))
        }
    }

    /**
     * Menangguhkan (suspend) atau mengaktifkan kembali produk.
     */
    fun toggleProductStatus(productId: String, isActive: Boolean): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            supabase.postgrest[SupabaseTables.PRODUCTS].update(
                {
                    set("is_active", isActive)
                }
            ) {
                filter { eq("id", productId) }
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengubah status keaktifan produk"))
        }
    }

    /**
     * Mengambil seluruh pengguna di platform.
     */
    fun getAllUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            val result = supabase.postgrest[SupabaseTables.PROFILES]
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<User>()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat daftar pengguna"))
        }
    }

    /**
     * Memblokir atau mengaktifkan kembali akun pengguna.
     */
    fun toggleUserStatus(userId: String, isActive: Boolean): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            supabase.postgrest[SupabaseTables.PROFILES].update(
                {
                    set("is_active", isActive)
                }
            ) {
                filter { eq("id", userId) }
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal merubah status blokir pengguna"))
        }
    }
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

@Serializable
data class ProductWithStoreDto(
    val id: String,
    @SerialName("store_id") val storeId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int,
    @SerialName("is_active") val isActive: Boolean,
    val stores: AdminStoreDto? = null
)

@Serializable
data class AdminStoreDto(
    val name: String
)
