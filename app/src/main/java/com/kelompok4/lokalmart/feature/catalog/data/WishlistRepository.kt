package com.kelompok4.lokalmart.feature.catalog.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("lokalmart_wishlist", Context.MODE_PRIVATE)
    
    private val _wishlistProductIds = MutableStateFlow<Set<String>>(loadWishlist())
    val wishlistProductIds: StateFlow<Set<String>> = _wishlistProductIds.asStateFlow()

    private fun loadWishlist(): Set<String> {
        return sharedPrefs.getStringSet("product_ids", emptySet()) ?: emptySet()
    }

    private fun saveWishlist(set: Set<String>) {
        sharedPrefs.edit().putStringSet("product_ids", set).apply()
    }

    fun toggleWishlist(productId: String) {
        val current = _wishlistProductIds.value
        val updated = if (current.contains(productId)) {
            current - productId
        } else {
            current + productId
        }
        _wishlistProductIds.value = updated
        saveWishlist(updated)
    }

    fun isWishlisted(productId: String): Boolean {
        return _wishlistProductIds.value.contains(productId)
    }

    fun getWishlistCount(): Int {
        return _wishlistProductIds.value.size
    }
}
