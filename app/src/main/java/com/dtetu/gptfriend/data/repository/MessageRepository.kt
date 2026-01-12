package com.dtetu.gptfriend.data.repository

import com.dtetu.gptfriend.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for message operations
 */
interface MessageRepository {
    /**
     * Get all messages as a Flow
     */
    fun getAllMessages(): Flow<List<Message>>

    /**
     * Get recent messages for context
     * @param limit Maximum number of messages to retrieve
     */
    suspend fun getRecentMessages(limit: Int): List<Message>

    /**
     * Save a message to local storage
     */
    suspend fun saveMessage(message: Message)

    /**
     * Save a message with text and user flag
     */
    suspend fun saveMessage(text: String, isUser: Boolean)

    /**
     * Clear all messages
     */
    suspend fun clearAllMessages()
}
