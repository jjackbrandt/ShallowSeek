package com.example.shallowseek.data

/**
 * Data class representing a request to the Ollama API.
 * 
 * @property prompt The user input prompt to send to the model
 * @property system Optional system prompt to provide context/instructions to the model
 * @property model The name of the model to use for generation (e.g., "deepseek-r1:1.5b")
 */
data class ApiRequest(
    val prompt: String,
    val system: String? = null,
    val model: String? = null
)