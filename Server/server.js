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
  const clientIP = req.headers['x-forwarded-for'] || req.connection.remoteAddress;
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} from IP: ${clientIP}`);
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

// Root endpoint - show simple homepage
app.get('/', (req, res) => {
  console.log('Homepage requested');
  res.send(`
    <html>
      <head>
        <title>ShallowSeek Server</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
          h1 { color: #333; }
          .container { max-width: 800px; margin: 0 auto; }
          .endpoint { background: #f4f4f4; padding: 10px; margin-bottom: 10px; border-radius: 5px; }
          code { background: #eee; padding: 2px 5px; border-radius: 3px; }
        </style>
      </head>
      <body>
        <div class="container">
          <h1>ShallowSeek Server</h1>
          <p>Server is running. Status: Active</p>
          
          <h2>Available Endpoints:</h2>
          <div class="endpoint">
            <h3>GET /health</h3>
            <p>Check server health status</p>
          </div>
          
          <div class="endpoint">
            <h3>GET /echo</h3>
            <p>Test endpoint that echoes back request information</p>
          </div>
          
          <div class="endpoint">
            <h3>GET /models</h3>
            <p>Get available language models from Ollama</p>
          </div>
          
          <div class="endpoint">
            <h3>POST /generate</h3>
            <p>Generate text using a language model</p>
          </div>
          
          <h3>SSH Tunnel Status:</h3>
          <p>Active tunnels: ${sshTunnel ? Array.from(sshTunnel.tunnels.keys()).join(', ') : 'None'}</p>
        </div>
      </body>
    </html>
  `);
});

// Health check endpoint
app.get('/health', (req, res) => {
  console.log('[HEALTH CHECK] Received health check request');
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    message: 'Server is healthy and responding to requests',
    tunnelActive: sshTunnel && sshTunnel.isConnected ? true : false
  });
});

// Echo endpoint for testing connections
app.get('/echo', (req, res) => {
  console.log('[ECHO] Received echo request');
  res.json({
    message: 'Echo successful!',
    timestamp: new Date().toISOString(),
    headers: req.headers,
    query: req.query,
    tunnelActive: sshTunnel && sshTunnel.isConnected ? true : false,
    tunnelInfo: sshTunnel ? Array.from(sshTunnel.tunnels.keys()) : []
  });
});

// SSH debug endpoint - to check SSH configuration on the server
app.get('/ssh/debug', (req, res) => {
  const os = require('os');
  const { execSync } = require('child_process');
  
  // Get system information
  const sshInfo = {
    hostname: os.hostname(),
    platform: os.platform(),
    release: os.release(),
    networkInterfaces: os.networkInterfaces(),
    tunnels: sshTunnel ? Array.from(sshTunnel.tunnels.keys()) : [],
    sshRunning: false,
    sshd_config: {}
  };
  
  // Check if SSH is running
  try {
    if (os.platform() === 'linux' || os.platform() === 'darwin') {
      const sshStatus = execSync('ps -ef | grep sshd | grep -v grep').toString();
      sshInfo.sshRunning = sshStatus.length > 0;
      
      // Try to get SSH config info
      try {
        const sshdConfig = execSync('grep "^Password\\|^Public" /etc/ssh/sshd_config').toString();
        const lines = sshdConfig.split('\n');
        lines.forEach(line => {
          if (line.trim()) {
            const [key, value] = line.split(/\s+/);
            sshInfo.sshd_config[key] = value;
          }
        });
      } catch (e) {
        sshInfo.sshd_config.error = 'Could not read SSH config';
      }
    } else if (os.platform() === 'win32') {
      const sshStatus = execSync('tasklist | findstr ssh').toString();
      sshInfo.sshRunning = sshStatus.includes('sshd.exe');
    }
  } catch (e) {
    sshInfo.sshRunning = false;
    sshInfo.sshCheckError = e.message;
  }
  
  res.json(sshInfo);
});

/**
 * SSH Tunnel endpoints
 */

// Create SSH tunnel
app.post('/ssh/connect', async (req, res) => {
  const { host, port, username, password, privateKey, passphrase, localPort, remoteHost, remotePort } = req.body;
  
  console.log('=== SSH Connection Request ===');
  console.log(`Host: ${host}`);
  console.log(`Port: ${port || 22}`);
  console.log(`Username: ${username}`);
  console.log(`Auth method: ${password ? 'password' : 'privateKey'}`);
  console.log(`Tunnel: 0.0.0.0:${localPort} -> ${remoteHost}:${remotePort}`);
  console.log('===============================');
  
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
    
    console.log('Connecting to SSH server...');
    // Connect to SSH server
    await sshTunnel.connect();
    
    console.log('SSH connected. Creating tunnel...');
    // Create tunnel
    await sshTunnel.createTunnel(localPort, remoteHost, remotePort);
    
    console.log('Tunnel established successfully');
    
    res.json({
      status: 'success',
      message: `SSH tunnel established from 0.0.0.0:${localPort} to ${remoteHost}:${remotePort}`,
      tunnel: {
        localPort,
        remoteHost,
        remotePort
      },
      note: "The SSH tunnel now forwards traffic from localhost:" + localPort + " to " + remoteHost + ":" + remotePort
    });
  } catch (error) {
    console.error('SSH tunnel error:', error);
    console.error('Error details:', {
      message: error.message,
      code: error.code,
      level: error.level,
      host,
      port: port || 22,
      username,
      authMethod: password ? 'password' : 'privateKey',
      localPort,
      remoteHost,
      remotePort
    });
    
    // Prepare a user-friendly error message
    let errorMessage = 'Failed to establish SSH tunnel';
    if (error.userMessage) {
      errorMessage = error.userMessage; // Use the enhanced error message if available
    } else if (error.level === 'client-authentication') {
      errorMessage = 'SSH authentication failed. Check your username and password and ensure password authentication is enabled.';
    }
    
    res.status(500).json({
      status: 'error',
      message: errorMessage,
      error: error.message,
      details: {
        code: error.code,
        level: error.level
      }
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
app.listen(PORT, '0.0.0.0', () => {
  console.log(`
╔══════════════════════════════════════════════╗
║                                              ║
║   ShallowSeek Server running on port ${PORT}    ║
║   http://0.0.0.0:${PORT} (all interfaces)      ║
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
