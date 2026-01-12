package com.dtetu.gptfriend.data.datasource

import com.dtetu.gptfriend.Message

/**
 * Interface for ChatGPT API data source operations
 */
interface ChatGptDataSource {
    /**
     * Initialize the OpenAI client with the API key
     */
    suspend fun initialize(apiKey: String): Result<Unit>

    /**
     * Check if the data source is initialized
     */
    fun isInitialized(): Boolean

    /**
     * Send a message to ChatGPT and get a response
     * @param userMessage The user's message
     * @param conversationHistory Previous messages for context (limited to recent messages)
     * @param systemPrompt Optional system prompt for context
     * @return Result containing the response or error
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<Message> = emptyList(),
        systemPrompt: String = "You are a super cool and friendly AI buddy! Chat like you're talking to your best friend - be chill, use slang when it feels right, keep it fun and real. Don't be too formal or stiff. You're here to vibe and help out! Answer in the same language as the request."
    ): Result<String>
}
