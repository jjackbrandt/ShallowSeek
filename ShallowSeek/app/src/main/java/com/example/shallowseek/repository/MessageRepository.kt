package com.example.shallowseek.repository

import com.example.shallowseek.api.RetrofitClient
import com.example.shallowseek.data.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageRepository {
    
    private val messageService = RetrofitClient.messageService
    
    suspend fun getMessages(): List<Message> = withContext(Dispatchers.IO) {
        try {
            messageService.getMessages()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun createMessage(content: String, sender: String): Message? = withContext(Dispatchers.IO) {
        try {
            val message = Message(content = content, sender = sender)
            messageService.createMessage(message)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun deleteMessage(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            messageService.deleteMessage(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}