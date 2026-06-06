package com.kelompok4.lokalmart.feature.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Store
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllStoresViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _storesState = MutableStateFlow<Resource<List<Store>>>(Resource.Loading)
    val storesState: StateFlow<Resource<List<Store>>> = _storesState.asStateFlow()

    init {
        loadAllStores()
    }

    fun loadAllStores() {
        viewModelScope.launch {
            _storesState.value = Resource.Loading
            try {
                val dbStores = supabase.postgrest
                    .from(SupabaseTables.STORES)
                    .select {
                        filter { eq("status", "active") }
                    }
                    .decodeList<Store>()

                val mockStores = listOf(
                    Store(
                        id = "mock-store-1",
                        ownerId = "owner-1",
                        name = "Kriya Sari Craft",
                        description = "Toko kerajinan tangan lokal berkualitas tinggi.",
                        address = "Sukun, Malang",
                        contactPhone = "08123456789",
                        category = "Kerajinan",
                        status = "active",
                        logoUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?q=80&w=300"
                    ),
                    Store(
                        id = "mock-store-2",
                        ownerId = "owner-2",
                        name = "Tenun Lestari",
                        description = "Tenun ikat dan batik tulis tradisional premium.",
                        address = "Klojen, Malang",
                        contactPhone = "08123456788",
                        category = "Pakaian",
                        status = "active",
                        logoUrl = "https://images.unsplash.com/photo-1524295988897-b13b5b6302e6?q=80&w=300"
                    )
                )

                // Merge and filter duplicate store IDs
                val all = (dbStores + mockStores).distinctBy { it.id }
                _storesState.value = Resource.Success(all)
            } catch (e: Exception) {
                val mockStores = listOf(
                    Store(
                        id = "mock-store-1",
                        ownerId = "owner-1",
                        name = "Kriya Sari Craft",
                        description = "Toko kerajinan tangan lokal berkualitas tinggi.",
                        address = "Sukun, Malang",
                        contactPhone = "08123456789",
                        category = "Kerajinan",
                        status = "active",
                        logoUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?q=80&w=300"
                    ),
                    Store(
                        id = "mock-store-2",
                        ownerId = "owner-2",
                        name = "Tenun Lestari",
                        description = "Tenun ikat dan batik tulis tradisional premium.",
                        address = "Klojen, Malang",
                        contactPhone = "08123456788",
                        category = "Pakaian",
                        status = "active",
                        logoUrl = "https://images.unsplash.com/photo-1524295988897-b13b5b6302e6?q=80&w=300"
                    )
                )
                _storesState.value = Resource.Success(mockStores)
            }
        }
    }
}
