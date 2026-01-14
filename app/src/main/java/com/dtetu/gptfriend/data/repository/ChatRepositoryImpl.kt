package com.dtetu.gptfriend.data.repository

import com.dtetu.gptfriend.Message
import com.dtetu.gptfriend.data.datasource.ChatGptDataSource

/**
 * Implementation of ChatRepository
 * Handles all ChatGPT API operations with local-first principle
 * API responses are saved directly to local database
 */
class ChatRepositoryImpl(
    private val chatGptDataSource: ChatGptDataSource,
    private val messageRepository: MessageRepository
) : ChatRepository {

    override suspend fun initialize(apiKey: String): Result<Unit> {
        return chatGptDataSource.initialize(apiKey).fold(
            onSuccess = {
                Result.success(Unit)
            },
            onFailure = { error ->
                val message = when {
                    error is IllegalArgumentException -> "API key not configured. Please add OPENAI_API_KEY to your ~/.gradle/gradle.properties file and restart."
                    else -> "FATAL on init: ${error.javaClass.simpleName}: ${error.message}. Check Logcat for stacktrace."
                }
                Result.failure(Exception(message))
            }
        )
    }

    override fun isInitialized(): Boolean {
        return chatGptDataSource.isInitialized()
    }

    override suspend fun getChatResponse(
        message: String,
        conversationHistory: List<Message>,
        systemPrompt: String
    ) {
        if (!isInitialized()) {
            // Save error message to local database
            messageRepository.saveMessage(
                "OpenAI client is not initialized. Please check previous error messages and restart the app.",
                isUser = false
            )
            return
        }

        // Call API and save response to local database
        chatGptDataSource.sendMessage(message, conversationHistory, systemPrompt).fold(
            onSuccess = { response ->
                // Save API response to local database
                messageRepository.saveMessage(response, isUser = false)
            },
            onFailure = { error ->
                // Save error message to local database
                messageRepository.saveMessage(
                    "Error: ${error.message}",
                    isUser = false
                )
            }
        )
    }
}
