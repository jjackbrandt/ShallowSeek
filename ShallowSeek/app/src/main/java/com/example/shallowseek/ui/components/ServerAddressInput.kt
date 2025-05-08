package com.example.shallowseek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.shallowseek.network.RetrofitClient
import com.example.shallowseek.util.PreferencesManager

/**
 * A composable that displays server configuration options including SSH tunneling.
 * 
 * @param currentAddress The current server address
 * @param onAddressChange Callback for when the address is updated
 * @param modifier Modifier for styling
 */
@Composable
fun ServerAddressInput(
    currentAddress: String,
    onAddressChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefManager = remember { PreferencesManager.getInstance(context) }
    
    var address by remember { mutableStateOf(currentAddress) }
    var isEditMode by remember { mutableStateOf(false) }
    var showSshOptions by remember { mutableStateOf(false) }
    var expandSshSection by remember { mutableStateOf(false) }
    var isConnectedViaSSH by remember { mutableStateOf(RetrofitClient.isConnectedViaSSH()) }
    
    // Initialize with saved server address
    LaunchedEffect(Unit) {
        address = prefManager.getServerAddress()
        onAddressChange(address)
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Server Configuration",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isEditMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Server address") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (address.isNotBlank()) {
                            // Save to preferences
                            prefManager.saveServerAddress(address)
                            onAddressChange(address)
                            isEditMode = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Server: $currentAddress",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Button(
                    onClick = { isEditMode = true }
                ) {
                    Text("Edit")
                }
                
                if (isConnectedViaSSH) {
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Add a test button
                    Button(
                        onClick = {
                            RetrofitClient.addSshDebugLog("Manual connection test requested")
                            
                            // Get current device type and ensure correct localhost is used
                            val currentAddress = RetrofitClient.getServerAddress()
                            RetrofitClient.addSshDebugLog("Current server address: $currentAddress")
                            
                            // Fix if wrong host is being used
                            if (isConnectedViaSSH) {
                                val localPortInt = prefManager.getSshLocalPort()
                                val localhost = RetrofitClient.getLocalhostEquivalent()
                                
                                // Only update if needed
                                if (!currentAddress.contains(localhost)) {
                                    val correctedAddress = "http://$localhost:$localPortInt/"
                                    RetrofitClient.addSshDebugLog("Fixing address to use $localhost: $correctedAddress")
                                    RetrofitClient.setServerAddress(correctedAddress)
                                    prefManager.saveServerAddress(correctedAddress)
                                    onAddressChange(correctedAddress)
                                }
                            }
                            
                            RetrofitClient.testConnection { success, message ->
                                RetrofitClient.addSshDebugLog("Connection test: $success - $message")
                            }
                        }
                    ) {
                        Text("Test")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // SSH Section Header with toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "SSH Tunnel",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            // SSH Status indicator
            if (isConnectedViaSSH) {
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        // Before disconnecting, temporarily switch to the SSH server's address
                        // to ensure the disconnect command goes to the right place
                        val currentHost = prefManager.getSshHost()
                        val tempServerAddress = "http://$currentHost:3000/"
                        
                        RetrofitClient.addSshDebugLog("Temporarily setting server address to $tempServerAddress for SSH disconnection")
                        RetrofitClient.setServerAddress(tempServerAddress)
                        
                        RetrofitClient.disconnectFromSsh { success, message ->
                            // Regardless of success/failure, we need to reset the address
                            val localhost = RetrofitClient.getLocalhostEquivalent()
                            val defaultAddress = "http://$localhost:3000/"
                            RetrofitClient.setServerAddress(defaultAddress)
                            prefManager.saveServerAddress(defaultAddress)
                            onAddressChange(defaultAddress)
                            
                            if (success) {
                                isConnectedViaSSH = false
                                RetrofitClient.addSshDebugLog("SSH disconnected successfully")
                            } else {
                                RetrofitClient.addSshDebugLog("SSH disconnect failed, but we've reset the server address anyway")
                                // Force reset the SSH status
                                isConnectedViaSSH = false
                            }
                        }
                    }
                ) {
                    Text("Disconnect")
                }
            } else {
                IconButton(onClick = { expandSshSection = !expandSshSection }) {
                    Icon(
                        imageVector = if (expandSshSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandSshSection) "Collapse" else "Expand"
                    )
                }
            }
        }
        
        // SSH Configuration Section
        if (expandSshSection && !isConnectedViaSSH) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Standard SSH
                    TextButton(
                        onClick = { showSshOptions = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect via SSH")
                    }
                }
            }
        }
        
        // Standard SSH Dialog
        if (showSshOptions) {
            SshConnectDialog(
                onDismiss = { showSshOptions = false },
                onConnect = { success ->
                    showSshOptions = false
                    if (success) {
                        isConnectedViaSSH = true
                        expandSshSection = false
                    }
                }
            )
        }
    }
}

