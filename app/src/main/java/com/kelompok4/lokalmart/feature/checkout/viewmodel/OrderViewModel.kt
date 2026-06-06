package com.kelompok4.lokalmart.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.feature.checkout.data.CheckoutRepository
import com.kelompok4.lokalmart.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.kelompok4.lokalmart.feature.checkout.data.OrderItemDetailDto
import com.kelompok4.lokalmart.feature.checkout.data.OrderWithDetailsDto
import com.kelompok4.lokalmart.feature.checkout.data.OrderDetailWithStoreAndPaymentDto
import kotlinx.coroutines.flow.update

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderWithDetailsDto> = emptyList(),
    val selectedOrder: OrderDetailWithStoreAndPaymentDto? = null,
    val selectedOrderItems: List<OrderItemDetailDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: CheckoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState

    fun fetchOrders() {
        val buyerId = authRepository.currentUserId()
        if (buyerId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesi berakhir, silakan login kembali"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getOrders(buyerId)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = result.data ?: emptyList()
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                else -> {}
            }
        }
    }

    fun fetchOrderDetail(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val orderResult = repository.getOrderDetail(orderId)
            val itemsResult = repository.getOrderItemsDetail(orderId)
            
            if (orderResult is Resource.Success && itemsResult is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedOrder = orderResult.data,
                        selectedOrderItems = itemsResult.data ?: emptyList()
                    )
                }
            } else {
                val errorMsg = (orderResult as? Resource.Error)?.message 
                    ?: (itemsResult as? Resource.Error)?.message 
                    ?: "Gagal memuat detail pesanan"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    fun confirmOrderReceipt(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.updateOrderStatus(orderId, "delivered")
            if (result is Resource.Success) {
                fetchOrderDetail(orderId)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = (result as Resource.Error).message
                    )
                }
            }
        }
    }

    fun processPayment(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "confirmed")
            repository.updatePaymentStatus(orderId, "paid")
        }
    }

    fun getOrdersByStatus(status: String): List<OrderWithDetailsDto> {
        return _uiState.value.orders.filter { it.status == status }
    }
}