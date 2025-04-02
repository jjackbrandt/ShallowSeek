package com.example.shallowseekserver.model

data class Message(
    val id: Long? = null,
    val content: String,
    val sender: String,
    val timestamp: Long = System.currentTimeMillis()
)