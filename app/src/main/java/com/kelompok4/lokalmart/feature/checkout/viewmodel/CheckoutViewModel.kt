package com.kelompok4.lokalmart.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Order
import com.kelompok4.lokalmart.data.model.OrderItem
import com.kelompok4.lokalmart.data.model.Payment
import com.kelompok4.lokalmart.feature.checkout.data.CheckoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val selectedPaymentMethod: String = "qris",
    val shippingAddress: String = "",
    val orderId: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: CheckoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }

    fun setShippingAddress(address: String) {
        _uiState.value = _uiState.value.copy(shippingAddress = address)
    }

    fun placeOrder(
        buyerId: String,
        storeId: String,
        items: List<OrderItem>,
        totalPrice: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val orderId = UUID.randomUUID().toString()
            val order = Order(
                id = orderId,
                buyerId = buyerId,
                storeId = storeId,
                totalPrice = totalPrice,
                shippingAddress = _uiState.value.shippingAddress,
                status = "pending"
            )

            when (val orderResult = repository.placeOrder(order)) {
                is Resource.Success -> {
                    val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
                    repository.insertOrderItems(itemsWithOrderId)

                    val payment = Payment(
                        id = UUID.randomUUID().toString(),
                        orderId = orderId,
                        method = _uiState.value.selectedPaymentMethod,
                        amount = totalPrice,
                        status = "pending"
                    )
                    repository.insertPayment(payment)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        orderId = orderId
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = orderResult.message
                    )
                }
                else -> {}
            }
        }
    }
}