package com.kelompok4.lokalmart.feature.checkout.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SavedPaymentMethod(
    val id: String,
    val type: String, // "qris", "gopay", "bank_transfer"
    val name: String, // e.g., "GoPay", "Transfer Bank BCA"
    val details: String, // e.g., "0812-****-7890", "1234567890"
    val isPrimary: Boolean = false
)

@Singleton
class PaymentMethodRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("lokalmart_payments", Context.MODE_PRIVATE)

    private val _paymentMethods = MutableStateFlow<List<SavedPaymentMethod>>(loadPaymentMethods())
    val paymentMethods: StateFlow<List<SavedPaymentMethod>> = _paymentMethods.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<SavedPaymentMethod?>(
        loadSelectedPaymentMethod() ?: _paymentMethods.value.firstOrNull { it.isPrimary } ?: _paymentMethods.value.firstOrNull()
    )
    val selectedPaymentMethod: StateFlow<SavedPaymentMethod?> = _selectedPaymentMethod.asStateFlow()

    private fun loadPaymentMethods(): List<SavedPaymentMethod> {
        val jsonString = sharedPrefs.getString("payment_methods_list", null)
        if (jsonString.isNullOrBlank()) {
            val defaults = listOf(
                SavedPaymentMethod("pay-1", "qris", "QRIS", "QRIS — Semua e-wallet", true),
                SavedPaymentMethod("pay-2", "gopay", "GoPay", "0812-****-7890"),
                SavedPaymentMethod("pay-3", "bank_transfer", "Virtual Account BCA", "123-456-7890")
            )
            savePaymentMethodsList(defaults)
            return defaults
        }
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePaymentMethodsList(list: List<SavedPaymentMethod>) {
        sharedPrefs.edit().putString("payment_methods_list", Json.encodeToString(list)).apply()
    }

    private fun loadSelectedPaymentMethod(): SavedPaymentMethod? {
        val jsonString = sharedPrefs.getString("selected_payment_method", null)
        return try {
            jsonString?.let { Json.decodeFromString<SavedPaymentMethod>(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun selectPaymentMethod(method: SavedPaymentMethod) {
        _selectedPaymentMethod.value = method
        sharedPrefs.edit().putString("selected_payment_method", Json.encodeToString(method)).apply()
    }

    fun addPaymentMethod(method: SavedPaymentMethod) {
        val currentList = _paymentMethods.value.toMutableList()
        if (method.isPrimary) {
            for (i in currentList.indices) {
                currentList[i] = currentList[i].copy(isPrimary = false)
            }
        }
        currentList.add(method)
        _paymentMethods.value = currentList
        savePaymentMethodsList(currentList)

        if (currentList.size == 1 || method.isPrimary) {
            selectPaymentMethod(method)
        }
    }

    fun deletePaymentMethod(id: String) {
        val currentList = _paymentMethods.value.filter { it.id != id }
        _paymentMethods.value = currentList
        savePaymentMethodsList(currentList)

        if (_selectedPaymentMethod.value?.id == id) {
            val nextSelected = currentList.firstOrNull { it.isPrimary } ?: currentList.firstOrNull()
            if (nextSelected != null) {
                selectPaymentMethod(nextSelected)
            } else {
                _selectedPaymentMethod.value = null
                sharedPrefs.edit().remove("selected_payment_method").apply()
            }
        }
    }
}
