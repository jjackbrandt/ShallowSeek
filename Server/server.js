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
const OLLAMA_API = 'http://localhost:11434/api/generate';

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
  const { prompt, system } = req.body;

  if (!prompt) {
    return res.status(400).json({ error: 'Prompt is required' });
  }

  console.log(`Processing prompt: "${prompt.substring(0, 50)}${prompt.length > 50 ? '...' : ''}"`);

  try {
    // Configure request to Ollama
    const ollamaRequest = {
      model: 'deepseek-r1:1.5b',
      prompt: prompt,
      system: system || '',
      stream: false
    };

    console.log('Sending request to Ollama API...');

    // Forward request to Ollama
    const response = await axios.post(OLLAMA_API, ollamaRequest);

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
║   ${OLLAMA_API}                   ║
║                                              ║
╚══════════════════════════════════════════════╝
  `);
});