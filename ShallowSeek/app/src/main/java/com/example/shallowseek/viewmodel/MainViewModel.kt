package com.example.shallowseek.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shallowseek.data.ApiResponse
import com.example.shallowseek.repository.ApiRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen of the ShallowSeek app.
 * 
 * This ViewModel manages the state of the UI and communicates with the repository
 * to handle API requests and responses.
 */
class MainViewModel : ViewModel() {
    private val repository = ApiRepository()
    
    // UI state
    var promptText by mutableStateOf("")
        private set
    
    var responseText by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var serverAddress by mutableStateOf(repository.getServerAddress())
        private set
    
    /**
     * Update the user's prompt text.
     * 
     * @param text The new prompt text
     */
    fun updatePrompt(text: String) {
        promptText = text
    }
    
    /**
     * Send the current prompt to the API and get a response.
     */
    fun sendPrompt() {
        if (promptText.isBlank()) {
            errorMessage = "Please enter a prompt"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                val result = repository.generateText(promptText)
                
                result.fold(
                    onSuccess = { response ->
                        handleSuccess(response)
                    },
                    onFailure = { error ->
                        handleError(error)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Handle a successful API response.
     * 
     * @param response The API response
     */
    private fun handleSuccess(response: ApiResponse) {
        responseText = response.response
    }
    
    /**
     * Handle an API error.
     * 
     * @param error The error that occurred
     */
    private fun handleError(error: Throwable) {
        errorMessage = "Error: ${error.localizedMessage ?: "Unknown error"}"
    }
    
    /**
     * Update the server address.
     * 
     * @param address The new server address
     */
    fun updateServerAddress(address: String) {
        repository.setServerAddress(address)
        serverAddress = repository.getServerAddress()
    }
    
    /**
     * Clear the current conversation.
     */
    fun clearConversation() {
        responseText = ""
        promptText = ""
        errorMessage = null
    }
}