package com.kelompok4.lokalmart.feature.search.data

import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

// ── DTO: hasil JOIN products + stores + rating agregat
@Serializable
data class ProductWithStoreDto(
    val id: String,
    @SerialName("store_id")   val storeId: String,
    @SerialName("category_id") val categoryId: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int,
    val variant: String? = null,
    @SerialName("is_active")  val isActive: Boolean,
    // JOIN ke tabel stores (nested object dari Supabase)
    val stores: StoreDto? = null,
    // JOIN ke tabel product_images (ambil 1 foto utama)
    @SerialName("product_images") val productImages: List<ProductImageDto>? = null,
    // Agregat rating dari tabel reviews — pakai computed column / view di Supabase
    // Kalau belum ada, set null dulu dan nanti hitung client-side
    @SerialName("avg_rating")   val avgRating: Double? = null,
    @SerialName("sold_count")   val soldCount: Int? = null,
)

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val address: String? = null,
)

@Serializable
data class ProductImageDto(
    @SerialName("image_url")  val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean,
)

// Extension: DTO → domain model
fun ProductWithStoreDto.toDomain() = Product(
    id          = id,
    storeId     = storeId,
    categoryId  = categoryId,
    name        = name,
    description = description,
    price       = price,
    stock       = stock,
    variant     = variant,
    isActive    = isActive,
    storeName   = stores?.name,
    imageUrl    = productImages
        ?.firstOrNull { it.isPrimary }?.imageUrl
        ?: productImages?.firstOrNull()?.imageUrl,
    rating      = avgRating?.toFloat() ?: 0f,
    soldCount   = soldCount ?: 0,
)

// ── Enum sort ─────────────────────────────────────────────────────────────────
enum class SortOption(val label: String) {
    TERLARIS("Terlaris"),
    TERMURAH("Termurah"),
    TERMAHAL("Termahal"),
    RATING("Rating"),
    TERBARU("Terbaru")
}

// ── Repository ────────────────────────────────────────────────────────────────
@Singleton
class SearchRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    val categories = listOf("Kerajinan", "Fashion", "Alas Kaki", "Kuliner", "Elektronik")

    // Mapping nama kategori → category_id sesuai tabel categories di Supabase
    private val categoryIdMap = mapOf(
        "Kerajinan"   to 1,
        "Fashion"     to 2,
        "Alas Kaki"   to 3,
        "Kuliner"     to 4,
        "Elektronik"  to 5,
    )

    /**
     * Search produk dengan filter dan sort.
     *
     * Query Supabase:
     *   SELECT products.*, stores(id, name, address), product_images(image_url, is_primary)
     *   FROM products
     *   WHERE is_active = true
     *     AND name ILIKE '%query%'          -- jika query tidak kosong
     *     AND category_id = ?              -- jika filter kategori aktif
     *     AND price >= ?                   -- jika minPrice aktif
     *     AND price <= ?                   -- jika maxPrice aktif
     *   ORDER BY sold_count DESC / price / avg_rating / created_at
     */
    fun searchProducts(
        query: String,
        category: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        location: String? = null,   // filter lokasi dilakukan client-side via stores.address
        sortBy: SortOption = SortOption.TERLARIS,
    ): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val results = supabase.postgrest["products"]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        *,
                        stores ( id, name, address ),
                        product_images ( image_url, is_primary )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("is_active", true)

                        if (query.isNotBlank()) {
                            ilike("name", "%$query%")
                        }
                        if (category != null) {
                            val catId = categoryIdMap[category]
                            if (catId != null) eq("category_id", catId)
                        }
                        if (minPrice != null) gte("price", minPrice)
                        if (maxPrice != null) lte("price", maxPrice)
                    }

                    when (sortBy) {
                        SortOption.TERLARIS -> order("sold_count", Order.DESCENDING)
                        SortOption.TERMURAH -> order("price", Order.ASCENDING)
                        SortOption.TERMAHAL -> order("price", Order.DESCENDING)
                        SortOption.RATING   -> order("avg_rating", Order.DESCENDING)
                        SortOption.TERBARU  -> order("created_at", Order.DESCENDING)
                    }
                }
                .decodeList<ProductWithStoreDto>()
                .map { it.toDomain() }

            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data produk"))
        }
    }
}