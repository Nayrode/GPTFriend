package com.dtetu.gptfriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dtetu.gptfriend.data.repository.ChatRepository
import com.dtetu.gptfriend.data.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val messages: Flow<List<Message>> = messageRepository.getAllMessages()

    init {
        viewModelScope.launch {
            val apiKey = BuildConfig.OPENAI_API_KEY
            chatRepository.initialize(apiKey).onFailure { error ->
                messageRepository.saveMessage(error.message ?: "Unknown error", isUser = false)
            }
        }
    }

    fun sendMessage(text: String, isUser: Boolean) {
        viewModelScope.launch {
            // Save user message to local database (source of truth)
            messageRepository.saveMessage(text, isUser)

            // If it's a user message, trigger API call
            // Response will be saved directly to local database by ChatRepository
            // UI will be updated automatically via the Flow<List<Message>>
            if (isUser) {
                // Get recent conversation history (last 40 messages, excluding the current one)
                // This gives GPT context while managing token limits
                val conversationHistory = messageRepository.getRecentMessages(40)
                    .filter { !it.text.contains("API key not configured") && !it.text.contains("FATAL on init") }
                
                // API call updates local database directly (local-first principle)
                chatRepository.getChatResponse(text, conversationHistory)
            }
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            messageRepository.clearAllMessages()
        }
    }
}

class ChatViewModelFactory(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(messageRepository, chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
