package com.kelompok4.lokalmart.feature.notification.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Pesanan", "Promo", "Info"
    val timestamp: String,
    val isRead: Boolean = false
)

@Singleton
class NotificationRepository @Inject constructor() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                id = "notif-1",
                title = "Pesanan Selesai",
                description = "Pesanan #ORD-98721 telah selesai. Terima kasih telah berbelanja di Kriya Sari Craft!",
                category = "Pesanan",
                timestamp = "1 jam yang lalu",
                isRead = false
            ),
            NotificationItem(
                id = "notif-2",
                title = "Promo Spesial UMKM",
                description = "Dapatkan cashback s/d Rp 20.000 khusus pembelian produk Tenun hari ini!",
                category = "Promo",
                timestamp = "4 jam yang lalu",
                isRead = false
            ),
            NotificationItem(
                id = "notif-3",
                title = "Pesanan Dikirim",
                description = "Pesanan #ORD-12345 sedang dalam perjalanan oleh kurir Mitra Lokal.",
                category = "Pesanan",
                timestamp = "1 hari yang lalu",
                isRead = true
            ),
            NotificationItem(
                id = "notif-4",
                title = "Selamat Datang di LokalMart",
                description = "Mulai belanja produk UMKM terbaik di sekitarmu sekarang juga!",
                category = "Info",
                timestamp = "2 hari yang lalu",
                isRead = true
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map { 
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun getUnreadCount(): Int {
        return _notifications.value.count { !it.isRead }
    }
}
