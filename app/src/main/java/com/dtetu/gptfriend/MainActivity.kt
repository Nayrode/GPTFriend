package com.dtetu.gptfriend

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.dtetu.gptfriend.data.datasource.ChatGptDataSourceImpl
import com.dtetu.gptfriend.data.datasource.LocalDataSourceImpl
import com.dtetu.gptfriend.data.repository.ChatRepositoryImpl
import com.dtetu.gptfriend.data.repository.MessageRepositoryImpl
import com.dtetu.gptfriend.ui.theme.GPTFriendTheme

class MainActivity : ComponentActivity() {
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
        ChatViewModelFactory(messageRepository, chatRepository)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GPTFriendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    ChatScreen(this.viewModel)
                }
            }
        }
    }
}
