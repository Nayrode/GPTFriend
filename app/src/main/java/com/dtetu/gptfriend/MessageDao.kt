package com.dtetu.gptfriend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM message ORDER BY id ASC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM message ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<Message>

    @Insert
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM message")
    suspend fun deleteAllMessages()
}
