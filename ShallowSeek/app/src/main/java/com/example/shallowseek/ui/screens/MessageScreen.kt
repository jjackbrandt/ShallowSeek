package com.example.shallowseek.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shallowseek.ui.components.MessageInput
import com.example.shallowseek.ui.components.MessageItem
import com.example.shallowseek.viewmodel.MessageViewModel

@Composable
fun MessageScreen(
    viewModel: MessageViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShallowSeek") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        if (messages.isEmpty() && !isLoading) {
                            item {
                                Text(
                                    text = "No messages yet. Be the first to send one!",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 24.dp)
                                )
                            }
                        }
                        
                        items(messages.sortedByDescending { it.timestamp }) { message ->
                            MessageItem(
                                message = message,
                                onDelete = { viewModel.deleteMessage(message.id ?: return@MessageItem) }
                            )
                        }
                    }
                    
                    Divider()
                    
                    MessageInput(
                        messageContent = viewModel.newMessageContent.value,
                        onMessageChange = { viewModel.newMessageContent.value = it },
                        senderName = viewModel.senderName.value,
                        onSenderNameChange = { viewModel.senderName.value = it },
                        onSendClick = { viewModel.sendMessage() },
                        isEnabled = !isLoading
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    )
}