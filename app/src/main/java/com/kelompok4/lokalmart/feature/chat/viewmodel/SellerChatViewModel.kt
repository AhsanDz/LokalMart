package com.kelompok4.lokalmart.feature.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.feature.chat.data.ChatConversation
import com.kelompok4.lokalmart.feature.chat.data.ChatRepository
import com.kelompok4.lokalmart.feature.store.data.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    private var currentStoreId: String = ""

    fun loadConversations() {
        viewModelScope.launch {
            val store = storeRepository.getMyStore()
            if (store != null) {
                currentStoreId = store.id
                // Filter conversations that belong to this store or start with store.id (unique simulated IDs)
                chatRepository.conversations.collect { list ->
                    val filtered = list.filter { it.sellerId == store.id || it.sellerId.startsWith("${store.id}_") }
                    if (filtered.isEmpty()) {
                        // Pre-populate mock conversations with unique IDs for this store
                        val mock1 = ChatConversation(
                            sellerId = "${store.id}_0",
                            sellerName = "Ahsan Faqih (Pembeli)",
                            lastMessage = "Halo, apakah Tas Anyaman Rotan ready?",
                            lastMessageTime = "14:20",
                            logoUrl = null,
                            messages = listOf(
                                com.kelompok4.lokalmart.feature.chat.data.ChatMessage("m1", "buyer", "Halo, apakah Tas Anyaman Rotan ready?", "14:15"),
                                com.kelompok4.lokalmart.feature.chat.data.ChatMessage("m2", "seller", "Halo, produk kami selalu ready kak. Silakan diorder ya!", "14:20")
                            )
                        )
                        val mock2 = ChatConversation(
                            sellerId = "${store.id}_1",
                            sellerName = "Wulandari (Pembeli)",
                            lastMessage = "Baik kak, pesanan Kakak sedang kami proses.",
                            lastMessageTime = "10:35",
                            logoUrl = null,
                            messages = listOf(
                                com.kelompok4.lokalmart.feature.chat.data.ChatMessage("m3", "buyer", "Saya sudah bayar untuk Batik Tulisnya ya kak", "10:30"),
                                com.kelompok4.lokalmart.feature.chat.data.ChatMessage("m4", "seller", "Baik kak, pesanan Kakak sedang kami proses.", "10:35")
                            )
                        )
                        chatRepository.addConversation(mock1)
                        chatRepository.addConversation(mock2)
                    } else {
                        _conversations.value = filtered
                    }
                }
            }
        }
    }

    fun selectConversation(sellerId: String, sellerName: String) {
        viewModelScope.launch {
            val conv = chatRepository.createOrGetConversation(sellerId, sellerName)
            _activeConversation.value = conv

            chatRepository.conversations.collect { list ->
                _activeConversation.value = list.firstOrNull { it.sellerId == sellerId }
            }
        }
    }

    fun sendMessage(sellerId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendSellerMessage(sellerId, text)
        }
    }
}
