/**
 * ShallowSeek Server
 * 
 * This Express.js server acts as a middleware between the Android client and Ollama.
 * It forwards requests to the Ollama API running the DeepSeek model and returns the responses.
 */

const express = require('express');
const axios = require('axios');
const cors = require('cors');

const app = express();
const PORT = 3000;
const OLLAMA_BASE_URL = 'http://localhost:11434/api';
const OLLAMA_GENERATE_API = `${OLLAMA_BASE_URL}/generate`;
const OLLAMA_MODELS_API = `${OLLAMA_BASE_URL}/tags`;

// Middleware
app.use(cors());
app.use(express.json());

// Logging middleware
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

/**
 * Text generation endpoint
 * Forwards requests to Ollama's API and streams back the response
 */
app.post('/generate', async (req, res) => {
  const { prompt, system, model } = req.body;

  if (!prompt) {
    return res.status(400).json({ error: 'Prompt is required' });
  }

  console.log(`Processing prompt using model ${model || 'deepseek-r1:1.5b'}: "${prompt.substring(0, 50)}${prompt.length > 50 ? '...' : ''}"`);

  try {
    // Configure request to Ollama
    const ollamaRequest = {
      model: model || 'deepseek-r1:1.5b',
      prompt: prompt,
      system: system || '',
      stream: false
    };

    console.log('Sending request to Ollama API...');

    // Forward request to Ollama
    const response = await axios.post(OLLAMA_GENERATE_API, ollamaRequest);

    console.log('Received response from Ollama');

    // Return the Ollama response
    return res.json(response.data);
  } catch (error) {
    console.error('Error communicating with Ollama:', error.message);

    // Detailed error logging
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    } else if (error.request) {
      console.error('No response received');
    }

    return res.status(500).json({
      error: 'Failed to communicate with Ollama',
      details: error.message
    });
  }
});

/**
 * Get available models from Ollama
 */
app.get('/models', async (req, res) => {
  try {
    console.log('Fetching available models from Ollama...');
    
    // Call Ollama's API to get available models
    const response = await axios.get(OLLAMA_MODELS_API);
    
    // Log the response structure for debugging
    console.log('Ollama models API response structure:');
    console.log(JSON.stringify(response.data, null, 2).substring(0, 500) + '...');
    
    console.log(`Found ${response.data.models?.length || 0} models`);
    
    // Return the list of models
    return res.json(response.data);
  } catch (error) {
    console.error('Error fetching models from Ollama:', error.message);
    
    // Detailed error logging
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    } else if (error.request) {
      console.error('No response received');
    }
    
    return res.status(500).json({
      error: 'Failed to fetch models from Ollama',
      details: error.message
    });
  }
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Start server
app.listen(PORT, () => {
  console.log(`
╔══════════════════════════════════════════════╗
║                                              ║
║   ShallowSeek Server running on port ${PORT}    ║
║   http://localhost:${PORT}                      ║
║                                              ║
║   Connected to Ollama at:                    ║
║   ${OLLAMA_BASE_URL}                   ║
║                                              ║
╚══════════════════════════════════════════════╝
  `);
});