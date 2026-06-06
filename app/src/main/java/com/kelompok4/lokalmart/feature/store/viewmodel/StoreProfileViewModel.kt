package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

data class StoreProfileState(
    val isLoading: Boolean = false,
    val store: Store? = null,
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val isOwner: Boolean = false
)

@Serializable
data class ProductDto(
    val id: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("category_id") val categoryId: Int? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int,
    val variant: String? = null,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("product_images") val productImages: List<ProductImageDto>? = null
)

@Serializable
data class ProductImageDto(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

fun ProductDto.toDomain(
    storeName: String,
    reviews: List<com.kelompok4.lokalmart.data.model.Review> = emptyList(),
    orderItems: List<com.kelompok4.lokalmart.data.model.OrderItem> = emptyList()
): Product {
    val prodReviews = reviews.filter { it.productId == id }
    val avgRating = if (prodReviews.isEmpty()) {
        when (id) {
            "mock-1" -> 4.9f
            "mock-2" -> 4.8f
            "mock-3" -> 4.7f
            "mock-4" -> 5.0f
            else -> 0f
        }
    } else prodReviews.map { it.rating }.average().toFloat()
    
    val totalSold = orderItems.filter { it.productId == id }.sumOf { it.quantity }
    val finalSold = if (totalSold == 0) {
        when (id) {
            "mock-1" -> 120
            "mock-2" -> 34
            "mock-3" -> 56
            "mock-4" -> 89
            else -> 0
        }
    } else totalSold

    return Product(
        id = id,
        storeId = storeId,
        categoryId = categoryId,
        name = name,
        description = description,
        price = price,
        stock = stock,
        variant = variant,
        isActive = isActive,
        storeName = storeName,
        imageUrl = productImages?.firstOrNull { it.isPrimary }?.imageUrl
            ?: productImages?.firstOrNull()?.imageUrl,
        rating = avgRating,
        soldCount = finalSold
    )
}

@HiltViewModel
class StoreProfileViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow(StoreProfileState())
    val state: StateFlow<StoreProfileState> = _state.asStateFlow()

    fun loadStoreProfile(storeId: String) {
        viewModelScope.launch {
            _state.value = StoreProfileState(isLoading = true)
            try {
                // 1. Fetch store info
                val store = supabase.postgrest
                    .from(SupabaseTables.STORES)
                    .select {
                        filter { eq("id", storeId) }
                    }
                    .decodeSingleOrNull<Store>()

                if (store != null) {
                    // 2. Fetch products of this store
                    val productsDto = supabase.postgrest
                        .from(SupabaseTables.PRODUCTS)
                        .select(
                            columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                                """
                                *,
                                product_images ( image_url, is_primary )
                                """.trimIndent()
                            )
                        ) {
                            filter {
                                eq("store_id", storeId)
                                eq("is_active", true)
                            }
                        }
                        .decodeList<ProductDto>()

                    val reviews = try {
                        supabase.postgrest.from(SupabaseTables.REVIEWS).select().decodeList<com.kelompok4.lokalmart.data.model.Review>()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val orderItems = try {
                        supabase.postgrest.from(SupabaseTables.ORDER_ITEMS).select().decodeList<com.kelompok4.lokalmart.data.model.OrderItem>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val products = productsDto.map { it.toDomain(store.name, reviews, orderItems) }
                    val currentUserId = supabase.auth.currentUserOrNull()?.id
                    val isOwner = store.ownerId == currentUserId

                    _state.value = StoreProfileState(
                        store = store,
                        products = products,
                        isOwner = isOwner
                    )
                } else {
                    _state.value = StoreProfileState(error = "Toko tidak ditemukan")
                }
            } catch (e: Exception) {
                _state.value = StoreProfileState(error = e.localizedMessage ?: "Gagal memuat profil toko")
            }
        }
    }
}
