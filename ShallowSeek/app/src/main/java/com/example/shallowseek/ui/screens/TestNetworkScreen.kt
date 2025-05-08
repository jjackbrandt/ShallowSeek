package com.example.shallowseek.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shallowseek.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * A test screen for verifying network connectivity and security configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestNetworkScreen() {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var testServerAddress by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("No test run yet") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Get current server address from RetrofitClient
    val currentAddress = RetrofitClient.getServerAddress()
    
    // Get device info and SSH status once to avoid race conditions
    val isEmulator = RetrofitClient.getLocalhostEquivalent() == "10.0.2.2"
    val deviceType = if (isEmulator) "Emulator" else "Physical Device"
    
    // Safely capture SSH connection info
    val isConnectedViaSSH = RetrofitClient.isConnectedViaSSH()
    val sshTunnelInfo = RetrofitClient.getSshTunnelInfo()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Network Security Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current configuration display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Current Configuration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Server Address: $currentAddress",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "Device Type: $deviceType",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "SSH Connected: $isConnectedViaSSH",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Safely display SSH tunnel info if available
                if (isConnectedViaSSH && sshTunnelInfo != null) {
                    Text(
                        text = "SSH Tunnel: localhost:${sshTunnelInfo.localPort} -> ${sshTunnelInfo.remoteHost}:${sshTunnelInfo.remotePort}",
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
        
        // Test specific server
        OutlinedTextField(
            value = testServerAddress,
            onValueChange = { testServerAddress = it },
            label = { Text("Server Address (e.g., http://10.249.187.131:3000/)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    testResult = "Testing connection to $testServerAddress..."
                    
                    // Save current server address
                    val originalAddress = RetrofitClient.getServerAddress()
                    
                    // Set new server address for testing
                    RetrofitClient.setServerAddress(testServerAddress)
                    
                    // Test connection
                    RetrofitClient.testConnection { success, message ->
                        if (success) {
                            testResult = "✅ Connection Successful!\n\nDetails: $message"
                        } else {
                            testResult = "❌ Connection Failed!\n\nError: $message\n\nCheck logs for more details."
                        }
                        
                        // Restore original address
                        RetrofitClient.setServerAddress(originalAddress)
                        isLoading = false
                    }
                }
            },
            enabled = testServerAddress.isNotEmpty() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(if (isLoading) "Testing..." else "Test Connection")
        }
        
        // Test default connection
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    testResult = "Testing default connection..."
                    
                    // Test connection with current settings
                    RetrofitClient.testConnection { success, message ->
                        if (success) {
                            testResult = "✅ Default Connection Successful!\n\nDetails: $message"
                        } else {
                            testResult = "❌ Default Connection Failed!\n\nError: $message\n\nCheck logs for more details."
                        }
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Test Default Connection")
        }
        
        // Show all SSH debug logs with copy function
        Button(
            onClick = {
                scope.launch {
                    testResult = "SSH Debug Logs:\n\n" + RetrofitClient.getSshDebugLogs().joinToString("\n")
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Show SSH Debug Logs")
        }
        
        // Copy logs to clipboard
        Button(
            onClick = {
                scope.launch {
                    val logs = RetrofitClient.getSshDebugLogs().joinToString("\n")
                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("SSH Debug Logs", logs)
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy Logs",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Copy SSH Debug Logs to Clipboard")
            }
        }
        
        // Clear SSH debug logs
        Button(
            onClick = {
                scope.launch {
                    RetrofitClient.clearSshDebugLogs()
                    testResult = "SSH Debug Logs cleared"
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Clear Debug Logs")
        }
        
        // Test result display
        if (testResult.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Test Result",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = ClipData.newPlainText("Test Result", testResult)
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(context, "Test result copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Copy Test Result"
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                            .heightIn(max = 300.dp) // Set a maximum height
                    ) {
                        Text(
                            text = testResult,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp)
            )
        }
        
        // Additional device information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Device Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Device Model: ${android.os.Build.MODEL}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "Device Manufacturer: ${android.os.Build.MANUFACTURER}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "Hardware: ${android.os.Build.HARDWARE}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "Product: ${android.os.Build.PRODUCT}",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}