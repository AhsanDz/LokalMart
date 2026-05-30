package com.kelompok4.lokalmart.feature.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.feature.cart.data.CartItem
import com.kelompok4.lokalmart.feature.cart.data.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    // Edit mode
    val isEditMode: Boolean = false,

    // Kalkulasi
    val selectedCount: Int = 0,
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0,
)

// Helper: group items by storeName
val CartUiState.itemsByStore: Map<String, List<CartItem>>
    get() = items.groupBy { it.storeName }

val CartUiState.selectedItems: List<CartItem>
    get() = items.filter { it.isSelected }

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { loadCart() }

    // ── Load ──────────────────────────────────────────────────────────────────
    fun loadCart() {
        viewModelScope.launch {
            repository.getCartItems().collect { resource ->
                when (resource) {
                    Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        val items = resource.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items     = items,
                            ).recalculate()
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                }
            }
        }
    }

    // ── Quantity ──────────────────────────────────────────────────────────────
    fun increment(cartId: String) {
        val item = _uiState.value.items.find { it.cartId == cartId } ?: return
        if (item.quantity >= item.productStock) return  // jangan melebihi stok

        // Optimistic update di UI
        updateItemQuantity(cartId, item.quantity + 1)

        viewModelScope.launch {
            val result = repository.updateQuantity(cartId, item.quantity + 1)
            if (result is Resource.Error) {
                // Rollback kalau gagal
                updateItemQuantity(cartId, item.quantity)
                _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun decrement(cartId: String) {
        val item = _uiState.value.items.find { it.cartId == cartId } ?: return

        if (item.quantity <= 1) {
            // Quantity jadi 0 → hapus item
            removeItem(cartId)
            return
        }

        updateItemQuantity(cartId, item.quantity - 1)

        viewModelScope.launch {
            val result = repository.updateQuantity(cartId, item.quantity - 1)
            if (result is Resource.Error) {
                updateItemQuantity(cartId, item.quantity)
                _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    private fun updateItemQuantity(cartId: String, newQty: Int) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map {
                    if (it.cartId == cartId) it.copy(quantity = newQty) else it
                }
            ).recalculate()
        }
    }

    // ── Remove ────────────────────────────────────────────────────────────────
    fun removeItem(cartId: String) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.cartId != cartId }).recalculate()
        }
        viewModelScope.launch {
            val result = repository.removeFromCart(cartId)
            if (result is Resource.Error) {
                _uiState.update { it.copy(error = result.message) }
                loadCart() // reload kalau gagal
            }
        }
    }

    // ── Edit mode ─────────────────────────────────────────────────────────────
    fun toggleEditMode() {
        _uiState.update { state ->
            val entering = !state.isEditMode
            state.copy(
                isEditMode = entering,
                // Saat masuk edit mode: deselect semua
                items = if (entering) state.items.map { it.copy(isSelected = false) }
                else state.items.map { it.copy(isSelected = true) },
            ).recalculate()
        }
    }

    fun toggleItemSelection(cartId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map {
                    if (it.cartId == cartId) it.copy(isSelected = !it.isSelected) else it
                }
            ).recalculate()
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val allSelected = state.items.all { it.isSelected }
            state.copy(
                items = state.items.map { it.copy(isSelected = !allSelected) }
            ).recalculate()
        }
    }

    fun deleteSelected() {
        val selectedIds = _uiState.value.selectedItems.map { it.cartId }
        if (selectedIds.isEmpty()) return

        _uiState.update { state ->
            state.copy(
                items      = state.items.filter { !selectedIds.contains(it.cartId) },
                isEditMode = false,
            ).recalculate()
        }

        viewModelScope.launch {
            val result = repository.removeMultiple(selectedIds)
            if (result is Resource.Error) {
                _uiState.update { it.copy(error = result.message) }
                loadCart()
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}

// ── Extension: hitung ulang total setiap kali items berubah ──────────────────
private fun CartUiState.recalculate(): CartUiState {
    val selected = items.filter { it.isSelected }
    return copy(
        selectedCount = selected.size,
        totalItems    = selected.sumOf { it.quantity },
        totalPrice    = selected.sumOf { it.productPrice * it.quantity },
    )
}