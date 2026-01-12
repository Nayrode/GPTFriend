package com.dtetu.gptfriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.internal.platform.android.AndroidSocketAdapter.Companion.factory
import kotlin.time.Duration.Companion.seconds

class ChatViewModel(private val messageDao: MessageDao) : ViewModel() {

    val messages: Flow<List<Message>> = messageDao.getAllMessages()
    private var openAI: OpenAI? = null

    init {
        viewModelScope.launch {
            val apiKey = BuildConfig.OPENAI_API_KEY
            if (apiKey.isBlank() || apiKey == "null" || apiKey == "YOUR_API_KEY") {
                messageDao.insertMessage(
                    Message(text = "API key not configured. Please add OPENAI_API_KEY to your ~/.gradle/gradle.properties file and restart.", isUser = false)
                )
            } else {
                try {
                    val config = OpenAIConfig(
                        token = apiKey,
                        timeout = Timeout(socket = 60.seconds),
                    )
                    openAI = OpenAI(config)
                    messageDao.insertMessage(
                        Message(text = "OpenAI client initialized successfully with OkHttp engine.", isUser = false)
                    )
                } catch (t: Throwable) {
                    openAI = null
                    messageDao.insertMessage(
                        Message(text = "FATAL on init: ${t.javaClass.simpleName}: ${t.message}. Check Logcat for stacktrace.", isUser = false)
                    )
                }
            }
        }
    }

    fun sendMessage(text: String, isUser: Boolean) {
        viewModelScope.launch {
            val userMessage = Message(text = text, isUser = isUser)
            messageDao.insertMessage(userMessage)

            if (isUser) {
                val currentClient = openAI
                if (currentClient == null) {
                    messageDao.insertMessage(Message(text = "OpenAI client is not initialized. Please check previous error messages and restart the app.", isUser = false))
                    return@launch
                }

                val chatMessages = listOf(
                    ChatMessage(role = ChatRole.System, content = "You are a helpful assistant!"),
                    ChatMessage(role = ChatRole.User, content = text)
                )
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = chatMessages
                )

                try {
                    val completion = currentClient.chatCompletion(chatCompletionRequest)
                    completion.choices.first().message?.content?.let { response ->
                        val gptMessage = Message(text = response, isUser = false)
                        messageDao.insertMessage(gptMessage)
                    }
                } catch (e: Exception) {
                    val errorMessage = Message(text = "Error: ${e.message}", isUser = false)
                    messageDao.insertMessage(errorMessage)
                }
            }
        }
    }
}

class ChatViewModelFactory(private val messageDao: MessageDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(messageDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
