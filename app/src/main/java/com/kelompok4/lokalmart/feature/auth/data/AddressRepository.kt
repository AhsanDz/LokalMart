package com.kelompok4.lokalmart.feature.auth.data

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
data class SavedAddress(
    val id: String,
    val name: String,
    val phone: String,
    val label: String, // e.g., "Rumah", "Kantor"
    val fullAddress: String,
    val isPrimary: Boolean = false
)

@Singleton
class AddressRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("lokalmart_addresses", Context.MODE_PRIVATE)

    private val _addresses = MutableStateFlow<List<SavedAddress>>(loadAddresses())
    val addresses: StateFlow<List<SavedAddress>> = _addresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow<SavedAddress?>(
        loadSelectedAddress() ?: _addresses.value.firstOrNull { it.isPrimary } ?: _addresses.value.firstOrNull()
    )
    val selectedAddress: StateFlow<SavedAddress?> = _selectedAddress.asStateFlow()

    private fun loadAddresses(): List<SavedAddress> {
        val jsonString = sharedPrefs.getString("addresses_list", null)
        if (jsonString.isNullOrBlank()) {
            val defaultAddress = SavedAddress(
                id = "default-addr-1",
                name = "Sari Wulandari",
                phone = "0812-3456-7890",
                label = "Rumah",
                fullAddress = "Jl. Bunga Kana 12B, Lowokwaru, Kota Malang, Jawa Timur 65141",
                isPrimary = true
            )
            val list = listOf(defaultAddress)
            saveAddressesList(list)
            return list
        }
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAddressesList(list: List<SavedAddress>) {
        sharedPrefs.edit().putString("addresses_list", Json.encodeToString(list)).apply()
    }

    private fun loadSelectedAddress(): SavedAddress? {
        val jsonString = sharedPrefs.getString("selected_address", null)
        return try {
            jsonString?.let { Json.decodeFromString<SavedAddress>(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun selectAddress(address: SavedAddress) {
        _selectedAddress.value = address
        sharedPrefs.edit().putString("selected_address", Json.encodeToString(address)).apply()
    }

    fun addAddress(address: SavedAddress) {
        val currentList = _addresses.value.toMutableList()
        if (address.isPrimary) {
            for (i in currentList.indices) {
                currentList[i] = currentList[i].copy(isPrimary = false)
            }
        }
        currentList.add(address)
        _addresses.value = currentList
        saveAddressesList(currentList)

        if (currentList.size == 1 || address.isPrimary) {
            selectAddress(address)
        }
    }

    fun updateAddress(updated: SavedAddress) {
        val currentList = _addresses.value.map {
            if (it.id == updated.id) {
                updated
            } else {
                if (updated.isPrimary) it.copy(isPrimary = false) else it
            }
        }
        _addresses.value = currentList
        saveAddressesList(currentList)

        if (updated.isPrimary || _selectedAddress.value?.id == updated.id) {
            selectAddress(updated)
        }
    }

    fun deleteAddress(id: String) {
        val currentList = _addresses.value.filter { it.id != id }
        _addresses.value = currentList
        saveAddressesList(currentList)

        if (_selectedAddress.value?.id == id) {
            val nextSelected = currentList.firstOrNull { it.isPrimary } ?: currentList.firstOrNull()
            if (nextSelected != null) {
                selectAddress(nextSelected)
            } else {
                _selectedAddress.value = null
                sharedPrefs.edit().remove("selected_address").apply()
            }
        }
    }
}
