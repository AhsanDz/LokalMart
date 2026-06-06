package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val isUpdating: Boolean = false
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    private var currentStoreId: String? = null

    fun loadInventory() {
        viewModelScope.launch {
            _state.value = InventoryState(isLoading = true)
            try {
                // 1. Get owner's store
                val store = storeRepository.getMyStore()
                if (store != null) {
                    currentStoreId = store.id
                    fetchProducts(store.id)
                } else {
                    _state.value = InventoryState(error = "Toko tidak ditemukan. Silakan daftarkan toko Anda terlebih dahulu.")
                }
            } catch (e: Exception) {
                _state.value = InventoryState(error = e.localizedMessage ?: "Gagal memuat inventori")
            }
        }
    }

    private suspend fun fetchProducts(storeId: String) {
        // Fetch products DTO and map them
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
                    eq("is_active", true) // show active products
                }
            }
            .decodeList<ProductDto>()

        val products = productsDto.map { it.toDomain(storeName = "") }
        _state.value = InventoryState(products = products)
    }

    fun updateStock(productId: String, newStock: Int) {
        val storeId = currentStoreId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true)
            try {
                // Update stock in products table
                supabase.postgrest
                    .from(SupabaseTables.PRODUCTS)
                    .update(
                        {
                            set("stock", newStock)
                        }
                    ) {
                        filter { eq("id", productId) }
                    }

                // Refresh product list
                fetchProducts(storeId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUpdating = false,
                    error = e.localizedMessage ?: "Gagal memperbarui stok"
                )
            }
        }
    }
}
