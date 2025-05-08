package com.example.shallowseek.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manager for app preferences that provides type-safe access to SharedPreferences
 */
class PreferencesManager(context: Context) {
    companion object {
        private const val TAG = "PreferencesManager"
        private const val PREF_FILE = "shallowseek_prefs"
        
        // SSH preferences
        private const val KEY_SERVER_ADDRESS = "server_address"
        private const val KEY_SSH_HOST = "ssh_host"
        private const val KEY_SSH_PORT = "ssh_port"
        private const val KEY_SSH_USERNAME = "ssh_username"
        private const val KEY_SSH_USE_PASSWORD = "ssh_use_password"
        private const val KEY_SSH_PRIVATE_KEY_PATH = "ssh_private_key_path"
        private const val KEY_SSH_LOCAL_PORT = "ssh_local_port"
        private const val KEY_SSH_REMOTE_HOST = "ssh_remote_host"
        private const val KEY_SSH_REMOTE_PORT = "ssh_remote_port"
        
        // GitHub SSH preferences removed
        
        // Singleton instance
        @Volatile
        private var instance: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    
    // Server address
    fun saveServerAddress(address: String) {
        prefs.edit().putString(KEY_SERVER_ADDRESS, address).apply()
    }
    
    fun getServerAddress(): String {
        return prefs.getString(KEY_SERVER_ADDRESS, "http://10.0.2.2:3000/") ?: "http://10.0.2.2:3000/"
    }
    
    // Standard SSH settings
    fun saveSshSettings(
        host: String,
        port: Int,
        username: String,
        usePassword: Boolean,
        privateKeyPath: String,
        localPort: Int,
        remoteHost: String,
        remotePort: Int
    ) {
        Log.d(TAG, "Saving SSH settings for host: $host")
        prefs.edit().apply {
            putString(KEY_SSH_HOST, host)
            putInt(KEY_SSH_PORT, port)
            putString(KEY_SSH_USERNAME, username)
            putBoolean(KEY_SSH_USE_PASSWORD, usePassword)
            putString(KEY_SSH_PRIVATE_KEY_PATH, privateKeyPath)
            putInt(KEY_SSH_LOCAL_PORT, localPort)
            putString(KEY_SSH_REMOTE_HOST, remoteHost)
            putInt(KEY_SSH_REMOTE_PORT, remotePort)
        }.apply()
    }
    
    fun getSshHost(): String = prefs.getString(KEY_SSH_HOST, "") ?: ""
    fun getSshPort(): Int = prefs.getInt(KEY_SSH_PORT, 22)
    fun getSshUsername(): String = prefs.getString(KEY_SSH_USERNAME, "") ?: ""
    fun getSshUsePassword(): Boolean = prefs.getBoolean(KEY_SSH_USE_PASSWORD, true)
    fun getSshPrivateKeyPath(): String = prefs.getString(KEY_SSH_PRIVATE_KEY_PATH, "") ?: ""
    fun getSshLocalPort(): Int = prefs.getInt(KEY_SSH_LOCAL_PORT, 3001)
    fun getSshRemoteHost(): String = prefs.getString(KEY_SSH_REMOTE_HOST, "localhost") ?: "localhost"
    fun getSshRemotePort(): Int = prefs.getInt(KEY_SSH_REMOTE_PORT, 3000)
    
    // GitHub SSH settings removed
    
    // Clear all preferences
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}