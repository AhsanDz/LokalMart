package com.kelompok4.lokalmart.feature.catalog.data

import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.storage.StorageHelper
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Category
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
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
    @SerialName("product_images") val productImages: List<ProductImageDto>? = null,
    val stores: StoreNameDto? = null
)

@Serializable
data class StoreNameDto(
    val name: String,
    val address: String? = null
)

@Serializable
data class ProductImageDto(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

@Serializable
data class ProductDtoInput(
    val id: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("category_id") val categoryId: Int?,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val variant: String?,
    @SerialName("is_active") val isActive: Boolean
)

@Serializable
data class ProductImageDtoInput(
    @SerialName("product_id") val productId: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

fun ProductDto.toDomain(
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
        storeName = stores?.name ?: "Toko Lokal",
        imageUrl = productImages?.firstOrNull { it.isPrimary }?.imageUrl
            ?: productImages?.firstOrNull()?.imageUrl,
        rating = avgRating,
        soldCount = finalSold
    )
}


@Singleton
class ProductRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val storageHelper: StorageHelper
) {

    // Fallback list of categories to match Home.png mockup
    private val mockCategories = listOf(
        Category(1, "Pakaian"),
        Category(2, "Tas"),
        Category(3, "Sepatu"),
        Category(4, "Kerajinan"),
        Category(5, "Aksesoris"),
        Category(6, "Batik & Tenun"),
        Category(7, "Dekor Rumah"),
        Category(8, "Lainnya")
    )

    // Fallback list of products to match Home.png mockup
    private val mockProducts = listOf(
        Product(
            id = "mock-1",
            storeId = "store-1",
            categoryId = 2,
            name = "Tas Anyaman Rotan Handmade",
            description = "Tas anyaman rotan handmade dari pengrajin lokal Yogyakarta. Bahan rotan alami pilihan, dianyam manual selama 3 hari per tas. Kuat, ringan, dan estetik.",
            price = 18000.0,
            stock = 28,
            variant = "Natural, Cokelat, Hitam, Cream",
            isActive = true,
            imageUrl = "https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=600",
            storeName = "Kriya Sari Craft",
            rating = 4.9f,
            soldCount = 120
        ),
        Product(
            id = "mock-2",
            storeId = "store-2",
            categoryId = 6,
            name = "Batik Tulis Tangan Motif Sekar",
            description = "Kemeja Batik Tulis Tangan dengan Motif Sekar Jagad khas Solo. Dibuat dengan bahan katun primissima yang halus dan sejuk dipakai.",
            price = 185000.0,
            stock = 12,
            variant = "M, L, XL",
            isActive = true,
            imageUrl = "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?q=80&w=600",
            storeName = "Tenun Lestari",
            rating = 4.8f,
            soldCount = 34
        ),
        Product(
            id = "mock-3",
            storeId = "store-1",
            categoryId = 4,
            name = "Anyaman Rotan Tempat Buah",
            description = "Keranjang anyaman rotan multifungsi, sangat cocok sebagai tempat buah di meja makan atau dekorasi rumah bergaya rustic.",
            price = 45000.0,
            stock = 15,
            variant = "Kecil, Besar",
            isActive = true,
            imageUrl = "https://images.unsplash.com/photo-1606744824163-985d376605aa?q=80&w=600",
            storeName = "Kriya Sari Craft",
            rating = 4.7f,
            soldCount = 56
        ),
        Product(
            id = "mock-4",
            storeId = "store-2",
            categoryId = 8,
            name = "Topi Pandan Anyaman Khas Bali",
            description = "Topi pantai dari anyaman daun pandan asli khas Bali. Desain stylish dan nyaman untuk melindungi wajah dari sinar matahari.",
            price = 62000.0,
            stock = 20,
            variant = "Natural",
            isActive = true,
            imageUrl = "https://images.unsplash.com/photo-1572426466154-1a9237ec7bad?q=80&w=600",
            storeName = "Tenun Lestari",
            rating = 5.0f,
            soldCount = 89
        )
    )

    fun getCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        try {
            val categories = supabase.postgrest
                .from(SupabaseTables.CATEGORIES)
                .select()
                .decodeList<Category>()
            
            if (categories.isEmpty()) {
                emit(Resource.Success(mockCategories))
            } else {
                emit(Resource.Success(categories))
            }
        } catch (e: Exception) {
            emit(Resource.Success(mockCategories)) // Fallback to mock so UI is never empty
        }
    }

    fun getProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
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

            val productsDto = supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        product_images ( image_url, is_primary ),
                        stores ( name )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("is_active", true)
                    }
                }
                .decodeList<ProductDto>()

            val products = productsDto.map { it.toDomain(reviews, orderItems) }
            if (products.isEmpty()) {
                emit(Resource.Success(mockProducts))
            } else {
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            emit(Resource.Success(mockProducts)) // Fallback to mock on network error
        }
    }


    fun getProductById(productId: String): Flow<Resource<Pair<Product, Store>>> = flow {
        emit(Resource.Loading)
        try {
            // Check if mock
            val mockProduct = mockProducts.firstOrNull { it.id == productId }
            if (mockProduct != null) {
                val mockStore = Store(
                    id = mockProduct.storeId,
                    ownerId = "owner-1",
                    name = mockProduct.storeName ?: "Toko Lokal",
                    description = "Toko kerajinan tangan lokal berkualitas tinggi.",
                    address = "Sukun, Malang • 0.4 km",
                    contactPhone = "08123456789",
                    category = "Kerajinan",
                    status = "active"
                )
                emit(Resource.Success(Pair(mockProduct, mockStore)))
                return@flow
            }

            // Fetch product from postgrest
            val productDto = supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        product_images ( image_url, is_primary ),
                        stores ( id, owner_id, name, description, address, contact_phone, category, status, logo_url )
                        """.trimIndent()
                    )
                ) {
                    filter { eq("id", productId) }
                }
                .decodeSingleOrNull<ProductDto>()

            if (productDto != null) {
                // Decode store object separately because nested deserialization might need direct decoding
                val store = supabase.postgrest
                    .from(SupabaseTables.STORES)
                    .select {
                        filter { eq("id", productDto.storeId) }
                    }
                    .decodeSingleOrNull<Store>()

                if (store != null) {
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
                    val domainProduct = productDto.toDomain(reviews, orderItems)
                    emit(Resource.Success(Pair(domainProduct, store)))
                } else {
                    emit(Resource.Error("Toko tidak ditemukan"))
                }
            } else {
                emit(Resource.Error("Produk tidak ditemukan"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat detail produk"))
        }
    }

    fun addProduct(
        storeId: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        categoryId: Int?,
        variant: String?,
        imageBytes: ByteArray?
    ): Flow<Resource<Product>> = flow {
        emit(Resource.Loading)
        try {
            val productId = UUID.randomUUID().toString()
            
            val imageUrl = if (imageBytes != null) {
                storageHelper.uploadProductImage(
                    storeId = storeId,
                    productId = productId,
                    filename = "product_primary.webp",
                    bytes = imageBytes
                )
            } else {
                null
            }

            val inputDto = ProductDtoInput(
                id = productId,
                storeId = storeId,
                categoryId = categoryId,
                name = name,
                description = description,
                price = price,
                stock = stock,
                variant = variant,
                isActive = true
            )

            supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .insert(inputDto)

            if (imageUrl != null) {
                val imageInput = ProductImageDtoInput(
                    productId = productId,
                    imageUrl = imageUrl,
                    isPrimary = true
                )
                supabase.postgrest
                    .from(SupabaseTables.PRODUCT_IMAGES)
                    .insert(imageInput)
            }

            emit(Resource.Success(
                Product(
                    id = productId,
                    storeId = storeId,
                    categoryId = categoryId,
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    variant = variant,
                    isActive = true,
                    imageUrl = imageUrl
                )
            ))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mendaftarkan produk"))
        }
    }

    fun updateProduct(
        productId: String,
        storeId: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        categoryId: Int?,
        variant: String?,
        imageBytes: ByteArray?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            // Upload new image if provided
            val imageUrl = if (imageBytes != null) {
                storageHelper.uploadProductImage(
                    storeId = storeId,
                    productId = productId,
                    filename = "product_primary_updated.webp",
                    bytes = imageBytes
                )
            } else {
                null
            }

            // Update product fields
            supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .update({
                    set("name", name)
                    set("description", description)
                    set("price", price)
                    set("stock", stock)
                    set("category_id", categoryId)
                    set("variant", variant)
                }) {
                    filter { eq("id", productId) }
                }

            if (imageUrl != null) {
                // Delete old images (or mark as non-primary)
                supabase.postgrest
                    .from(SupabaseTables.PRODUCT_IMAGES)
                    .delete {
                        filter { eq("product_id", productId) }
                    }

                // Insert new primary image
                val imageInput = ProductImageDtoInput(
                    productId = productId,
                    imageUrl = imageUrl,
                    isPrimary = true
                )
                supabase.postgrest
                    .from(SupabaseTables.PRODUCT_IMAGES)
                    .insert(imageInput)
            }

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memperbarui produk"))
        }
    }

    fun deleteProduct(productId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            // Soft delete: set is_active to false
            supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .update({
                    set("is_active", false)
                }) {
                    filter { eq("id", productId) }
                }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal menghapus produk"))
        }
    }

    fun getProductsByCategory(categoryId: Int): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
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

            val productsDto = supabase.postgrest
                .from(SupabaseTables.PRODUCTS)
                .select(
                    columns = Columns.raw(
                        """
                        *,
                        product_images ( image_url, is_primary ),
                        stores ( name )
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("is_active", true)
                        eq("category_id", categoryId)
                    }
                }
                .decodeList<ProductDto>()

            val products = productsDto.map { it.toDomain(reviews, orderItems) }
            // If empty, filter mock products for dev demonstration
            if (products.isEmpty()) {
                val mockFiltered = mockProducts.filter { it.categoryId == categoryId }
                emit(Resource.Success(mockFiltered))
            } else {
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            val mockFiltered = mockProducts.filter { it.categoryId == categoryId }
            emit(Resource.Success(mockFiltered))
        }
    }
}
