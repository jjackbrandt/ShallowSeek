package com.example.shallowseek.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    val id: Long? = null,
    val content: String,
    val sender: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}