package com.example.shallowseek.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shallowseek.data.ModelInfo
import com.example.shallowseek.data.ModelDetails

/**
 * A composable that displays a dropdown for selecting an Ollama model.
 * 
 * @param models List of available models
 * @param selectedModel The currently selected model name
 * @param onModelSelected Callback for when a model is selected
 * @param onRefreshModels Callback for when the refresh button is clicked
 * @param isLoading Whether models are currently being loaded
 * @param modifier Modifier for styling
 */
@Composable
fun ModelSelector(
    models: List<ModelInfo>,
    selectedModel: String?,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Model Selection",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Selected model display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedModel ?: "Select a model",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle model selection"
                        )
                    }
                    
                    // Dropdown menu
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        models.forEach { model ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = model.name,
                                            fontWeight = if (model.name == selectedModel) FontWeight.Bold else FontWeight.Normal
                                        )
                                        
                                        val details = mutableListOf<String>()
                                        
                                        model.details?.parameter_size?.let { 
                                            details.add("Size: $it") 
                                        }
                                        
                                        model.details?.family?.let { 
                                            details.add("Family: $it") 
                                        }
                                        
                                        model.details?.quantization_level?.let { 
                                            details.add("Quantization: $it") 
                                        }
                                        
                                        if (details.isNotEmpty()) {
                                            Text(
                                                text = details.joinToString(" • "),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onModelSelected(model.name)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Refresh button
                IconButton(
                    onClick = onRefreshModels,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh models"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModelSelectorPreview() {
    val previewModels = listOf(
        ModelInfo(
            name = "deepseek-r1:1.5b",
            model = "deepseek-r1:1.5b",
            size = 1500000000,
            modified_at = "2023-01-01T00:00:00Z",
            digest = "123456",
            details = ModelDetails(
                parameter_size = "1.8B",
                family = "qwen2",
                quantization_level = "Q4_K_M"
            )
        ),
        ModelInfo(
            name = "llama2:7b",
            model = "llama2:7b",
            size = 7000000000,
            modified_at = "2023-01-01T00:00:00Z",
            digest = "789012",
            details = ModelDetails(
                parameter_size = "7B",
                family = "llama",
                quantization_level = "Q4_0"
            )
        )
    )
    
    ModelSelector(
        models = previewModels,
        selectedModel = "deepseek-r1:1.5b",
        onModelSelected = {},
        onRefreshModels = {},
        isLoading = false
    )
}