package com.kelompok4.lokalmart.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.data.model.OrderItem
import com.kelompok4.lokalmart.data.model.Payment
import com.kelompok4.lokalmart.feature.checkout.data.CheckoutRepository
import com.kelompok4.lokalmart.feature.cart.data.CartItem
import com.kelompok4.lokalmart.feature.cart.data.CartRepository
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import com.kelompok4.lokalmart.feature.auth.data.AddressRepository
import com.kelompok4.lokalmart.feature.auth.data.SavedAddress
import com.kelompok4.lokalmart.feature.checkout.data.PaymentMethodRepository
import com.kelompok4.lokalmart.feature.checkout.data.SavedPaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val selectedPaymentMethod: String = "qris",
    val shippingAddress: String = "",
    val selectedAddress: SavedAddress? = null,
    val selectedPaymentMethodDto: SavedPaymentMethod? = null,
    val orderId: String? = null,
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val buyerId: String = ""
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: CheckoutRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val addressRepository: AddressRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            addressRepository.selectedAddress.collect { address ->
                _uiState.update {
                    it.copy(
                        selectedAddress = address,
                        shippingAddress = address?.fullAddress ?: ""
                    )
                }
            }
        }
        viewModelScope.launch {
            paymentMethodRepository.selectedPaymentMethod.collect { method ->
                _uiState.update {
                    it.copy(
                        selectedPaymentMethodDto = method,
                        selectedPaymentMethod = method?.type ?: "qris"
                    )
                }
            }
        }
    }

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setShippingAddress(address: String) {
        _uiState.update { it.copy(shippingAddress = address) }
    }

    fun loadCheckoutDetails(storeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val userId = authRepository.currentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Belum login") }
                return@launch
            }

            cartRepository.getCartItems().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val filteredItems = (resource.data ?: emptyList()).filter { it.storeId == storeId && it.isSelected }
                        val total = filteredItems.sumOf { it.productPrice * it.quantity }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items = filteredItems,
                                totalPrice = total,
                                buyerId = userId
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun placeOrder(storeId: String) {
        val state = _uiState.value
        val buyerId = state.buyerId
        val totalPrice = state.totalPrice
        val cartItems = state.items

        if (buyerId.isBlank() || cartItems.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Data checkout tidak valid") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val orderId = UUID.randomUUID().toString()
            val order = Order(
                id = orderId,
                buyerId = buyerId,
                storeId = storeId,
                totalPrice = totalPrice,
                shippingAddress = _uiState.value.shippingAddress.ifBlank { "Jl. Bunga Kana 12B, Lowokwaru, Kota Malang, Jawa Timur 65141" },
                status = "pending"
            )

            when (val orderResult = repository.placeOrder(order)) {
                is Resource.Success -> {
                    val orderItems = cartItems.map {
                        OrderItem(
                            id = UUID.randomUUID().toString(),
                            orderId = orderId,
                            productId = it.productId,
                            quantity = it.quantity,
                            priceAtOrder = it.productPrice
                        )
                    }
                    repository.insertOrderItems(orderItems)

                    // Decrement product stock
                    cartItems.forEach { item ->
                        repository.decrementProductStock(item.productId, item.quantity)
                    }

                    val payment = Payment(
                        id = UUID.randomUUID().toString(),
                        orderId = orderId,
                        method = _uiState.value.selectedPaymentMethod,
                        amount = totalPrice,
                        status = "pending"
                    )
                    repository.insertPayment(payment)

                    // Clear checked out items from cart table in Supabase
                    val cartIds = cartItems.map { it.cartId }
                    cartRepository.removeMultiple(cartIds)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            orderId = orderId
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = orderResult.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
}