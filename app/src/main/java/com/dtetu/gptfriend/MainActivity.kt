package com.dtetu.gptfriend

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.dtetu.gptfriend.data.datasource.ChatGptDataSourceImpl
import com.dtetu.gptfriend.data.datasource.LocalDataSourceImpl
import com.dtetu.gptfriend.data.repository.ChatRepositoryImpl
import com.dtetu.gptfriend.data.repository.MessageRepositoryImpl
import com.dtetu.gptfriend.notification.NotificationScheduler
import com.dtetu.gptfriend.ui.theme.GPTFriendTheme
import com.google.firebase.messaging.FirebaseMessaging

// import com.google.firebase.messaging.FirebaseMessaging // Disabled until Firebase is configured

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "gpt-friend.db"
        ).allowMainThreadQueries().build()
    }

    private val viewModel: ChatViewModel by viewModels {
        // Create data sources
        val localDataSource = LocalDataSourceImpl(db.messageDao())
        val chatGptDataSource = ChatGptDataSourceImpl()
        
        // Create repositories
        val messageRepository = MessageRepositoryImpl(localDataSource)
        // ChatRepository now needs MessageRepository for local-first architecture
        val chatRepository = ChatRepositoryImpl(chatGptDataSource, messageRepository)
        
        // Create ViewModel factory
        ChatViewModelFactory(messageRepository, chatRepository, applicationContext)
    }

    // Permission launcher for notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule notifications
            NotificationScheduler.scheduleNotifications(this)
            // getFCMToken() // Disabled until Firebase is properly configured
        } else {
            Log.d(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission and schedule notifications
        requestNotificationPermissionAndSchedule()
        
        // Get FCM token - disabled until Firebase is properly configured
        // getFCMToken()
        
        setContent {
            GPTFriendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    ChatScreen(this.viewModel)
                }
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token
            Log.d(TAG, "FCM Token: $token")
            
            // Optionally show token to user (for testing)
            // Toast.makeText(baseContext, "FCM Token: $token", Toast.LENGTH_SHORT).show()
            
            // TODO: Send token to your server or use it for sending notifications
        }
    }

    private fun requestNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    NotificationScheduler.scheduleNotifications(this)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Below Android 13, no runtime permission needed
            NotificationScheduler.scheduleNotifications(this)
        }
    }
}
