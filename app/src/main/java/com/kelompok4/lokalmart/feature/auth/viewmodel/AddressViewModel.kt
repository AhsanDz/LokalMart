package com.kelompok4.lokalmart.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.feature.auth.data.AddressRepository
import com.kelompok4.lokalmart.feature.auth.data.SavedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    val addresses: StateFlow<List<SavedAddress>> = addressRepository.addresses
    val selectedAddress: StateFlow<SavedAddress?> = addressRepository.selectedAddress

    fun selectAddress(address: SavedAddress) {
        addressRepository.selectAddress(address)
    }

    fun addAddress(name: String, phone: String, label: String, fullAddress: String, isPrimary: Boolean) {
        viewModelScope.launch {
            val newAddress = SavedAddress(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                label = label,
                fullAddress = fullAddress,
                isPrimary = isPrimary
            )
            addressRepository.addAddress(newAddress)
        }
    }

    fun updateAddress(id: String, name: String, phone: String, label: String, fullAddress: String, isPrimary: Boolean) {
        viewModelScope.launch {
            val updated = SavedAddress(
                id = id,
                name = name,
                phone = phone,
                label = label,
                fullAddress = fullAddress,
                isPrimary = isPrimary
            )
            addressRepository.updateAddress(updated)
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(id)
        }
    }
}
