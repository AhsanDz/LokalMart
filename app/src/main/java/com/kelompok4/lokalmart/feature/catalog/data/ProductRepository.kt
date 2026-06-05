package com.kelompok4.lokalmart.feature.catalog.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Category
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.ProductImage
import com.kelompok4.lokalmart.data.model.Review
import com.kelompok4.lokalmart.data.model.Store
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

// ── DTO: Hasil JOIN products → stores + product_images ──────────────────────

@Serializable
data class ProductWithStoreDto(
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
    @SerialName("avg_rating") val avgRating: Double? = null,
    @SerialName("sold_count") val soldCount: Int? = null,
    val stores: EmbeddedStoreDto? = null,
    @SerialName("product_images") val productImages: List<EmbeddedImageDto>? = null,
)

@Serializable
data class EmbeddedStoreDto(
    val id: String,
    val name: String,
    val address: String? = null,
    val status: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val category: String? = null,
    val description: String? = null,
)

@Serializable
data class EmbeddedImageDto(
    val id: String? = null,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean = false,
)

// ── DTO: Hasil JOIN untuk detail produk — termasuk reviews ──────────────────

@Serializable
data class ProductDetailDto(
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
    @SerialName("avg_rating") val avgRating: Double? = null,
    @SerialName("sold_count") val soldCount: Int? = null,
    val stores: EmbeddedStoreDto? = null,
    @SerialName("product_images") val productImages: List<EmbeddedImageDto>? = null,
    val reviews: List<EmbeddedReviewDto>? = null,
)

@Serializable
data class EmbeddedReviewDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("order_id") val orderId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// ── Extension: DTO → domain model ──────────────────────────────────────────

fun ProductWithStoreDto.toDomain() = Product(
    id = id,
    storeId = storeId,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    stock = stock,
    variant = variant,
    isActive = isActive,
    createdAt = createdAt,
    storeName = stores?.name,
    imageUrl = productImages
        ?.firstOrNull { it.isPrimary }?.imageUrl
        ?: productImages?.firstOrNull()?.imageUrl,
    rating = avgRating?.toFloat() ?: 0f,
    soldCount = soldCount ?: 0,
)

fun ProductDetailDto.toDomainProduct() = Product(
    id = id,
    storeId = storeId,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    stock = stock,
    variant = variant,
    isActive = isActive,
    createdAt = createdAt,
    storeName = stores?.name,
    imageUrl = productImages
        ?.firstOrNull { it.isPrimary }?.imageUrl
        ?: productImages?.firstOrNull()?.imageUrl,
    rating = avgRating?.toFloat() ?: 0f,
    soldCount = soldCount ?: 0,
)

fun ProductDetailDto.toDomainImages(): List<ProductImage> =
    productImages?.mapIndexed { index, dto ->
        ProductImage(
            id = dto.id ?: "img_$index",
            productId = id,
            imageUrl = dto.imageUrl,
            isPrimary = dto.isPrimary,
        )
    } ?: emptyList()

fun ProductDetailDto.toDomainStore(): Store? = stores?.let { s ->
    Store(
        id = s.id,
        ownerId = "",
        name = s.name,
        address = s.address ?: "",
        category = s.category ?: "",
        status = s.status ?: "active",
        logoUrl = s.logoUrl,
        contactPhone = s.contactPhone,
        description = s.description,
    )
}

fun ProductDetailDto.toDomainReviews(): List<Review> =
    reviews?.map { r ->
        Review(
            id = r.id,
            productId = id,
            userId = r.userId,
            orderId = r.orderId,
            rating = r.rating,
            comment = r.comment,
            createdAt = r.createdAt,
        )
    } ?: emptyList()

// ── Data class gabungan untuk Product Detail ────────────────────────────────

data class ProductDetailData(
    val product: Product,
    val images: List<ProductImage>,
    val store: Store?,
    val reviews: List<Review>,
    val averageRating: Float,
    val totalReviews: Int,
)

// ── Repository ──────────────────────────────────────────────────────────────

@Singleton
class ProductRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    /**
     * Ambil semua kategori dari tabel categories.
     */
    fun getCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        try {
            val categories = supabase.postgrest[SupabaseTables.CATEGORIES]
                .select()
                .decodeList<Category>()
            emit(Resource.Success(categories))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat kategori"))
        }
    }

    /**
     * Ambil daftar produk aktif, opsional filter berdasarkan category_id.
     * JOIN ke tabel stores dan product_images.
     */
    fun getProducts(categoryId: Int? = null): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val results = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        stores ( id, name, address, status ),
                        product_images ( id, image_url, is_primary )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("is_active", true)
                        if (categoryId != null) {
                            eq("category_id", categoryId)
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ProductWithStoreDto>()
                .map { it.toDomain() }

            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat produk"))
        }
    }

    /**
     * Ambil detail produk beserta semua gambar, info toko, dan review.
     */
    fun getProductDetail(productId: String): Flow<Resource<ProductDetailData>> = flow {
        emit(Resource.Loading)
        try {
            val dto = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        stores ( id, name, address, status, logo_url, contact_phone, category, description ),
                        product_images ( id, image_url, is_primary ),
                        reviews ( id, user_id, order_id, rating, comment, created_at )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("id", productId)
                    }
                }
                .decodeSingle<ProductDetailDto>()

            val reviews = dto.toDomainReviews()
            val avgRating = if (reviews.isNotEmpty()) {
                reviews.map { it.rating }.average().toFloat()
            } else {
                dto.avgRating?.toFloat() ?: 0f
            }

            val detail = ProductDetailData(
                product = dto.toDomainProduct(),
                images = dto.toDomainImages(),
                store = dto.toDomainStore(),
                reviews = reviews,
                averageRating = avgRating,
                totalReviews = reviews.size,
            )
            emit(Resource.Success(detail))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat detail produk"))
        }
    }

    /**
     * Ambil semua produk aktif dari toko tertentu.
     */
    fun getProductsByStore(storeId: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val results = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        stores ( id, name, address, status ),
                        product_images ( id, image_url, is_primary )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("store_id", storeId)
                        eq("is_active", true)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ProductWithStoreDto>()
                .map { it.toDomain() }

            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat produk toko"))
        }
    }
}
