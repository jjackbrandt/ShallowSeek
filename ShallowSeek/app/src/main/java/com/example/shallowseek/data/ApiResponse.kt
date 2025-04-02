package com.example.shallowseek.data

/**
 * Data class representing the response from the Ollama API.
 * 
 * The response contains the generated model output and metadata about the 
 * request/response including timing information and token counts.
 */
data class ApiResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean,
    val total_duration: Long = 0,
    val load_duration: Long = 0,
    val prompt_eval_count: Int = 0,
    val prompt_eval_duration: Long = 0,
    val eval_count: Int = 0,
    val eval_duration: Long = 0
)