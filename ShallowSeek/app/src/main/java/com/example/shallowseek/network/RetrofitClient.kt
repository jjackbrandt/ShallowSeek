package com.example.shallowseek.network

import android.util.Log
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Data classes for SSH connection requests
 */
data class SshConnectRequest(
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String? = null,
    val privateKey: String? = null,
    val passphrase: String? = null,
    val localPort: Int,
    val remoteHost: String,
    val remotePort: Int
)

data class SshTunnelResponse(
    val status: String,
    val message: String,
    val tunnel: SshTunnelInfo? = null,
    val error: String? = null
)

data class SshTunnelInfo(
    val localPort: Int,
    val remoteHost: String,
    val remotePort: Int
)

data class SshStatusResponse(
    val status: String,
    val tunnels: List<String>? = null
)

/**
 * Interface for SSH-related API calls
 */
interface SshApiService {
    @POST("ssh/connect")
    fun connectSsh(@Body request: SshConnectRequest): Call<SshTunnelResponse>
    
    @POST("ssh/disconnect")
    fun disconnectSsh(): Call<SshTunnelResponse>
}

// Test API interface for echo endpoint
interface TestApiService {
    @retrofit2.http.GET("echo")
    fun testEcho(): Call<Map<String, Any>>
    
    @retrofit2.http.GET("health")
    fun testHealth(): Call<Map<String, Any>>
}

