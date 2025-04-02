package com.example.shallowseek.repository

import com.example.shallowseek.data.ApiRequest
import com.example.shallowseek.data.ApiResponse
import com.example.shallowseek.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class for handling API communication.
 * 
 * This class abstracts the API calls and provides a clean interface for the ViewModel.
 * It encapsulates the network logic and error handling.
 */
class ApiRepository {
    private val apiService = RetrofitClient.getApiService()
    
    /**
     * Send a prompt to the API and get the generated response.
     * 
     * @param prompt The user's input prompt
     * @param systemPrompt Optional system prompt for context
     * @return Result containing either the API response or an error
     */
    suspend fun generateText(prompt: String, systemPrompt: String? = null): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ApiRequest(prompt, systemPrompt)
                val response = apiService.generateText(request)
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("API error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Update the server address used for API calls.
     * 
     * @param serverAddress The new server address (e.g., "http://192.168.1.100:3000")
     */
    fun setServerAddress(serverAddress: String) {
        RetrofitClient.setServerAddress(serverAddress)
    }
    
    /**
     * Get the current server address.
     * 
     * @return The current server address being used for API calls
     */
    fun getServerAddress(): String {
        return RetrofitClient.getServerAddress()
    }
}