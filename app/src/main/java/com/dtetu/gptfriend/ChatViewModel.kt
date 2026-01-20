package com.dtetu.gptfriend

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dtetu.gptfriend.data.repository.ChatRepository
import com.dtetu.gptfriend.data.repository.MessageRepository
import com.dtetu.gptfriend.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val context: Context
) : ViewModel() {

    val messages: Flow<List<Message>> = messageRepository.getAllMessages()
    
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled
    
    // Track user message count for ad display
    private val _userMessageCount = MutableStateFlow(0)
    private val _shouldShowAd = MutableStateFlow(false)
    val shouldShowAd: StateFlow<Boolean> = _shouldShowAd
    
    companion object {
        private const val AD_FREQUENCY = 5 // Show ad every 5 user messages
    }

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

            // If it's a user message, trigger API call and check for ad display
            // Response will be saved directly to local database by ChatRepository
            // UI will be updated automatically via the Flow<List<Message>>
            if (isUser) {
                // Increment user message counter
                _userMessageCount.value += 1
                
                // Check if we should show an ad (every 5 user messages)
                if (_userMessageCount.value % AD_FREQUENCY == 0) {
                    _shouldShowAd.value = true
                }
                
                // Get recent conversation history (last 40 messages, excluding the current one)
                // This gives GPT context while managing token limits
                val conversationHistory = messageRepository.getRecentMessages(40)
                    .filter { !it.text.contains("API key not configured") && !it.text.contains("FATAL on init") }
                
                // API call updates local database directly (local-first principle)
            // Reset message counter when clearing conversation
            _userMessageCount.value = 0
                chatRepository.getChatResponse(text, conversationHistory)
            }
        }
    }
    
    /**
     * Reset the ad display flag after showing the ad
     */
    fun onAdShown() {
        _shouldShowAd.value = false
    }

    fun clearConversation() {
        viewModelScope.launch {
            messageRepository.clearAllMessages()
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        if (enabled) {
            NotificationScheduler.scheduleNotifications(context)
        } else {
            NotificationScheduler.cancelNotifications(context)
        }
    }
}

class ChatViewModelFactory(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(messageRepository, chatRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
