package com.example.shallowseek.network

import com.example.shallowseek.data.ApiRequest
import com.example.shallowseek.data.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for API communication with the Node.js server.
 * 
 * This interface defines the endpoints for text generation using the DeepSeek model.
 */
interface ApiService {
    
    /**
     * Sends a text generation request to the server.
     * 
     * @param request The API request containing the prompt and optional system prompt
     * @return A Response containing the API response with the generated text
     */
    @POST("generate")
    suspend fun generateText(@Body request: ApiRequest): Response<ApiResponse>
}