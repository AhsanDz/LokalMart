package com.kelompok4.lokalmart.feature.review.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.AnalyticsSummary
import com.kelompok4.lokalmart.data.model.Review
import com.kelompok4.lokalmart.data.model.Store
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

// ── DTO untuk JOIN review + profiles ─────────────────────────────────────────
@Serializable
data class ReviewWithUserDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("order_id") val orderId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val profiles: ReviewUserDto? = null
)

@Serializable
data class ReviewUserDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

// ── Domain model untuk Review + info reviewer ────────────────────────────────
data class ReviewItem(
    val id: String,
    val productId: String,
    val userId: String,
    val orderId: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String?,
    val reviewerName: String,
    val reviewerAvatarUrl: String?
)

fun ReviewWithUserDto.toDomain() = ReviewItem(
    id               = id,
    productId        = productId,
    userId           = userId,
    orderId          = orderId,
    rating           = rating,
    comment          = comment,
    createdAt        = createdAt,
    reviewerName     = profiles?.fullName ?: "Anonim",
    reviewerAvatarUrl = profiles?.avatarUrl
)

// ── Dashboard data ───────────────────────────────────────────────────────────
data class DashboardData(
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalItemsSold: Int = 0,
    val averageRating: Float = 0f,
    val recentReviews: List<ReviewItem> = emptyList()
)

// ── Repository ───────────────────────────────────────────────────────────────
@Singleton
class ReviewRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private fun currentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id

    /**
     * Submit ulasan baru ke Supabase.
     */
    suspend fun submitReview(review: Review): Resource<Unit> {
        return try {
            supabase.postgrest[SupabaseTables.REVIEWS]
                .insert(review)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengirim ulasan")
        }
    }

    /**
     * Ambil semua review untuk suatu produk, JOIN ke profiles untuk info reviewer.
     */
    suspend fun getProductReviews(productId: String): Resource<List<ReviewItem>> {
        return try {
            val reviews = supabase.postgrest[SupabaseTables.REVIEWS]
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        profiles ( id, full_name, avatar_url )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("product_id", productId) }
                }
                .decodeList<ReviewWithUserDto>()
                .map { it.toDomain() }

            Resource.Success(reviews)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengambil ulasan")
        }
    }

    /**
     * Hitung rata-rata rating untuk suatu produk.
     */
    suspend fun getAverageRating(productId: String): Resource<Float> {
        return try {
            val reviews = supabase.postgrest[SupabaseTables.REVIEWS]
                .select {
                    filter { eq("product_id", productId) }
                }
                .decodeList<Review>()

            val avg = if (reviews.isNotEmpty()) {
                reviews.map { it.rating }.average().toFloat()
            } else {
                0f
            }

            Resource.Success(avg)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghitung rating")
        }
    }

    /**
     * Ambil data dashboard penjual: analytics summary + recent reviews.
     */
    suspend fun getStoreDashboard(storeId: String): Resource<DashboardData> {
        return try {
            // 1. Ambil analytics summary
            val summaries = supabase.postgrest[SupabaseTables.ANALYTICS_SUMMARY]
                .select {
                    filter { eq("store_id", storeId) }
                }
                .decodeList<AnalyticsSummary>()

            val totalOrders = summaries.sumOf { it.totalOrders }
            val totalRevenue = summaries.sumOf { it.totalRevenue }
            val totalItemsSold = summaries.sumOf { it.totalItemsSold }

            // 2. Ambil semua produk milik toko ini
            val products = supabase.postgrest[SupabaseTables.PRODUCTS]
                .select(columns = Columns.raw("id")) {
                    filter { eq("store_id", storeId) }
                }
                .decodeList<ProductIdDto>()
            val productIds = products.map { it.id }

            // 3. Ambil semua review untuk produk-produk toko ini
            var allReviews = emptyList<ReviewItem>()
            var avgRating = 0f

            if (productIds.isNotEmpty()) {
                val reviewDtos = supabase.postgrest[SupabaseTables.REVIEWS]
                    .select(
                        columns = Columns.raw(
                            """
                            *,
                            profiles ( id, full_name, avatar_url )
                            """.trimIndent()
                        )
                    ) {
                        filter { isIn("product_id", productIds) }
                    }
                    .decodeList<ReviewWithUserDto>()

                allReviews = reviewDtos.map { it.toDomain() }
                avgRating = if (allReviews.isNotEmpty()) {
                    allReviews.map { it.rating }.average().toFloat()
                } else {
                    0f
                }
            }

            // 4. Sort reviews by date descending, ambil 10 terbaru
            val recentReviews = allReviews
                .sortedByDescending { it.createdAt }
                .take(10)

            Resource.Success(
                DashboardData(
                    totalOrders    = totalOrders,
                    totalRevenue   = totalRevenue,
                    totalItemsSold = totalItemsSold,
                    averageRating  = avgRating,
                    recentReviews  = recentReviews
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal memuat dashboard")
        }
    }

    /**
     * Cek apakah user sudah pernah memberikan review untuk order+product tertentu.
     */
    suspend fun hasUserReviewed(
        userId: String,
        orderId: String,
        productId: String
    ): Resource<Boolean> {
        return try {
            val existing = supabase.postgrest[SupabaseTables.REVIEWS]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("order_id", orderId)
                        eq("product_id", productId)
                    }
                }
                .decodeList<Review>()

            Resource.Success(existing.isNotEmpty())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengecek review")
        }
    }

    /**
     * Ambil store milik user yang sedang login.
     */
    suspend fun getMyStore(): Resource<Store?> {
        return try {
            val userId = currentUserId()
                ?: return Resource.Error("Belum login")

            val store = supabase.postgrest[SupabaseTables.STORES]
                .select {
                    filter { eq("owner_id", userId) }
                }
                .decodeList<Store>()
                .firstOrNull()

            Resource.Success(store)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal mengambil data toko")
        }
    }
}

// ── DTO kecil untuk ambil product id saja ────────────────────────────────────
@Serializable
private data class ProductIdDto(val id: String)
