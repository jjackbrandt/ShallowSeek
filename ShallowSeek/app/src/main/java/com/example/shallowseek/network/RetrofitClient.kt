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

data class GithubSshRequest(
    val githubUsername: String,
    val sshKeyPath: String? = null,
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
    
    @POST("ssh/github")
    fun connectGithubSsh(@Body request: GithubSshRequest): Call<SshTunnelResponse>
    
    @POST("ssh/disconnect")
    fun disconnectSsh(): Call<SshTunnelResponse>
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
            sshApiInstance = null
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
    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)  // LLM responses can take time
            .writeTimeout(120, TimeUnit.SECONDS)
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
    
    // SSH API service instance (cached)
    private var sshApiInstance: SshApiService? = null
    
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
        if (sshApiInstance == null) {
            sshApiInstance = retrofit.create(SshApiService::class.java)
        }
        return sshApiInstance!!
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
        
        getSshApiService().connectSsh(request).enqueue(object : Callback<SshTunnelResponse> {
            override fun onResponse(call: Call<SshTunnelResponse>, response: Response<SshTunnelResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    isConnectedViaSSH = true
                    sshTunnelInfo = response.body()?.tunnel
                    callback(true, response.body()?.message ?: "SSH tunnel established")
                } else {
                    val errorMsg = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                    callback(false, errorMsg)
                }
            }
            
            override fun onFailure(call: Call<SshTunnelResponse>, t: Throwable) {
                Log.e(TAG, "SSH connection failed", t)
                callback(false, "Connection failed: ${t.message}")
            }
        })
    }
    
    /**
     * Connect to SSH server using GitHub SSH credentials
     * 
     * @param githubUsername GitHub username
     * @param sshKeyPath Path to SSH key (optional, default: ~/.ssh/id_rsa)
     * @param passphrase Passphrase for SSH key (optional)
     * @param localPort Local port to forward
     * @param remoteHost Remote host to forward to
     * @param remotePort Remote port to forward to
     * @param callback Callback for success/failure
     */
    fun connectToGithubSsh(
        githubUsername: String,
        sshKeyPath: String? = null,
        passphrase: String? = null,
        localPort: Int,
        remoteHost: String,
        remotePort: Int,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val request = GithubSshRequest(
            githubUsername = githubUsername,
            sshKeyPath = sshKeyPath,
            passphrase = passphrase,
            localPort = localPort,
            remoteHost = remoteHost,
            remotePort = remotePort
        )
        
        getSshApiService().connectGithubSsh(request).enqueue(object : Callback<SshTunnelResponse> {
            override fun onResponse(call: Call<SshTunnelResponse>, response: Response<SshTunnelResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    isConnectedViaSSH = true
                    sshTunnelInfo = response.body()?.tunnel
                    callback(true, response.body()?.message ?: "GitHub SSH tunnel established")
                } else {
                    val errorMsg = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                    callback(false, errorMsg)
                }
            }
            
            override fun onFailure(call: Call<SshTunnelResponse>, t: Throwable) {
                Log.e(TAG, "GitHub SSH connection failed", t)
                callback(false, "Connection failed: ${t.message}")
            }
        })
    }
    
    /**
     * Disconnect from SSH server
     * 
     * @param callback Callback for success/failure
     */
    fun disconnectFromSsh(callback: (success: Boolean, message: String) -> Unit) {
        getSshApiService().disconnectSsh().enqueue(object : Callback<SshTunnelResponse> {
            override fun onResponse(call: Call<SshTunnelResponse>, response: Response<SshTunnelResponse>) {
                if (response.isSuccessful) {
                    isConnectedViaSSH = false
                    sshTunnelInfo = null
                    callback(true, response.body()?.message ?: "SSH tunnel disconnected")
                } else {
                    val errorMsg = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                    callback(false, errorMsg)
                }
            }
            
            override fun onFailure(call: Call<SshTunnelResponse>, t: Throwable) {
                Log.e(TAG, "SSH disconnection failed", t)
                callback(false, "Disconnection failed: ${t.message}")
            }
        })
    }
}