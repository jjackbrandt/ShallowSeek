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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.shallowseek.network.RetrofitClient

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
    var address by remember { mutableStateOf(currentAddress) }
    var isEditMode by remember { mutableStateOf(false) }
    var showSshOptions by remember { mutableStateOf(false) }
    var expandSshSection by remember { mutableStateOf(false) }
    var isConnectedViaSSH by remember { mutableStateOf(RetrofitClient.isConnectedViaSSH()) }
    
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
                        RetrofitClient.disconnectFromSsh { success, message ->
                            if (success) {
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
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // GitHub SSH
                    var showGithubSshOptions by remember { mutableStateOf(false) }
                    
                    TextButton(
                        onClick = { showGithubSshOptions = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect via GitHub SSH")
                    }
                    
                    if (showGithubSshOptions) {
                        GithubSshDialog(
                            onDismiss = { showGithubSshOptions = false },
                            onConnect = { success ->
                                showGithubSshOptions = false
                                if (success) {
                                    isConnectedViaSSH = true
                                    expandSshSection = false
                                }
                            }
                        )
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
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usePassword by remember { mutableStateOf(true) }
    var privateKey by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var localPort by remember { mutableStateOf("3000") }
    var remoteHost by remember { mutableStateOf("localhost") }
    var remotePort by remember { mutableStateOf("3000") }
    var connecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
                            connecting = true
                            errorMessage = ""
                            
                            RetrofitClient.connectToSsh(
                                host = host,
                                port = port.toIntOrNull() ?: 22,
                                username = username,
                                password = if (usePassword) password else null,
                                privateKey = if (!usePassword) privateKey else null,
                                passphrase = if (!usePassword) passphrase else null,
                                localPort = localPort.toIntOrNull() ?: 3000,
                                remoteHost = remoteHost,
                                remotePort = remotePort.toIntOrNull() ?: 3000
                            ) { success, message ->
                                connecting = false
                                if (success) {
                                    onConnect(true)
                                } else {
                                    errorMessage = message
                                }
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
 * Dialog for GitHub SSH connection configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GithubSshDialog(
    onDismiss: () -> Unit,
    onConnect: (Boolean) -> Unit
) {
    var githubUsername by remember { mutableStateOf("") }
    var sshKeyPath by remember { mutableStateOf("~/.ssh/id_rsa") }
    var passphrase by remember { mutableStateOf("") }
    var localPort by remember { mutableStateOf("3000") }
    var remoteHost by remember { mutableStateOf("localhost") }
    var remotePort by remember { mutableStateOf("3000") }
    var connecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
                    text = "GitHub SSH Connection",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // GitHub details
                OutlinedTextField(
                    value = githubUsername,
                    onValueChange = { githubUsername = it },
                    label = { Text("GitHub Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = sshKeyPath,
                    onValueChange = { sshKeyPath = it },
                    label = { Text("SSH Key Path") },
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
                            connecting = true
                            errorMessage = ""
                            
                            RetrofitClient.connectToGithubSsh(
                                githubUsername = githubUsername,
                                sshKeyPath = sshKeyPath,
                                passphrase = passphrase,
                                localPort = localPort.toIntOrNull() ?: 3000,
                                remoteHost = remoteHost,
                                remotePort = remotePort.toIntOrNull() ?: 3000
                            ) { success, message ->
                                connecting = false
                                if (success) {
                                    onConnect(true)
                                } else {
                                    errorMessage = message
                                }
                            }
                        },
                        enabled = !connecting && githubUsername.isNotEmpty() && sshKeyPath.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}