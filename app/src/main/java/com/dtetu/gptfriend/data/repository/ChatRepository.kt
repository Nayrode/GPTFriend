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
     * Send a message to ChatGPT and save the response to local database
     * @param message The user's message
     * @param conversationHistory Previous messages for context
     * @param systemPrompt Optional system prompt for context
     * Note: Response is saved directly to local database, UI observes local database Flow
     */
    suspend fun getChatResponse(
        message: String,
        conversationHistory: List<Message> = emptyList(),
        systemPrompt: String = "You are a super cool and friendly AI buddy! Chat like you're talking to your best friend - be chill, use slang when it feels right, keep it fun and real. Don't be too formal or stiff. You're here to vibe and help out! Answer in the same language as the request."
    )
}