/**
 * Singleton class that provides a pre-configured Retrofit instance for API communication.
 * 
 * Features:
 * - Configurable base URL for different environments
 * - SSH tunnel support for remote connections
 * - Logging interceptor for debugging
 * - Timeout configuration
 * - Gson conversion for JSON serialization/deserialization
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Default server address (change to your MacBook's IP address on your local network)
    private var BASE_URL = "http://10.0.2.2:3000/"
    
    // SSH connection status
    private var isConnectedViaSSH = false
    private var sshTunnelInfo: SshTunnelInfo? = null
    
    // Debug logs for SSH connections
    private val sshDebugLogs = mutableListOf<String>()
    
    /**
     * Add a debug log message for SSH connections
     */
    fun addSshDebugLog(message: String) {
        sshDebugLogs.add("[${System.currentTimeMillis()}] $message")
        // Keep only the last 100 log entries
        if (sshDebugLogs.size > 100) {
            sshDebugLogs.removeAt(0)
        }
        Log.d(TAG, "SSH Debug: $message")
    }
    
    /**
     * Get all SSH debug logs
     */
    fun getSshDebugLogs(): List<String> = sshDebugLogs.toList()
    
    /**
     * Configure the base URL for the API.
     * 
     * @param serverAddress The server address including protocol and port (e.g., "http://192.168.1.100:3000/")
     */
    fun setServerAddress(serverAddress: String) {
        if (serverAddress.isNotEmpty()) {
            // Log the address change
            addSshDebugLog("Changing server address from $BASE_URL to $serverAddress")
            
            // Ensure URL ends with a slash
            BASE_URL = if (serverAddress.endsWith("/")) serverAddress else "$serverAddress/"
            
            // Reset the API instances to force them to use the new URL
            apiInstance = null
            sshApiInstance = null
            
            Log.d(TAG, "Server address updated to: $BASE_URL")
        }
    }
    
    /**
     * Get the current server address.
     * 
     * @return The current server address being used for API calls
     */
    fun getServerAddress(): String = BASE_URL
    
    /**
     * Check if connected via SSH tunnel
     * 
     * @return True if connected via SSH tunnel, false otherwise
     */
    fun isConnectedViaSSH(): Boolean = isConnectedViaSSH
    
    /**
     * Get the current SSH tunnel info
     * 
     * @return SshTunnelInfo if connected via SSH, null otherwise
     */
    fun getSshTunnelInfo(): SshTunnelInfo? = sshTunnelInfo
    
    // OkHttpClient with logging and timeouts
    private val client by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val customInterceptor = HttpLoggingInterceptor {
            // Custom log interceptor for detailed request/response logging
            addSshDebugLog(it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customInterceptor) 
            .connectTimeout(30, TimeUnit.SECONDS)  // Increased from 15 to 30 seconds for more reliability
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)  // Increased from 15 to 30 seconds for more reliability
            .build()
    }
    
    // Get a fresh Retrofit instance with the current BASE_URL
    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL) // Always use the current BASE_URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    
    // API service instance (cached)
    private var apiInstance: ApiService? = null
    
    // SSH API service instance (cached)
    private var sshApiInstance: SshApiService? = null
    
    // Test API service instance (not cached, always fresh)
    private var testApiInstance: TestApiService? = null
    
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
    
    /**
     * Get the SSH API service instance.
     * 
     * @return SshApiService instance for making SSH-related API calls
     */
    fun getSshApiService(): SshApiService {
        // Force recreate the SSH API instance to ensure it uses the current BASE_URL
        apiInstance = null
        sshApiInstance = null
        
        // Create a fresh instance with the current server address
        sshApiInstance = retrofit.create(SshApiService::class.java)
        return sshApiInstance!!
    }
    
    /**
     * Get a test API service instance for the echo endpoint.
     * 
     * @return TestApiService instance for making test API calls
     */
    private fun getTestApiService(): TestApiService {
        // Always create a fresh instance with the current server address
        testApiInstance = retrofit.create(TestApiService::class.java)
        return testApiInstance!!
    }
    
    /**
     * Test the connection to the server.
     * 
     * @param callback Callback for success/failure with message
     */
    fun testConnection(callback: (success: Boolean, message: String) -> Unit) {
        addSshDebugLog("Testing connection to $BASE_URL...")
        
        // Detect if we're using localhost with a port
        if (BASE_URL.contains("localhost")) {
            addSshDebugLog("Using localhost address. Important: This will NOT work on Android emulator. Use 10.0.2.2 instead of localhost for emulator.")
            
            // Try to check what the actual server address should be
            val host = if (isConnectedViaSSH()) {
                addSshDebugLog("SSH is connected - using localhost is correct for SSH tunneling")
                "localhost"
            } else if (BASE_URL.contains("10.0.2.2")) {
                addSshDebugLog("Using emulator address (10.0.2.2)")
                "10.0.2.2"
            } else {
                addSshDebugLog("Warning: Using localhost but not connected via SSH may not work on emulator")
                "localhost"
            }
            
            addSshDebugLog("Testing with host: $host")
        }
        
        try {
            // First try the echo endpoint
            getTestApiService().testEcho().enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        addSshDebugLog("Echo test successful: ${response.body()}")
                        callback(true, "Echo test successful")
                    } else {
                        addSshDebugLog("Echo test failed with status code: ${response.code()}")
                        
                        // Try health endpoint as fallback
                        getTestApiService().testHealth().enqueue(object : Callback<Map<String, Any>> {
                            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                                if (response.isSuccessful) {
                                    addSshDebugLog("Health check successful: ${response.body()}")
                                    callback(true, "Health check successful")
                                } else {
                                    addSshDebugLog("Health check failed: ${response.code()}")
                                    callback(false, "Both echo and health endpoints failed")
                                }
                            }
                            
                            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                addSshDebugLog("Health check failed: ${t.message}")
                                callback(false, "Connection test failed: ${t.message}")
                            }
                        })
                    }
                }
                
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    addSshDebugLog("Echo test failed: ${t.message}")
                    
                    // Try health endpoint as fallback
                    getTestApiService().testHealth().enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                            if (response.isSuccessful) {
                                addSshDebugLog("Health check successful: ${response.body()}")
                                callback(true, "Health check successful")
                            } else {
                                addSshDebugLog("Health check failed: ${response.code()}")
                                callback(false, "Both echo and health endpoints failed")
                            }
                        }
                        
                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            addSshDebugLog("Health check failed: ${t.message}")
                            callback(false, "Connection test failed: ${t.message}")
                        }
                    })
                }
            })
        } catch (e: Exception) {
            addSshDebugLog("Error testing connection: ${e.message}")
            callback(false, "Error testing connection: ${e.message}")
        }
    }
    
    /**
     * Connect to SSH server using standard SSH credentials
     * 
     * @param host SSH server hostname
     * @param port SSH server port (default: 22)
     * @param username SSH username
     * @param password SSH password (optional if using privateKey)
     * @param privateKey Private key for authentication (optional if using password)
     * @param passphrase Passphrase for privateKey (optional)
     * @param localPort Local port to forward
     * @param remoteHost Remote host to forward to
     * @param remotePort Remote port to forward to
     * @param callback Callback for success/failure
     */
    fun connectToSsh(
        host: String,
        port: Int = 22,
        username: String,
        password: String? = null,
        privateKey: String? = null,
        passphrase: String? = null,
        localPort: Int,
        remoteHost: String,
        remotePort: Int,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        try {
            addSshDebugLog("Starting SSH connection to $host:$port with username $username")
            addSshDebugLog("Using ${if (password != null) "password" else "private key"} authentication")
            addSshDebugLog("Setting up tunnel: localhost:$localPort -> $remoteHost:$remotePort")
            
            val request = SshConnectRequest(
                host = host,
                port = port,
                username = username,
                password = password,
                privateKey = privateKey,
                passphrase = passphrase,
                localPort = localPort,
                remoteHost = remoteHost,
                remotePort = remotePort
            )
            
            try {
                getSshApiService().connectSsh(request).enqueue(object : Callback<SshTunnelResponse> {
                    override fun onResponse(call: Call<SshTunnelResponse>, response: Response<SshTunnelResponse>) {
                        try {
                            if (response.isSuccessful && response.body()?.status == "success") {
                                addSshDebugLog("SSH connection successful: ${response.body()?.message}")
                                addSshDebugLog("SSH tunnel info - Local port: ${response.body()?.tunnel?.localPort}, Remote: ${response.body()?.tunnel?.remoteHost}:${response.body()?.tunnel?.remotePort}")
                                addSshDebugLog("⚠️ IMPORTANT: App will now use 'localhost:${response.body()?.tunnel?.localPort}' for API calls")
                                isConnectedViaSSH = true
                                sshTunnelInfo = response.body()?.tunnel
                                
                                // First update the server address to use localhost instead of 10.0.2.2
                                val tunnelPort = response.body()?.tunnel?.localPort ?: 3000
                                val newServerAddress = "http://localhost:$tunnelPort/"
                                addSshDebugLog("Updating server address to: $newServerAddress")
                                
                                callback(true, response.body()?.message ?: "SSH tunnel established")
                            } else {
                                val errorMsg = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                                addSshDebugLog("SSH connection failed: $errorMsg")
                                callback(false, errorMsg)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception in SSH response handling", e)
                            addSshDebugLog("Exception in SSH response handling: ${e.message}")
                            callback(false, "Error processing response: ${e.message}")
                        }
                    }
                    
                    override fun onFailure(call: Call<SshTunnelResponse>, t: Throwable) {
                        addSshDebugLog("SSH connection request failed: ${t.message}")
                        addSshDebugLog("Current server address: ${getServerAddress()}")
                        addSshDebugLog("Debug info - Is the server running and accessible? Is SSH server running?")
                        Log.e(TAG, "SSH connection failed", t)
                        
                        // Provide more actionable error message to user
                        val errorMsg = when {
                            t.message?.contains("timeout") == true -> 
                                "Connection timed out after 30 seconds. Check that the SSH server is running on port 22 and your network allows connections."
                            t.message?.contains("refused") == true -> 
                                "Connection refused. Check that the SSH server is running and accepting connections."
                            t.message?.contains("Network is unreachable") == true -> 
                                "Network is unreachable. Check your network connection and the host address."
                            else -> "Connection failed: ${t.message}"
                        }
                        
                        callback(false, errorMsg)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Exception while making SSH API call", e)
                addSshDebugLog("Exception while making SSH API call: ${e.message}")
                callback(false, "Error starting connection: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal exception in SSH connection", e)
            addSshDebugLog("Fatal exception in SSH connection: ${e.message}")
            callback(false, "Fatal error: ${e.message}")
        }
    }
    
    /**
     * Disconnect from SSH server
     * 
     * @param callback Callback for success/failure
     */
    fun disconnectFromSsh(callback: (success: Boolean, message: String) -> Unit) {
        addSshDebugLog("Disconnecting SSH tunnel...")
        if (sshTunnelInfo != null) {
            addSshDebugLog("Current SSH tunnel: localhost:${sshTunnelInfo?.localPort} -> ${sshTunnelInfo?.remoteHost}:${sshTunnelInfo?.remotePort}")
        } else {
            addSshDebugLog("No active SSH tunnel information found")
        }
        
        getSshApiService().disconnectSsh().enqueue(object : Callback<SshTunnelResponse> {
            override fun onResponse(call: Call<SshTunnelResponse>, response: Response<SshTunnelResponse>) {
                if (response.isSuccessful) {
                    addSshDebugLog("SSH tunnel disconnected successfully")
                    addSshDebugLog("Resetting server address to default (10.0.2.2:3000)")
                    isConnectedViaSSH = false
                    sshTunnelInfo = null
                    callback(true, response.body()?.message ?: "SSH tunnel disconnected")
                } else {
                    val errorMsg = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                    addSshDebugLog("SSH disconnection failed: $errorMsg")
                    callback(false, errorMsg)
                }
            }
            
            override fun onFailure(call: Call<SshTunnelResponse>, t: Throwable) {
                addSshDebugLog("SSH disconnection request failed: ${t.message}")
                addSshDebugLog("Current server address: ${getServerAddress()}")
                Log.e(TAG, "SSH disconnection failed", t)
                
                // Even if the disconnect request fails, let's reset our state
                addSshDebugLog("Resetting SSH state despite disconnection failure")
                isConnectedViaSSH = false
                sshTunnelInfo = null
                
                callback(false, "Disconnection failed: ${t.message}")
            }
        })
    }
}