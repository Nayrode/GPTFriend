package com.dtetu.gptfriend.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dtetu.gptfriend.AppDatabase
import com.dtetu.gptfriend.BuildConfig
import com.dtetu.gptfriend.R
import com.dtetu.gptfriend.data.datasource.ChatGptDataSourceImpl
import com.dtetu.gptfriend.data.datasource.LocalDataSourceImpl
import com.dtetu.gptfriend.data.repository.MessageRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker that generates AI-powered "miss you" messages every minute
 */
class MissYouNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "miss_you_notifications"
        const val CHANNEL_NAME = "Miss You Messages"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Create notification channel
            createNotificationChannel()

            // Generate message using ChatGPT
            val message = generateMissYouMessage()

            // Show notification
            showNotification(message)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Show fallback notification on error
            showNotification("i miss you </3")
            Result.success()
        }
    }

    private suspend fun generateMissYouMessage(): String {
        return try {
            val chatDataSource = ChatGptDataSourceImpl()
            
            // Initialize with API key
            val apiKey = BuildConfig.OPENAI_API_KEY
            val initResult = chatDataSource.initialize(apiKey)
            
            if (initResult.isFailure) {
                return "i miss you </3"
            }

            // Get conversation language from recent messages
            val language = detectConversationLanguage(chatDataSource)

            // Custom system prompt for generating "miss you" messages
            val systemPrompt = """
                You are a loving, affectionate friend who misses someone deeply. 
                Generate a short, sweet, heartfelt message (10-20 words max) expressing how much you miss them.
                Be creative, romantic, cute, and genuine. Use emojis occasionally.
                Vary the messages - sometimes playful, sometimes deep, sometimes cute.
                Examples style: "thinking about you rn </3", "miss your smile 🥺", "wish you were here with me", "can't stop thinking about you ❤️"
                Use the following language: $language
            """.trimIndent()

            // Request message from ChatGPT
            val result = chatDataSource.sendMessage(
                userMessage = "Generate a short miss you message",
                conversationHistory = emptyList(),
                systemPrompt = systemPrompt
            )

            result.getOrNull()?.take(100) ?: "i miss you </3"
        } catch (e: Exception) {
            e.printStackTrace()
            "i miss you </3"
        }
    }

    private suspend fun detectConversationLanguage(chatDataSource: ChatGptDataSourceImpl): String {
        return try {
            // Get database instance
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "gpt-friend.db"
            ).build()

            // Create repository to access messages
            val localDataSource = LocalDataSourceImpl(db.messageDao())
            val messageRepository = MessageRepositoryImpl(localDataSource)

            // Get recent messages (last 10 for language detection)
            val recentMessages = messageRepository.getRecentMessages(10)
            
            if (recentMessages.isEmpty()) {
                return "English" // Default to English if no conversation history
            }

            // Build conversation text
            val conversationText = recentMessages.joinToString("\n") { message ->
                "${if (message.isUser) "User" else "Assistant"}: ${message.text}"
            }

            // Ask ChatGPT to detect the language
            val languageDetectionPrompt = """
                Deduce the language of this conversation: 
                
                $conversationText
                
                Use only one word.
            """.trimIndent()

            val result = chatDataSource.sendMessage(
                userMessage = languageDetectionPrompt,
                conversationHistory = emptyList(),
                systemPrompt = "You are a language detection expert. Answer with only the name of the language in one word."
            )

            result.getOrNull()?.trim() ?: "English"
        } catch (e: Exception) {
            e.printStackTrace()
            "English" // Default to English on error
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic messages from your GPT friend"
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("GPTFriend")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
