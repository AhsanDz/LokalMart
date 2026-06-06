package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Store
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyStoreState(
    val isLoading: Boolean = false,
    val store: Store? = null,
    val todayRevenue: Double = 0.0,
    val newOrdersCount: Int = 0,
    val activeProductsCount: Int = 0,
    val newOrders: List<com.kelompok4.lokalmart.feature.store.data.StoreOrderDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MyStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyStoreState())
    val state: StateFlow<MyStoreState> = _state.asStateFlow()

    fun loadMyStore() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val store = storeRepository.getMyStore()
                if (store != null) {
                    val productsCount = storeRepository.getActiveProductsCount(store.id)
                    val orders = storeRepository.getStoreOrders(store.id)
                    
                    val todayString = java.time.LocalDate.now().toString()
                    val todayRevenue = orders
                        .filter { it.createdAt?.startsWith(todayString) == true && it.status != "pending" && it.status != "cancelled" }
                        .sumOf { it.totalPrice }
                        
                    val newOrdersCount = orders.count { it.status == "confirmed" }
                    
                    _state.value = MyStoreState(
                        store = store,
                        todayRevenue = todayRevenue,
                        newOrdersCount = newOrdersCount,
                        activeProductsCount = productsCount,
                        newOrders = orders.filter { it.status == "confirmed" || it.status == "pending" }
                    )
                } else {
                    _state.value = MyStoreState(error = "Anda belum mendaftarkan toko")
                }
            } catch (e: Exception) {
                _state.value = MyStoreState(error = e.localizedMessage ?: "Gagal memuat data toko")
            }
        }
    }

    fun confirmOrder(orderId: String) {
        viewModelScope.launch {
            val result = storeRepository.updateOrderStatus(orderId, "shipped")
            if (result is Resource.Success) {
                loadMyStore()
            }
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            val result = storeRepository.updateOrderStatus(orderId, "cancelled")
            if (result is Resource.Success) {
                loadMyStore()
            }
        }
    }
}
