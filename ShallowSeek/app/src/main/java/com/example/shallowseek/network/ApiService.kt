package com.example.shallowseek.network

import com.example.shallowseek.data.ApiRequest
import com.example.shallowseek.data.ApiResponse
import com.example.shallowseek.data.ModelsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit interface for API communication with the Node.js server.
 * 
 * This interface defines the endpoints for text generation using the Ollama models.
 */
interface ApiService {
    
    /**
     * Sends a text generation request to the server.
     * 
     * @param request The API request containing the prompt, model, and optional system prompt
     * @return A Response containing the API response with the generated text
     */
    @POST("generate")
    suspend fun generateText(@Body request: ApiRequest): Response<ApiResponse>
    
    /**
     * Retrieves the list of available models from Ollama.
     * 
     * @return A Response containing the models response with available models
     */
    @GET("models")
    suspend fun getModels(): Response<ModelsResponse>
}