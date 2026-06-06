package com.kelompok4.lokalmart.feature.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok4.lokalmart.feature.chat.data.ChatConversation
import com.kelompok4.lokalmart.feature.chat.data.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val conversations: StateFlow<List<ChatConversation>> = chatRepository.conversations

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    fun selectConversation(sellerId: String, sellerName: String) {
        viewModelScope.launch {
            val conv = chatRepository.createOrGetConversation(sellerId, sellerName)
            _activeConversation.value = conv

            // Keep observing changes to the active conversation
            chatRepository.conversations.collect { list ->
                _activeConversation.value = list.firstOrNull { it.sellerId == sellerId }
            }
        }
    }

    fun sendMessage(sellerId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(sellerId, text)
        }
    }
}
