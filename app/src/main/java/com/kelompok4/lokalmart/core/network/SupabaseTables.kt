package com.kelompok4.lokalmart.core.network

/**
 * Constants untuk nama tabel di Supabase.
 * Pakai konstanta supaya tidak ada typo & mudah refactor.
 */
object SupabaseTables {
    const val PROFILES = "profiles"
    const val STORES = "stores"
    const val STORE_VERIFICATIONS = "store_verifications"
    const val CATEGORIES = "categories"
    const val PRODUCTS = "products"
    const val PRODUCT_IMAGES = "product_images"
    const val CARTS = "carts"
    const val ORDERS = "orders"
    const val ORDER_ITEMS = "order_items"
    const val PAYMENTS = "payments"
    const val REVIEWS = "reviews"
    const val ANALYTICS_SUMMARY = "analytics_summary"
}

/**
 * Konstanta nama bucket di Supabase Storage.
 */
object SupabaseBuckets {
    const val AVATARS = "avatars"
    const val STORE_ASSETS = "store-assets"
    const val PRODUCT_IMAGES = "product-images"
    const val CATEGORY_ICONS = "category-icons"
}
