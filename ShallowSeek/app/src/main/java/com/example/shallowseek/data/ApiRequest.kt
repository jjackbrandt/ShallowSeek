package com.example.shallowseek.data

/**
 * Data class representing a request to the Ollama API.
 * 
 * @property prompt The user input prompt to send to the model
 * @property system Optional system prompt to provide context/instructions to the model
 */
data class ApiRequest(
    val prompt: String,
    val system: String? = null
)