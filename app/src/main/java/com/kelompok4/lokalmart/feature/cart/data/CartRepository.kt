package com.kelompok4.lokalmart.feature.cart.data

import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Cart
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

// ── DTO ───────────────────────────────────────────────────────────────────────
@Serializable
data class CartItemDto(
    val id: String,
    @SerialName("user_id")    val userId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    // JOIN ke products
    val products: CartProductDto? = null,
)

@Serializable
data class CartProductDto(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val variant: String? = null,
    @SerialName("is_active") val isActive: Boolean,
    // JOIN ke stores
    val stores: CartStoreDto? = null,
    // JOIN ke product_images
    @SerialName("product_images") val productImages: List<CartImageDto>? = null,
)

@Serializable
data class CartStoreDto(
    val id: String,
    val name: String,
    val status: String,
)

@Serializable
data class CartImageDto(
    @SerialName("image_url")  val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean,
)

// ── Domain model untuk Cart item yang sudah di-join ───────────────────────────
data class CartItem(
    val cartId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productStock: Int,
    val variant: String?,
    val imageUrl: String?,
    val storeName: String,
    val storeId: String,
    val isStoreActive: Boolean,
    val quantity: Int,
    val isSelected: Boolean = true,   // untuk fitur edit/pilih item
)

fun CartItemDto.toDomain(isSelected: Boolean = true) = CartItem(
    cartId        = id,
    productId     = productId,
    productName   = products?.name ?: "-",
    productPrice  = products?.price ?: 0.0,
    productStock  = products?.stock ?: 0,
    variant       = products?.variant,
    imageUrl      = products?.productImages
        ?.firstOrNull { it.isPrimary }?.imageUrl
        ?: products?.productImages?.firstOrNull()?.imageUrl,
    storeName     = products?.stores?.name ?: "-",
    storeId       = products?.stores?.id ?: "",
    isStoreActive = products?.stores?.status == "active",
    quantity      = quantity,
    isSelected    = isSelected,
)

// ── Repository ────────────────────────────────────────────────────────────────
@Singleton
class CartRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private fun currentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id

    /**
     * Ambil semua item keranjang user yang sedang login.
     * JOIN ke products → stores & product_images.
     */
    fun getCartItems(): Flow<Resource<List<CartItem>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = currentUserId()
                ?: throw Exception("Belum login")

            val items = supabase.postgrest["carts"]
                .select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                        """
                        *,
                        products (
                            id, name, price, stock, variant, is_active,
                            stores ( id, name, status ),
                            product_images ( image_url, is_primary )
                        )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<CartItemDto>()
                .map { it.toDomain() }

            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil keranjang"))
        }
    }

    /**
     * Tambah produk ke keranjang.
     * Kalau produk sudah ada, update quantity-nya.
     */
    suspend fun addToCart(productId: String, quantity: Int = 1): Resource<Unit> {
        return try {
            val userId = currentUserId()
                ?: return Resource.Error("Belum login")

            // Cek stok produk terlebih dahulu
            val product = supabase.postgrest["products"]
                .select {
                    filter { eq("id", productId) }
                }
                .decodeSingleOrNull<CartProductDto>()

            if (product == null) {
                return Resource.Error("Produk tidak ditemukan")
            }

            if (product.stock <= 0 || !product.isActive) {
                return Resource.Error("Stok produk habis!")
            }

            // Cek apakah produk sudah ada di keranjang
            val existing = supabase.postgrest["carts"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("product_id", productId)
                    }
                }
                .decodeList<CartItemDto>()
                .firstOrNull()

            if (existing != null) {
                val newQty = existing.quantity + quantity
                if (newQty > product.stock) {
                    return Resource.Error("Jumlah di keranjang melebihi stok yang tersedia (${product.stock} pcs)!")
                }
                // Update quantity
                supabase.postgrest["carts"]
                    .update({
                        set("quantity", newQty)
                    }) {
                        filter { eq("id", existing.id) }
                    }
            } else {
                if (quantity > product.stock) {
                    return Resource.Error("Jumlah melebihi stok yang tersedia (${product.stock} pcs)!")
                }
                // Insert baru
                val cartInsert = buildJsonObject {
                    put("user_id", userId)
                    put("product_id", productId)
                    put("quantity", quantity)
                }
                supabase.postgrest["carts"]
                    .insert(cartInsert)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menambah ke keranjang")
        }
    }

    /**
     * Update quantity item keranjang.
     * Kalau quantity <= 0, hapus item.
     */
    suspend fun updateQuantity(cartId: String, newQuantity: Int): Resource<Unit> {
        return try {
            if (newQuantity <= 0) {
                removeFromCart(cartId)
            } else {
                supabase.postgrest["carts"]
                    .update(mapOf("quantity" to newQuantity)) {
                        filter { eq("id", cartId) }
                    }
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal update quantity")
        }
    }

    /**
     * Hapus item dari keranjang.
     */
    suspend fun removeFromCart(cartId: String): Resource<Unit> {
        return try {
            supabase.postgrest["carts"]
                .delete { filter { eq("id", cartId) } }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus item")
        }
    }

    /**
     * Hapus banyak item sekaligus (untuk fitur edit mode).
     */
    suspend fun removeMultiple(cartIds: List<String>): Resource<Unit> {
        return try {
            cartIds.forEach { cartId ->
                supabase.postgrest["carts"]
                    .delete { filter { eq("id", cartId) } }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus item")
        }
    }
}