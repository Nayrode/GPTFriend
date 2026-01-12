package com.dtetu.gptfriend.data.datasource

import com.dtetu.gptfriend.Message
import com.dtetu.gptfriend.MessageDao
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of LocalDataSource using Room database
 */
class LocalDataSourceImpl(
    private val messageDao: MessageDao
) : LocalDataSource {

    override fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages()
    }

    override suspend fun getRecentMessages(limit: Int): List<Message> {
        return messageDao.getRecentMessages(limit).reversed() // Reverse to get chronological order
    }

    override suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
    }

    override suspend fun clearAllMessages() {
        messageDao.deleteAllMessages()
    }
}
