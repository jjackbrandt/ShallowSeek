/**
 * ShallowSeek Server
 *
 * This Express.js server acts as a middleware between the Android client and Ollama.
 * It forwards requests to the Ollama API running the DeepSeek model and returns the responses.
 * 
 * Also supports SSH tunneling for remote access to the server.
 */

const express = require('express');
const axios = require('axios');
const cors = require('cors');
const SSHTunnel = require('./ssh-tunnel');
const fs = require('fs');
const os = require('os');
const path = require('path');

const app = express();
const PORT = 3000;
const OLLAMA_BASE_URL = 'http://localhost:11434/api';
const OLLAMA_GENERATE_API = `${OLLAMA_BASE_URL}/generate`;
const OLLAMA_MODELS_API = `${OLLAMA_BASE_URL}/tags`;

// SSH tunnel instance
let sshTunnel = null;

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

/**
 * SSH Tunnel endpoints
 */

// Create SSH tunnel
app.post('/ssh/connect', async (req, res) => {
  const { host, port, username, password, privateKey, passphrase, localPort, remoteHost, remotePort } = req.body;
  
  try {
    // Close any existing tunnel
    if (sshTunnel) {
      sshTunnel.disconnect();
    }
    
    // Create a new tunnel
    sshTunnel = new SSHTunnel();
    
    // Configure SSH connection
    sshTunnel.setConfig({
      host,
      port: port || 22,
      username,
      password,
      privateKey,
      passphrase
    });
    
    // Connect to SSH server
    await sshTunnel.connect();
    
    // Create tunnel
    await sshTunnel.createTunnel(localPort, remoteHost, remotePort);
    
    res.json({
      status: 'success',
      message: `SSH tunnel established from localhost:${localPort} to ${remoteHost}:${remotePort}`,
      tunnel: {
        localPort,
        remoteHost,
        remotePort
      }
    });
  } catch (error) {
    console.error('SSH tunnel error:', error);
    
    res.status(500).json({
      status: 'error',
      message: 'Failed to establish SSH tunnel',
      error: error.message
    });
  }
});

// Create SSH tunnel with GitHub
app.post('/ssh/github', async (req, res) => {
  const { githubUsername, sshKeyPath, passphrase, localPort, remoteHost, remotePort } = req.body;
  
  try {
    // Close any existing tunnel
    if (sshTunnel) {
      sshTunnel.disconnect();
    }
    
    // Create a new GitHub tunnel
    sshTunnel = await SSHTunnel.createGitHubTunnel(
      localPort,
      remoteHost,
      remotePort,
      githubUsername,
      sshKeyPath || '~/.ssh/id_rsa',
      passphrase || ''
    );
    
    res.json({
      status: 'success',
      message: `GitHub SSH tunnel established from localhost:${localPort} to ${remoteHost}:${remotePort}`,
      tunnel: {
        localPort,
        remoteHost,
        remotePort
      }
    });
  } catch (error) {
    console.error('GitHub SSH tunnel error:', error);
    
    res.status(500).json({
      status: 'error',
      message: 'Failed to establish GitHub SSH tunnel',
      error: error.message
    });
  }
});

// Check SSH status
app.get('/ssh/status', (req, res) => {
  if (sshTunnel && sshTunnel.isConnected) {
    const tunnels = Array.from(sshTunnel.tunnels.keys());
    res.json({
      status: 'connected',
      tunnels
    });
  } else {
    res.json({
      status: 'disconnected'
    });
  }
});

// Disconnect SSH tunnel
app.post('/ssh/disconnect', (req, res) => {
  if (sshTunnel) {
    sshTunnel.disconnect();
    sshTunnel = null;
    
    res.json({
      status: 'success',
      message: 'SSH tunnel disconnected'
    });
  } else {
    res.json({
      status: 'success',
      message: 'No SSH tunnel was connected'
    });
  }
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
║   ${OLLAMA_BASE_URL}                  ║
║                                              ║
║   SSH Tunnel Endpoints:                      ║
║   - POST /ssh/connect (Standard SSH)         ║
║   - POST /ssh/github (GitHub SSH)            ║
║   - GET  /ssh/status (Check tunnel status)   ║
║   - POST /ssh/disconnect (Close tunnels)     ║
║                                              ║
╚══════════════════════════════════════════════╝
  `);
});
