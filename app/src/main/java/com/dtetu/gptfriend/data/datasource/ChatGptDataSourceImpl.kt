package com.dtetu.gptfriend.data.datasource

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.dtetu.gptfriend.Message
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of ChatGptDataSource using OpenAI client
 */
class ChatGptDataSourceImpl : ChatGptDataSource {

    private var openAI: OpenAI? = null

    override suspend fun initialize(apiKey: String): Result<Unit> {
        return try {
            if (apiKey.isBlank() || apiKey == "null" || apiKey == "YOUR_API_KEY") {
                Result.failure(IllegalArgumentException("API key not configured properly"))
            } else {
                val config = OpenAIConfig(
                    token = apiKey,
                    timeout = Timeout(socket = 60.seconds),
                )
                openAI = OpenAI(config)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            openAI = null
            Result.failure(e)
        }
    }

    override fun isInitialized(): Boolean {
        return openAI != null
    }

    override suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<Message>,
        systemPrompt: String
    ): Result<String> {
        val client = openAI ?: return Result.failure(
            IllegalStateException("OpenAI client is not initialized")
        )

        return try {
            // Build message list: system prompt + conversation history + current message
            val chatMessages = buildList {
                add(ChatMessage(role = ChatRole.System, content = systemPrompt))
                
                // Add conversation history (limited to last 20 messages to manage tokens)
                conversationHistory.takeLast(20).forEach { msg ->
                    add(ChatMessage(
                        role = if (msg.isUser) ChatRole.User else ChatRole.Assistant,
                        content = msg.text
                    ))
                }
                
                // Add current user message
                add(ChatMessage(role = ChatRole.User, content = userMessage))
            }
            
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = chatMessages
            )

            val completion = client.chatCompletion(chatCompletionRequest)
            val response = completion.choices.first().message?.content
                ?: return Result.failure(IllegalStateException("No response from ChatGPT"))

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
