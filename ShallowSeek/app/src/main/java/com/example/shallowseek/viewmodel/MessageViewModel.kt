package com.example.shallowseek.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shallowseek.data.Message
import com.example.shallowseek.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    
    private val repository = MessageRepository()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val newMessageContent = mutableStateOf("")
    val senderName = mutableStateOf("")
    
    fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedMessages = repository.getMessages()
            _messages.value = fetchedMessages
            _isLoading.value = false
        }
    }
    
    fun sendMessage() {
        val content = newMessageContent.value.trim()
        val sender = senderName.value.trim()
        
        if (content.isNotEmpty() && sender.isNotEmpty()) {
            viewModelScope.launch {
                _isLoading.value = true
                val newMessage = repository.createMessage(content, sender)
                if (newMessage != null) {
                    _messages.value = _messages.value + newMessage
                    newMessageContent.value = ""
                }
                _isLoading.value = false
            }
        }
    }
    
    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteMessage(id)
            if (success) {
                _messages.value = _messages.value.filter { it.id != id }
            }
            _isLoading.value = false
        }
    }
    
    init {
        loadMessages()
    }
}