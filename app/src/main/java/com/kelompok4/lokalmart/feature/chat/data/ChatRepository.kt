package com.kelompok4.lokalmart.feature.chat.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ChatMessage(
    val id: String,
    val senderId: String, // "buyer" or "seller"
    val text: String,
    val timestamp: String,
    val isRead: Boolean = true
)

@Serializable
data class ChatConversation(
    val sellerId: String,
    val sellerName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val logoUrl: String? = null,
    val messages: List<ChatMessage> = emptyList()
)

@Singleton
class ChatRepository @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _conversations = MutableStateFlow<List<ChatConversation>>(
        listOf(
            ChatConversation(
                sellerId = "mock-store-1",
                sellerName = "Kriya Sari Craft",
                lastMessage = "Halo, produk kami selalu ready kak. Silakan diorder ya!",
                lastMessageTime = "14:20",
                logoUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?q=80&w=300",
                messages = listOf(
                    ChatMessage("m1", "buyer", "Halo, apakah Tas Anyaman Rotan ready?", "14:15"),
                    ChatMessage("m2", "seller", "Halo, produk kami selalu ready kak. Silakan diorder ya!", "14:20")
                )
            ),
            ChatConversation(
                sellerId = "mock-store-2",
                sellerName = "Tenun Lestari",
                lastMessage = "Baik kak, pesanan Kakak sedang kami proses.",
                lastMessageTime = "10:35",
                logoUrl = "https://images.unsplash.com/photo-1524295988897-b13b5b6302e6?q=80&w=300",
                messages = listOf(
                    ChatMessage("m3", "buyer", "Saya sudah bayar untuk Batik Tulisnya ya kak", "10:30"),
                    ChatMessage("m4", "seller", "Baik kak, pesanan Kakak sedang kami proses.", "10:35")
                )
            )
        )
    )
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    fun addConversation(conv: ChatConversation) {
        if (_conversations.value.none { it.sellerId == conv.sellerId }) {
            _conversations.update { it + conv }
        }
    }

    fun createOrGetConversation(sellerId: String, sellerName: String): ChatConversation {
        val existing = _conversations.value.firstOrNull { it.sellerId == sellerId }
        if (existing != null) return existing

        val newConv = ChatConversation(
            sellerId = sellerId,
            sellerName = sellerName,
            lastMessage = "Mulai obrolan baru...",
            lastMessageTime = LocalTime.now().format(timeFormatter),
            messages = emptyList()
        )
        _conversations.update { it + newConv }
        return newConv
    }

    fun sendMessage(sellerId: String, text: String) {
        val timeNow = LocalTime.now().format(timeFormatter)
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = "buyer",
            text = text,
            timestamp = timeNow
        )

        // 1. Append user message
        _conversations.update { list ->
            list.map { conv ->
                if (conv.sellerId == sellerId) {
                    conv.copy(
                        lastMessage = text,
                        lastMessageTime = timeNow,
                        messages = conv.messages + userMsg
                    )
                } else {
                    conv
                }
            }
        }

        // 2. Simulate typing and auto-reply from seller after 1.5 seconds
        scope.launch {
            delay(1500)
            val replyText = when (sellerId) {
                "mock-store-1" -> "Halo kak! Terima kasih telah menghubungi kami. Kami siap melayani pemesanan kerajinan anyaman rotan custom. Mau pesan ukuran berapa kak?"
                "mock-store-2" -> "Sore kak, batik tulis motif Sekar ready ukuran M, L, dan XL ya. Silakan langsung diorder kak sebelum kehabisan."
                else -> "Halo kak, terima kasih sudah menghubungi toko kami. Pesan Kakak sudah kami terima dan akan segera kami jawab secepatnya ya! 😊"
            }
            val replyTime = LocalTime.now().format(timeFormatter)
            val sellerMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = "seller",
                text = replyText,
                timestamp = replyTime
            )

            _conversations.update { list ->
                list.map { conv ->
                    if (conv.sellerId == sellerId) {
                        conv.copy(
                            lastMessage = replyText,
                            lastMessageTime = replyTime,
                            messages = conv.messages + sellerMsg
                        )
                    } else {
                        conv
                    }
                }
            }
        }
    }

    fun sendSellerMessage(sellerId: String, text: String) {
        val timeNow = java.time.LocalTime.now().format(timeFormatter)
        val sellerMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = "seller",
            text = text,
            timestamp = timeNow
        )

        // 1. Append seller message
        _conversations.update { list ->
            list.map { conv ->
                if (conv.sellerId == sellerId) {
                    conv.copy(
                        lastMessage = text,
                        lastMessageTime = timeNow,
                        messages = conv.messages + sellerMsg
                    )
                } else {
                    conv
                }
            }
        }

        // 2. Simulate reply from buyer after 1.5 seconds
        scope.launch {
            delay(1500)
            val replyText = "Baik kak, terima kasih informasinya! Ditunggu kirimannya ya. 👍"
            val replyTime = java.time.LocalTime.now().format(timeFormatter)
            val buyerMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = "buyer",
                text = replyText,
                timestamp = replyTime
            )

            _conversations.update { list ->
                list.map { conv ->
                    if (conv.sellerId == sellerId) {
                        conv.copy(
                            lastMessage = replyText,
                            lastMessageTime = replyTime,
                            messages = conv.messages + buyerMsg
                        )
                    } else {
                        conv
                    }
                }
            }
        }
    }
}
