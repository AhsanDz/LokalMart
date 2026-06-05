package com.kelompok4.lokalmart.feature.store.data

import android.net.Uri

// Data class utama form Buka Toko
data class StoreFormModel(
    val logoUri          : Uri?   = null,
    val namaUsaha        : String = "",
    val kategoriUsaha    : String = "",
    val deskripsiSingkat : String = "",
    val alamatLengkap    : String = "",
    val nomorWhatsApp    : String = ""
)

// List kategori usaha untuk dropdown
val kategoriUsahaList = listOf(
    "Fashion & Aksesoris",
    "Makanan & Minuman",
    "Elektronik",
    "Kecantikan & Perawatan",
    "Olahraga & Outdoor",
    "Rumah & Taman",
    "Otomotif",
    "Buku & Alat Tulis",
    "Mainan & Hobi",
    "Lainnya"
)

// Konstanta validasi
object StoreFormConstants {
    const val MAX_DESKRIPSI_LENGTH = 200
    const val MIN_NAMA_LENGTH      = 3
    const val MIN_NOMOR_WA_LENGTH  = 10
}