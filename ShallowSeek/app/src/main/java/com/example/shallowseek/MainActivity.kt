package com.example.shallowseek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shallowseek.ui.screens.MainScreen
import com.example.shallowseek.ui.theme.ShallowSeekTheme
import com.example.shallowseek.viewmodel.MainViewModel

/**
 * Main entry point for the ShallowSeek application.
 * 
 * This Activity initializes the Compose UI and sets up the application.
 * The app connects to a Node.js server that interfaces with Ollama running
 * the DeepSeek model.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ShallowSeekTheme {
                // Create a surface container with the default background color
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create the ViewModel and pass it to the MainScreen
                    val viewModel: MainViewModel = viewModel()
                    MainScreen(viewModel)
                }
            }
        }
    }
}