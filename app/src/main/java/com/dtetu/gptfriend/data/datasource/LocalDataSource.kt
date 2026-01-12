package com.dtetu.gptfriend.data.datasource

import com.dtetu.gptfriend.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface for local data source operations
 */
interface LocalDataSource {
    /**
     * Get all messages from local storage
     */
    fun getAllMessages(): Flow<List<Message>>

    /**
     * Get recent messages from local storage
     * @param limit Maximum number of messages to retrieve
     */
    suspend fun getRecentMessages(limit: Int): List<Message>

    /**
     * Insert a message into local storage
     */
    suspend fun insertMessage(message: Message)

    /**
     * Clear all messages from local storage
     */
    suspend fun clearAllMessages()
}
