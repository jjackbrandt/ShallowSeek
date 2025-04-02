package com.example.shallowseek.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shallowseek.data.ApiResponse
import com.example.shallowseek.data.ModelInfo
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
    
    // Model selection state
    var availableModels by mutableStateOf<List<ModelInfo>>(emptyList())
        private set
        
    var selectedModel by mutableStateOf<String?>(null)
        private set
        
    var isLoadingModels by mutableStateOf(false)
        private set
    
    init {
        // Load models on initialization
        loadModels()
    }
    
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
                val result = repository.generateText(promptText, model = selectedModel)
                
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
     * Load available models from the server.
     */
    fun loadModels() {
        isLoadingModels = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                val result = repository.getModels()
                
                result.fold(
                    onSuccess = { response ->
                        availableModels = response.models
                        
                        // If no model is selected yet, select the first one
                        if (selectedModel == null && availableModels.isNotEmpty()) {
                            selectedModel = availableModels.first().name
                        }
                    },
                    onFailure = { error ->
                        handleError(error)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isLoadingModels = false
            }
        }
    }
    
    /**
     * Update the selected model.
     * 
     * @param modelName The name of the selected model
     */
    fun selectModel(modelName: String) {
        selectedModel = modelName
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
        
        // Reload models when the server address changes
        loadModels()
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