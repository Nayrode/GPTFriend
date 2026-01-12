package com.dtetu.gptfriend.data.repository

import com.dtetu.gptfriend.Message
import com.dtetu.gptfriend.data.datasource.ChatGptDataSource

/**
 * Implementation of ChatRepository
 * Handles all ChatGPT API operations
 */
class ChatRepositoryImpl(
    private val chatGptDataSource: ChatGptDataSource
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
    ): Result<String> {
        if (!isInitialized()) {
            return Result.failure(
                IllegalStateException("OpenAI client is not initialized. Please check previous error messages and restart the app.")
            )
        }

        return chatGptDataSource.sendMessage(message, conversationHistory, systemPrompt).fold(
            onSuccess = { response ->
                Result.success(response)
            },
            onFailure = { error ->
                Result.failure(Exception("Error: ${error.message}"))
            }
        )
    }
}
