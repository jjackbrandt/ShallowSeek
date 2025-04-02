package com.example.shallowseek.data

/**
 * Data class representing the response from the server's models endpoint.
 * 
 * This class maps to the JSON response from Ollama API which contains a list of available models.
 */
data class ModelsResponse(
    val models: List<ModelInfo> = emptyList()
)

/**
 * Data class representing information about a single model.
 */
data class ModelInfo(
    val name: String,
    val model: String,
    val modified_at: String,
    val size: Long,
    val digest: String,
    val details: ModelDetails? = null
)

/**
 * Data class representing detailed model information.
 */
data class ModelDetails(
    val parent_model: String? = null,
    val format: String? = null,
    val family: String? = null,
    val families: List<String>? = null,
    val parameter_size: String? = null,
    val quantization_level: String? = null
)