package com.kelompok4.lokalmart

/**
 * SMOKE TEST screen — TEMPORARY!
 *
 * Tujuan: verifikasi koneksi Supabase di Android Studio berhasil.
 * Ambil list kategori dari tabel `categories` (yang policy SELECT-nya public).
 *
 * HAPUS file ini setelah Step 6 selesai. Kembalikan AppNavHost.Splash route
 * ke PlaceholderScreen atau ganti dengan SplashScreen beneran nanti.
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.core.network.SupabaseTables
import com.kelompok4.lokalmart.core.util.Resource
import com.kelompok4.lokalmart.data.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmokeTestViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Category>>>(Resource.Loading)
    val state: StateFlow<Resource<List<Category>>> = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        _state.value = Resource.Loading
        viewModelScope.launch {
            try {
                val categories = supabase.postgrest
                    .from(SupabaseTables.CATEGORIES)
                    .select()
                    .decodeList<Category>()
                _state.value = Resource.Success(categories)
            } catch (e: Exception) {
                _state.value = Resource.Error(
                    message = e.message ?: "Unknown error",
                    throwable = e
                )
            }
        }
    }
}

@Composable
fun SmokeTestScreen(
    viewModel: SmokeTestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🔌 Supabase Smoke Test",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Mengambil daftar kategori dari Supabase...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is Resource.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading...")
                }
            }

            is Resource.Success -> {
                Text(
                    "✅ Berhasil! Dapat ${s.data.size} kategori",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(s.data) { category ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "#${category.id}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(category.name)
                            }
                        }
                    }
                }
            }

            is Resource.Error -> {
                Text(
                    "❌ Gagal koneksi",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = s.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.loadCategories() }) {
            Text("Test lagi")
        }
    }
}