/**
 * Dialog for standard SSH connection configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshConnectDialog(
    onDismiss: () -> Unit,
    onConnect: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefManager = remember { PreferencesManager.getInstance(context) }
    
    var host by remember { mutableStateOf(prefManager.getSshHost()) }
    var port by remember { mutableStateOf(prefManager.getSshPort().toString()) }
    var username by remember { mutableStateOf(prefManager.getSshUsername()) }
    var password by remember { mutableStateOf("") } // We don't save passwords for security
    var usePassword by remember { mutableStateOf(prefManager.getSshUsePassword()) }
    var privateKey by remember { mutableStateOf(prefManager.getSshPrivateKeyPath()) }
    var passphrase by remember { mutableStateOf("") } // We don't save passphrases for security
    var localPort by remember { mutableStateOf(prefManager.getSshLocalPort().toString()) }
    // Default to port 3001 to avoid conflict with the Node.js server which runs on 3000
    LaunchedEffect(Unit) {
        if (localPort == "3000") {
            localPort = "3001"
        }
    }
    var remoteHost by remember { mutableStateOf(prefManager.getSshRemoteHost()) }
    var remotePort by remember { mutableStateOf(prefManager.getSshRemotePort().toString()) }
    var connecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDebugLogs by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SSH Connection",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Server details
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("SSH Host") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("SSH Port") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Authentication toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use Password")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = usePassword,
                        onCheckedChange = { usePassword = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Authentication fields based on method
                if (usePassword) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                } else {
                    OutlinedTextField(
                        value = privateKey,
                        onValueChange = { privateKey = it },
                        label = { Text("Private Key Path") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text("Passphrase (if needed)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Port forwarding
                Text(
                    text = "Port Forwarding",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = localPort,
                    onValueChange = { localPort = it },
                    label = { Text("Local Port") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = remoteHost,
                    onValueChange = { remoteHost = it },
                    label = { Text("Remote Host") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = remotePort,
                    onValueChange = { remotePort = it },
                    label = { Text("Remote Port") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Error message
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Advanced section
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Advanced",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Button(
                        onClick = { showDebugLogs = true },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text("Debug Logs")
                    }
                }
                
                if (showDebugLogs) {
                    SshDebugDialog(onDismiss = { showDebugLogs = false })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            try {
                                connecting = true
                                errorMessage = ""
                                
                                // Validate inputs
                                if (host.isBlank()) {
                                    errorMessage = "SSH Host cannot be empty"
                                    connecting = false
                                    return@Button
                                }
                                
                                if (username.isBlank()) {
                                    errorMessage = "Username cannot be empty"
                                    connecting = false
                                    return@Button
                                }
                                
                                if (usePassword && password.isBlank()) {
                                    errorMessage = "Password cannot be empty"
                                    connecting = false
                                    return@Button
                                }
                                
                                if (!usePassword && privateKey.isBlank()) {
                                    errorMessage = "Private key path cannot be empty"
                                    connecting = false
                                    return@Button
                                }
                                
                                // Parse numeric values safely
                                val portInt = port.toIntOrNull() 
                                if (portInt == null || portInt <= 0 || portInt > 65535) {
                                    errorMessage = "Invalid port number (must be 1-65535)"
                                    connecting = false
                                    return@Button
                                }
                                
                                val localPortInt = localPort.toIntOrNull()
                                if (localPortInt == null || localPortInt <= 0 || localPortInt > 65535) {
                                    errorMessage = "Invalid local port number (must be 1-65535)"
                                    connecting = false
                                    return@Button
                                }
                                
                                val remotePortInt = remotePort.toIntOrNull() 
                                if (remotePortInt == null || remotePortInt <= 0 || remotePortInt > 65535) {
                                    errorMessage = "Invalid remote port number (must be 1-65535)"
                                    connecting = false
                                    return@Button
                                }
                                
                                if (remoteHost.isBlank()) {
                                    errorMessage = "Remote host cannot be empty"
                                    connecting = false
                                    return@Button
                                }
                                
                                // Save settings to preferences
                                prefManager.saveSshSettings(
                                    host = host,
                                    port = portInt,
                                    username = username,
                                    usePassword = usePassword,
                                    privateKeyPath = privateKey,
                                    localPort = localPortInt,
                                    remoteHost = remoteHost,
                                    remotePort = remotePortInt
                                )
                                
                                // Show connection attempt message
                                errorMessage = "Attempting to connect to SSH server at $host:$portInt..."
                                RetrofitClient.addSshDebugLog("User initiated SSH connection: $host:$portInt -> $remoteHost:$remotePortInt with port forwarding on local port $localPortInt")
                                
                                // First set direct server address (temporary, just for initial connection)
                                // Connect directly to the SSH server's API port (3000, not the tunnel port)
                                val directServerAddress = "http://$host:3000/"
                                RetrofitClient.addSshDebugLog("Setting temporary direct server address: $directServerAddress for SSH API connection")
                                RetrofitClient.setServerAddress(directServerAddress)
                                
                                // Try to connect
                                RetrofitClient.connectToSsh(
                                    host = host,
                                    port = portInt,
                                    username = username,
                                    password = if (usePassword) password else null,
                                    privateKey = if (!usePassword) privateKey else null,
                                    passphrase = if (!usePassword) passphrase else null,
                                    localPort = localPortInt,
                                    remoteHost = remoteHost,
                                    remotePort = remotePortInt
                                ) { success, message ->
                                    connecting = false
                                    if (success) {
                                        // Set correct host based on device type
                                        val localhost = RetrofitClient.getLocalhostEquivalent()
                                        val newServerAddress = "http://$localhost:$localPortInt/"
                                        RetrofitClient.setServerAddress(newServerAddress)
                                        prefManager.saveServerAddress(newServerAddress)
                                        
                                        // Test the connection through SSH tunnel
                                        RetrofitClient.addSshDebugLog("Testing SSH tunnel connection to $newServerAddress")
                                        try {
                                            // This will test if the tunnel is working by making a request
                                            RetrofitClient.testConnection { testSuccess, testMessage ->
                                                if (testSuccess) {
                                                    RetrofitClient.addSshDebugLog("✅ SSH tunnel connection test successful")
                                                } else {
                                                    RetrofitClient.addSshDebugLog("❌ SSH tunnel connection test failed: $testMessage")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            RetrofitClient.addSshDebugLog("Error testing connection: ${e.message}")
                                        }
                                        
                                        onConnect(true)
                                    } else {
                                        // Improve error messages for SSH authentication issues
                                        if (message.contains("authentication") || message.contains("auth failed")) {
                                            errorMessage = "SSH authentication failed. Please check your username and password."
                                            RetrofitClient.addSshDebugLog("Authentication error details: $message")
                                        } else {
                                            errorMessage = message
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                connecting = false
                                errorMessage = "Error: ${e.message}"
                                RetrofitClient.addSshDebugLog("Exception in SSH connect button: ${e.message}")
                            }
                        },
                        enabled = !connecting && host.isNotEmpty() && username.isNotEmpty() && 
                                (usePassword && password.isNotEmpty() || !usePassword && privateKey.isNotEmpty()),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for SSH debug logs.
 */
@Composable
fun SshDebugDialog(
    onDismiss: () -> Unit
) {
    val logs = RetrofitClient.getSshDebugLogs()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SSH Debug Logs",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (logs.isEmpty()) {
                    Text(
                        text = "No SSH debug logs available.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        logs.forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// GitHub SSH Dialog removed