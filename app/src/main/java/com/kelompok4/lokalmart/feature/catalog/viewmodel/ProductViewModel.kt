package com.kelompok4.lokalmart.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Category
import com.kelompok4.lokalmart.data.model.Product
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.catalog.data.ProductRepository
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val userName: String = "Pengguna",
    val error: String? = null
)

data class DetailState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val store: Store? = null,
    val error: String? = null,
    val isAddedToCart: Boolean = false,
    val cartError: String? = null
)

data class ProductFormState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val myStore: Store? = null
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val authRepository: AuthRepository,
    private val cartRepository: com.kelompok4.lokalmart.feature.cart.data.CartRepository,
    private val wishlistRepository: com.kelompok4.lokalmart.feature.catalog.data.WishlistRepository
) : ViewModel() {

    val wishlistProductIds: StateFlow<Set<String>> = wishlistRepository.wishlistProductIds

    fun toggleWishlist(productId: String) {
        wishlistRepository.toggleWishlist(productId)
    }

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _detailState = MutableStateFlow(DetailState())
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    fun loadHomeCatalog() {
        viewModelScope.launch {
            _homeState.value = HomeState(isLoading = true)
            
            val currentUser = try {
                authRepository.getCurrentUser()
            } catch (e: Exception) {
                null
            }
            val rawName = currentUser?.fullName ?: "Pengguna"
            val userName = rawName.split(" ").firstOrNull() ?: rawName

            // 1. Fetch categories
            productRepository.getCategories().collect { catResource ->
                when (catResource) {
                    is Resource.Success -> {
                        val categories = catResource.data
                        
                        // 2. Fetch products
                        productRepository.getProducts().collect { prodResource ->
                            when (prodResource) {
                                is Resource.Success -> {
                                    _homeState.value = HomeState(
                                        categories = categories,
                                        products = prodResource.data,
                                        userName = userName
                                    )
                                }
                                is Resource.Error -> {
                                    _homeState.value = HomeState(
                                        categories = categories,
                                        error = prodResource.message,
                                        userName = userName
                                    )
                                }
                                is Resource.Loading -> { /* Handled initially */ }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _homeState.value = HomeState(error = catResource.message, userName = userName)
                    }
                    is Resource.Loading -> { /* Handled initially */ }
                }
            }
        }
    }

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            productRepository.getProductById(productId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val (product, store) = resource.data
                        _detailState.value = DetailState(
                            product = product,
                            store = store
                        )
                    }
                    is Resource.Error -> {
                        _detailState.value = DetailState(error = resource.message)
                    }
                    is Resource.Loading -> {
                        _detailState.value = DetailState(isLoading = true)
                    }
                }
            }
        }
    }

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            val result = cartRepository.addToCart(productId, quantity)
            _detailState.update { state ->
                when (result) {
                    is Resource.Success -> state.copy(isLoading = false, isAddedToCart = true, cartError = null)
                    is Resource.Error -> state.copy(isLoading = false, cartError = result.message)
                    else -> state.copy(isLoading = false)
                }
            }
        }
    }

    fun resetCartStatus() {
        _detailState.update { it.copy(isAddedToCart = false, cartError = null) }
    }

    fun loadMyStoreForForm() {
        viewModelScope.launch {
            _formState.value = ProductFormState(isLoading = true)
            try {
                val store = storeRepository.getMyStore()
                if (store != null) {
                    _formState.value = ProductFormState(myStore = store)
                } else {
                    _formState.value = ProductFormState(error = "Anda harus mendaftarkan toko terlebih dahulu")
                }
            } catch (e: Exception) {
                _formState.value = ProductFormState(error = e.localizedMessage ?: "Gagal memuat data toko")
            }
        }
    }

    fun addProduct(
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        categoryId: Int?,
        variant: String?,
        imageBytes: ByteArray?
    ) {
        val storeId = _formState.value.myStore?.id
        if (storeId == null) {
            _formState.value = _formState.value.copy(error = "Toko tidak teridentifikasi")
            return
        }

        viewModelScope.launch {
            productRepository.addProduct(
                storeId = storeId,
                name = name,
                description = description,
                price = price,
                stock = stock,
                categoryId = categoryId,
                variant = variant,
                imageBytes = imageBytes
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _formState.value = _formState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _formState.value = _formState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _formState.value = _formState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun updateProduct(
        productId: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        categoryId: Int?,
        variant: String?,
        imageBytes: ByteArray?
    ) {
        val storeId = _formState.value.myStore?.id
        if (storeId == null) {
            _formState.value = _formState.value.copy(error = "Toko tidak teridentifikasi")
            return
        }

        viewModelScope.launch {
            productRepository.updateProduct(
                productId = productId,
                storeId = storeId,
                name = name,
                description = description,
                price = price,
                stock = stock,
                categoryId = categoryId,
                variant = variant,
                imageBytes = imageBytes
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _formState.value = _formState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _formState.value = _formState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _formState.value = _formState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun deleteProduct(productId: String, onDeleteFinished: () -> Unit) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId).collect { resource ->
                if (resource is Resource.Success) {
                    onDeleteFinished()
                }
            }
        }
    }

    fun resetFormSuccess() {
        _formState.value = _formState.value.copy(isSuccess = false, error = null)
    }
}
