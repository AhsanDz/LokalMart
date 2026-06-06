package com.kelompok4.lokalmart.feature.review.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Review
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
class ReviewRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {
    /**
     * Submit ulasan baru untuk suatu produk dalam pesanan tertentu.
     */
    fun submitReview(
        productId: String,
        orderId: String,
        rating: Int,
        comment: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val userId = authRepository.currentUserId()
                ?: throw IllegalStateException("Pengguna tidak terautentikasi")

            val review = Review(
                id = UUID.randomUUID().toString(),
                productId = productId,
                userId = userId,
                orderId = orderId,
                rating = rating,
                comment = comment?.takeIf { it.isNotBlank() }
            )

            supabase.postgrest[SupabaseTables.REVIEWS].insert(review)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengirim ulasan"))
        }
    }

    /**
     * Mengambil daftar ulasan untuk suatu produk beserta nama & foto profil pengulas.
     */
    fun getProductReviews(productId: String): Flow<Resource<List<ReviewWithUserDto>>> = flow {
        emit(Resource.Loading)
        try {
            val result = supabase.postgrest[SupabaseTables.REVIEWS]
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        profiles ( full_name, avatar_url )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("product_id", productId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<ReviewWithUserDto>()

            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat ulasan produk"))
        }
    }

    /**
     * Mengambil daftar ID produk yang sudah diulas oleh user pada pesanan tertentu.
     */
    suspend fun getReviewedProductIdsForOrder(orderId: String): List<String> {
        val userId = authRepository.currentUserId() ?: return emptyList()
        return try {
            val result = supabase.postgrest[SupabaseTables.REVIEWS]
                .select {
                    filter {
                        eq("order_id", orderId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<Review>()
            result.map { it.productId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Mengambil daftar ulasan yang ditulis oleh user tertentu.
     */
    suspend fun getUserReviews(userId: String): List<Review> {
        return try {
            supabase.postgrest[SupabaseTables.REVIEWS]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Review>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

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
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
