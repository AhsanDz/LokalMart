package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.feature.store.data.StoreOrderDto
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellerOrdersState(
    val isLoading: Boolean = false,
    val orders: List<StoreOrderDto> = emptyList(),
    val storeId: String = "",
    val error: String? = null
)

@HiltViewModel
class SellerOrdersViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SellerOrdersState())
    val state: StateFlow<SellerOrdersState> = _state.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val store = storeRepository.getMyStore()
                if (store != null) {
                    val orders = storeRepository.getStoreOrders(store.id)
                    _state.value = SellerOrdersState(
                        orders = orders,
                        storeId = store.id
                    )
                } else {
                    _state.value = SellerOrdersState(error = "Toko tidak ditemukan")
                }
            } catch (e: Exception) {
                _state.value = SellerOrdersState(error = e.localizedMessage ?: "Gagal memuat pesanan")
            }
        }
    }

    fun confirmOrder(orderId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = storeRepository.updateOrderStatus(orderId, "shipped")
            if (result is Resource.Success) {
                loadOrders()
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = storeRepository.updateOrderStatus(orderId, "cancelled")
            if (result is Resource.Success) {
                loadOrders()
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }
    }
}
