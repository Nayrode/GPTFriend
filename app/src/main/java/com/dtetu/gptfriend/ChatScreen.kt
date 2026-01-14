package com.dtetu.gptfriend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isDarkTheme = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    // Automatically scroll to the bottom when a new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.size - 1)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF2C2C2C) 
                    else MaterialTheme.colorScheme.primaryContainer
                )
        ) {
            Text(
                text = "GPTFriend",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) androidx.compose.ui.graphics.Color.White 
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = { viewModel.clearConversation() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear conversation",
                    tint = if (isDarkTheme) androidx.compose.ui.graphics.Color.White 
                        else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    focusManager.clearFocus()
                }
        ) {
            if (messages.isEmpty()) {
                // Show sleeping robot when no messages
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    // Robot image centered
                    androidx.compose.foundation.Image(
                        painter = painterResource(
                            id = if (isDarkTheme) R.drawable.sleeping_robot_dark else R.drawable.sleeping_robot
                        ),
                        contentDescription = "No messages yet",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                            .alpha(0.2f)
                    )
                    // Animated Z's positioned at top right of robot
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 300.dp, y = 600.dp) // Offset to position at top right of robot
                            .size(300.dp) // Fixed size to prevent layout shifts
                    ) {
                        SleepingZAnimation(isDarkTheme = isDarkTheme)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 150.dp),
                label = { Text("Type a message") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
                minLines = 1,
                colors = if (isDarkTheme) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                        focusedLabelColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedLabelColor = androidx.compose.ui.graphics.Color.White,
                        cursorColor = androidx.compose.ui.graphics.Color.White
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                }
            )
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText, true)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = if (isDarkTheme) {
                    ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.White,
                        contentColor = androidx.compose.ui.graphics.Color.Black
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isDarkTheme = isSystemInDarkTheme()
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val maxCardWidth = maxWidth * 0.75f
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = if (message.isUser) "Moi" else "GPTFriend",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) {
                        if (isDarkTheme) androidx.compose.ui.graphics.Color.White
                        else MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .widthIn(max = maxCardWidth)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isUser && isDarkTheme) {
                        androidx.compose.ui.graphics.Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
@Composable
fun SleepingZAnimation(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sleeping")
    
    // Create independent animations for each Z to avoid flicker
    repeat(3) { index ->
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    delayMillis = index * 667, // Stagger by ~667ms for even spacing
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "z_$index"
        )
        
        // Calculate position and alpha based on progress
        // Parent Box is already positioned at robot's top right via offset
        val offsetX = 250f * progress
        val offsetY = -300f * progress
        // Smooth fade-in from 5-10%, then fade out
        val alpha = when {
            progress < 0.05f -> 0f // Hidden at start
            progress < 0.10f -> 0.2f * ((progress - 0.05f) / 0.05f) // Fade in
            else -> 0.2f * (1f - progress) // Fade out
        }
        
        // Always render to avoid recomposition flicker, use alpha for visibility
        Text(
            text = "Z",
            fontSize = (20 + 15 * progress).sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black,
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                }
                .alpha(alpha)
        )
    }
}