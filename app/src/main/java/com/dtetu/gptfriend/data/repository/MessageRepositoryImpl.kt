package com.dtetu.gptfriend.data.repository

import com.dtetu.gptfriend.Message
import com.dtetu.gptfriend.data.datasource.LocalDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of MessageRepository
 * Handles all local message storage operations
 */
class MessageRepositoryImpl(
    private val localDataSource: LocalDataSource
) : MessageRepository {

    override fun getAllMessages(): Flow<List<Message>> {
        return localDataSource.getAllMessages()
    }

    override suspend fun getRecentMessages(limit: Int): List<Message> {
        return localDataSource.getRecentMessages(limit)
    }

    override suspend fun saveMessage(message: Message) {
        localDataSource.insertMessage(message)
    }

    override suspend fun saveMessage(text: String, isUser: Boolean) {
        val message = Message(text = text, isUser = isUser)
        localDataSource.insertMessage(message)
    }

    override suspend fun clearAllMessages() {
        localDataSource.clearAllMessages()
    }
}
