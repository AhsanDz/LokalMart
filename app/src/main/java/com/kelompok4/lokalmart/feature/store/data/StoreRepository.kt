package com.kelompok4.lokalmart.feature.store.data

import android.util.Log
import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.data.model.StoreStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DTO untuk insert store baru ke Supabase.
 * Tidak pakai `id` karena Supabase generate UUID otomatis via default.
 * Status default 'pending' — menunggu verifikasi admin.
 */
@Serializable
data class StoreInsertDto(
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val description: String? = null,
    val address: String,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val category: String,
    val status: String = StoreStatus.PENDING
)

/**
 * DTO untuk produk yang di-JOIN dengan product_images.
 * Dipakai supaya bisa ambil gambar utama produk sekaligus.
 */
@Serializable
data class StoreProductDto(
    val id: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("category_id") val categoryId: Int? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int = 0,
    val variant: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("product_images") val productImages: List<StoreProductImageDto>? = null,
    @SerialName("sold_count") val soldCount: Int = 0
)

@Serializable
data class StoreProductImageDto(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

/**
 * Extension: DTO → domain model Product.
 */
fun StoreProductDto.toDomain() = Product(
    id          = id,
    storeId     = storeId,
    categoryId  = categoryId,
    name        = name,
    description = description,
    price       = price,
    stock       = stock,
    variant     = variant,
    isActive    = isActive,
    createdAt   = createdAt,
    imageUrl    = productImages
        ?.firstOrNull { it.isPrimary }?.imageUrl
        ?: productImages?.firstOrNull()?.imageUrl,
    soldCount   = soldCount,
)

/**
 * Repository untuk operasi terkait toko (store).
 *
 * Pakai @Singleton supaya satu instance di-share oleh semua ViewModel
 * yang butuh akses data toko (StoreRegister, MyStore, dsb).
 */
@Singleton
class StoreRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    // ── Helper ────────────────────────────────────────────────────────────────
    private fun currentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id

    // ── Register Store ────────────────────────────────────────────────────────

    /**
     * Daftarkan toko baru. Store masuk dengan status='pending' (butuh
     * verifikasi admin sebelum aktif).
     */
    suspend fun registerStore(
        name: String,
        description: String?,
        address: String,
        contactPhone: String?,
        category: String
    ): Resource<Store> {
        return try {
            val userId = currentUserId()
                ?: return Resource.Error("Sesi login tidak ditemukan. Silakan login ulang.")

            val dto = StoreInsertDto(
                ownerId      = userId,
                name         = name,
                description  = description,
                address      = address,
                contactPhone = contactPhone,
                category     = category,
            )

            val store = supabase.postgrest
                .from(SupabaseTables.STORES)
                .insert(dto) {
                    select()
                }
                .decodeSingle<Store>()

            // Update role user menjadi 'seller' di tabel profiles
            try {
                supabase.postgrest
                    .from(SupabaseTables.PROFILES)
                    .update({ set("role", "seller") }) {
                        filter { eq("id", userId) }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal update role ke seller", e)
            }

            Resource.Success(store)
        } catch (e: Exception) {
            Log.e(TAG, "registerStore gagal", e)
            Resource.Error(mapStoreError(e))
        }
    }

    // ── Get My Store ──────────────────────────────────────────────────────────

    /**
     * Ambil toko milik user yang sedang login.
     * Return null jika belum punya toko.
     */
    fun getMyStore(): Flow<Resource<Store?>> = flow {
        emit(Resource.Loading)
        try {
            val userId = currentUserId()
                ?: throw IllegalStateException("User belum login")

            val store = supabase.postgrest
                .from(SupabaseTables.STORES)
                .select {
                    filter { eq("owner_id", userId) }
                }
                .decodeSingleOrNull<Store>()

            emit(Resource.Success(store))
        } catch (e: Exception) {
            Log.e(TAG, "getMyStore gagal", e)
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data toko"))
        }
    }

    // ── Get Store by ID ───────────────────────────────────────────────────────

    /**
     * Ambil detail toko berdasarkan ID. Dipakai untuk halaman profil toko
     * yang dilihat pembeli.
     */
    fun getStoreById(storeId: String): Flow<Resource<Store>> = flow {
        emit(Resource.Loading)
        try {
            val store = supabase.postgrest
                .from(SupabaseTables.STORES)
                .select {
                    filter { eq("id", storeId) }
                }
                .decodeSingle<Store>()

            emit(Resource.Success(store))
        } catch (e: Exception) {
            Log.e(TAG, "getStoreById gagal", e)
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data toko"))
        }
    }

    // ── Update Store ──────────────────────────────────────────────────────────

    /**
     * Update info toko (nama, deskripsi, alamat, kontak, kategori).
     */
    suspend fun updateStore(
        storeId: String,
        name: String,
        description: String?,
        address: String,
        contactPhone: String?,
        category: String
    ): Resource<Store> {
        return try {
            val store = supabase.postgrest
                .from(SupabaseTables.STORES)
                .update({
                    set("name", name)
                    set("description", description)
                    set("address", address)
                    set("contact_phone", contactPhone)
                    set("category", category)
                }) {
                    filter { eq("id", storeId) }
                    select()
                }
                .decodeSingle<Store>()

            Resource.Success(store)
        } catch (e: Exception) {
            Log.e(TAG, "updateStore gagal", e)
            Resource.Error(e.localizedMessage ?: "Gagal memperbarui data toko")
        }
    }

    // ── Get Store Products ────────────────────────────────────────────────────

    /**
     * Ambil semua produk milik toko tertentu, termasuk gambar utama.
     * Diurutkan berdasarkan created_at terbaru.
     */
    fun getStoreProducts(storeId: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val products = supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        product_images ( image_url, is_primary )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("store_id", storeId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<StoreProductDto>()
                .map { it.toDomain() }

            emit(Resource.Success(products))
        } catch (e: Exception) {
            Log.e(TAG, "getStoreProducts gagal", e)
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data produk"))
        }
    }

    // ── Update Product Stock ──────────────────────────────────────────────────

    /**
     * Update stok produk. Dipakai di dashboard toko untuk quick-edit stok.
     */
    suspend fun updateProductStock(productId: String, newStock: Int): Resource<Unit> {
        return try {
            supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .update({ set("stock", newStock) }) {
                    filter { eq("id", productId) }
                }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateProductStock gagal", e)
            Resource.Error(e.localizedMessage ?: "Gagal memperbarui stok")
        }
    }

    // ── Error mapping ─────────────────────────────────────────────────────────

    private fun mapStoreError(e: Exception): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            "duplicate" in msg || "unique" in msg ->
                "Anda sudah memiliki toko terdaftar"
            "network" in msg || "unable to resolve" in msg ->
                "Tidak ada koneksi internet"
            "permission" in msg || "rls" in msg ->
                "Akses ditolak. Silakan login ulang."
            else -> e.message ?: "Terjadi kesalahan, coba lagi"
        }
    }

    companion object {
        private const val TAG = "StoreRepository"
    }
}
