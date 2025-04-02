package com.example.shallowseek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageInput(
    messageContent: String,
    onMessageChange: (String) -> Unit,
    senderName: String,
    onSenderNameChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = senderName,
            onValueChange = onSenderNameChange,
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = messageContent,
                onValueChange = onMessageChange,
                label = { Text("Message") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                enabled = isEnabled
            )
            
            Button(
                onClick = onSendClick,
                enabled = isEnabled && messageContent.isNotEmpty() && senderName.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}