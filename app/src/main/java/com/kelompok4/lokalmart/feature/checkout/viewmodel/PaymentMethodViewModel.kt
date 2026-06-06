package com.kelompok4.lokalmart.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.feature.checkout.data.PaymentMethodRepository
import com.kelompok4.lokalmart.feature.checkout.data.SavedPaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    val paymentMethods: StateFlow<List<SavedPaymentMethod>> = paymentMethodRepository.paymentMethods
    val selectedPaymentMethod: StateFlow<SavedPaymentMethod?> = paymentMethodRepository.selectedPaymentMethod

    fun selectPaymentMethod(method: SavedPaymentMethod) {
        paymentMethodRepository.selectPaymentMethod(method)
    }

    fun addPaymentMethod(type: String, name: String, details: String, isPrimary: Boolean) {
        viewModelScope.launch {
            val newMethod = SavedPaymentMethod(
                id = java.util.UUID.randomUUID().toString(),
                type = type,
                name = name,
                details = details,
                isPrimary = isPrimary
            )
            paymentMethodRepository.addPaymentMethod(newMethod)
        }
    }

    fun deletePaymentMethod(id: String) {
        viewModelScope.launch {
            paymentMethodRepository.deletePaymentMethod(id)
        }
    }
}
