package com.dtetu.gptfriend.data.repository

import com.dtetu.gptfriend.Message

/**
 * Repository interface for ChatGPT operations
 */
interface ChatRepository {
    /**
     * Initialize the ChatGPT client
     * @return Result indicating success or failure
     */
    suspend fun initialize(apiKey: String): Result<Unit>

    /**
     * Check if the chat service is ready
     */
    fun isInitialized(): Boolean

    /**
     * Send a message to ChatGPT and get a response
     * @param message The user's message
     * @param conversationHistory Previous messages for context
     * @param systemPrompt Optional system prompt for context
     * @return Result containing the response or error message
     */
    suspend fun getChatResponse(
        message: String,
        conversationHistory: List<Message> = emptyList(),
        systemPrompt: String = "You are a super cool and friendly AI buddy! Chat like you're talking to your best friend - be chill, use slang when it feels right, keep it fun and real. Don't be too formal or stiff. You're here to vibe and help out! Answer in the same language as the request."
    ): Result<String>
}
