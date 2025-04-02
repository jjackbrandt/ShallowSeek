package com.example.shallowseek.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shallowseek.ui.components.LoadingIndicator
import com.example.shallowseek.ui.components.ModelSelector
import com.example.shallowseek.ui.components.PromptInput
import com.example.shallowseek.ui.components.ResponseDisplay
import com.example.shallowseek.ui.components.ServerAddressInput
import com.example.shallowseek.viewmodel.MainViewModel

/**
 * Main screen of the ShallowSeek app.
 * 
 * This composable displays the complete UI with server configuration,
 * model selection, prompt input, and response display areas.
 * 
 * @param viewModel The ViewModel that manages the state and business logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettings by remember { mutableStateOf(false) }
    
    // Show error messages as snackbars
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ShallowSeek") },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Settings area (shown/hidden)
            if (showSettings) {
                // Server configuration
                ServerAddressInput(
                    currentAddress = viewModel.serverAddress,
                    onAddressChange = { viewModel.updateServerAddress(it) }
                )
                
                // Model selection
                ModelSelector(
                    models = viewModel.availableModels,
                    selectedModel = viewModel.selectedModel,
                    onModelSelected = { viewModel.selectModel(it) },
                    onRefreshModels = { viewModel.loadModels() },
                    isLoading = viewModel.isLoadingModels
                )
                
                Divider()
            }
            
            // Response area (expands to fill available space)
            ResponseDisplay(
                response = viewModel.responseText,
                modifier = Modifier.weight(1f)
            )
            
            // Loading indicator (shown only when loading)
            if (viewModel.isLoading) {
                LoadingIndicator()
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            
            // Prompt input area (at the bottom)
            PromptInput(
                value = viewModel.promptText,
                onValueChange = { viewModel.updatePrompt(it) },
                onSend = { viewModel.sendPrompt() },
                isLoading = viewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}