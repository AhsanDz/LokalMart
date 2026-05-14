package com.kelompok4.lokalmart.core.storage

import com.kelompok4.lokalmart.core.network.SupabaseBuckets
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper untuk operasi upload/get public URL ke Supabase Storage.
 *
 * Konvensi path mengikuti proposal:
 * - avatars         : {user_id}/avatar.webp
 * - store-assets    : logos/{store_id}/logo.webp | docs/{store_id}/{filename}
 * - product-images  : {store_id}/{product_id}/{filename}.webp
 * - category-icons  : {nama_kategori}.svg
 */
@Singleton
class StorageHelper @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun uploadAvatar(userId: String, bytes: ByteArray): String {
        val path = "$userId/avatar.webp"
        supabase.storage.from(SupabaseBuckets.AVATARS)
            .upload(path, bytes) { upsert = true }
        return supabase.storage.from(SupabaseBuckets.AVATARS).publicUrl(path)
    }

    suspend fun uploadStoreLogo(storeId: String, bytes: ByteArray): String {
        val path = "logos/$storeId/logo.webp"
        supabase.storage.from(SupabaseBuckets.STORE_ASSETS)
            .upload(path, bytes) { upsert = true }
        return supabase.storage.from(SupabaseBuckets.STORE_ASSETS).publicUrl(path)
    }

    suspend fun uploadProductImage(
        storeId: String,
        productId: String,
        filename: String,
        bytes: ByteArray
    ): String {
        val path = "$storeId/$productId/$filename"
        supabase.storage.from(SupabaseBuckets.PRODUCT_IMAGES)
            .upload(path, bytes) { upsert = true }
        return supabase.storage.from(SupabaseBuckets.PRODUCT_IMAGES).publicUrl(path)
    }

    suspend fun deleteFromBucket(bucket: String, paths: List<String>) {
        supabase.storage.from(bucket).delete(paths)
    }
}
