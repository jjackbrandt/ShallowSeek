package com.example.shallowseek.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton class that provides a pre-configured Retrofit instance for API communication.
 * 
 * Features:
 * - Configurable base URL for different environments
 * - Logging interceptor for debugging
 * - Timeout configuration
 * - Gson conversion for JSON serialization/deserialization
 */
object RetrofitClient {
    
    // Default server address (change to your MacBook's IP address on your local network)
    private var BASE_URL = "http://10.0.2.2:3000/"
    
    /**
     * Configure the base URL for the API.
     * 
     * @param serverAddress The server address including protocol and port (e.g., "http://192.168.1.100:3000/")
     */
    fun setServerAddress(serverAddress: String) {
        if (serverAddress.isNotEmpty()) {
            // Ensure URL ends with a slash
            BASE_URL = if (serverAddress.endsWith("/")) serverAddress else "$serverAddress/"
            
            // Reset the API instance to use the new URL
            apiInstance = null
        }
    }
    
    /**
     * Get the current server address.
     * 
     * @return The current server address being used for API calls
     */
    fun getServerAddress(): String = BASE_URL
    
    // OkHttpClient with logging and timeouts
    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)  // LLM responses can take time
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // Retrofit instance with our configuration
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API service instance (cached)
    private var apiInstance: ApiService? = null
    
    /**
     * Get the API service instance.
     * 
     * @return ApiService instance for making API calls
     */
    fun getApiService(): ApiService {
        if (apiInstance == null) {
            apiInstance = retrofit.create(ApiService::class.java)
        }
        return apiInstance!!
    }
}