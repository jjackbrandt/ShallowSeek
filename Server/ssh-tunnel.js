/**
 * SSH Tunnel Module for ShallowSeek
 * 
 * This module sets up an SSH tunnel from your device to the server,
 * allowing secure port forwarding for connecting to localhost services.
 */

const { Client } = require('ssh2');
const net = require('net');

class SSHTunnel {
  constructor() {
    this.tunnels = new Map();
    this.sshClient = null;
    this.isConnected = false;
    this.config = null;
  }

  /**
   * Configure SSH connection settings
   * 
   * @param {Object} config SSH configuration
   * @param {string} config.host SSH server hostname
   * @param {number} config.port SSH server port (default: 22)
   * @param {string} config.username SSH username
   * @param {string} config.password SSH password (optional if using privateKey)
   * @param {string} config.privateKey Private key for authentication (optional if using password)
   * @param {string} config.passphrase Passphrase for privateKey (optional)
   */
  setConfig(config) {
    this.config = {
      host: config.host,
      port: config.port || 22,
      username: config.username,
      password: config.password,
      privateKey: config.privateKey,
      passphrase: config.passphrase
    };
    return this;
  }

  /**
   * Connect to SSH server
   * 
   * @returns {Promise} Promise that resolves when connected
   */
  connect() {
    return new Promise((resolve, reject) => {
      if (!this.config) {
        return reject(new Error('SSH configuration not set. Call setConfig() first.'));
      }

      if (this.isConnected) {
        console.log('SSH already connected');
        return resolve();
      }

      this.sshClient = new Client();

      this.sshClient.on('ready', () => {
        console.log('SSH connection established successfully - authentication successful');
        console.log(`SSH connection details: ${this.config.host}:${this.config.port} user: ${this.config.username} auth: ${this.config.password ? 'password' : 'privateKey'}`);
        this.isConnected = true;
        resolve();
      });

      this.sshClient.on('error', (err) => {
        console.error('SSH connection error:', err);
        
        // Detailed error analysis
        let errorDetails = {
          message: err.message,
          code: err.code,
          level: err.level,
          host: this.config.host,
          port: this.config.port,
          username: this.config.username,
          authMethod: this.config.password ? 'password' : 'privateKey'
        };
        
        console.error('Error details:', errorDetails);
        
        // Provide more user-friendly error messages
        let userMessage = '';
        if (err.level === 'client-authentication') {
          userMessage = 'SSH authentication failed. Check your username and password. ' +
            'Make sure your SSH server allows password authentication (check your sshd_config).';
          console.error(userMessage);
        } else if (err.code === 'ECONNREFUSED') {
          userMessage = `SSH connection refused to ${this.config.host}:${this.config.port}. ` +
            'Make sure SSH server is running and accepting connections.';
          console.error(userMessage);
        } else if (err.code === 'ETIMEDOUT') {
          userMessage = `SSH connection timed out to ${this.config.host}:${this.config.port}. ` +
            'Check your network connection and firewall settings.';
          console.error(userMessage);
        }
        
        // Add the user-friendly message to the error object
        if (userMessage) {
          err.userMessage = userMessage;
        }
        
        this.isConnected = false;
        reject(err);
      });

      this.sshClient.on('end', () => {
        console.log('SSH connection ended');
        this.isConnected = false;
      });

      this.sshClient.on('close', (hadError) => {
        console.log(`SSH connection closed${hadError ? ' with error' : ''}`);
        this.isConnected = false;
      });

      try {
        // Add reasonable connection timeout (30 seconds)
        const configWithTimeout = {
          ...this.config,
          readyTimeout: 30000, // 30 seconds timeout (increased from 15 seconds)
          keepaliveInterval: 5000, // Send keep-alive every 5 seconds
          keepaliveCountMax: 3 // Disconnect after 3 failed keep-alives
        };
        this.sshClient.connect(configWithTimeout);
      } catch (err) {
        reject(err);
      }
    });
  }

  /**
   * Create a port forwarding tunnel
   * 
   * @param {number} localPort Local port to listen on
   * @param {string} remoteHost Remote host to forward to
   * @param {number} remotePort Remote port to forward to
   * @returns {Promise} Promise that resolves with local server when tunnel is established
   */
  createTunnel(localPort, remoteHost, remotePort) {
    return new Promise((resolve, reject) => {
      if (!this.isConnected) {
        return reject(new Error('SSH not connected. Call connect() first.'));
      }

      const tunnelKey = `${localPort}:${remoteHost}:${remotePort}`;
      
      // Check if tunnel already exists
      if (this.tunnels.has(tunnelKey)) {
        console.log(`Tunnel ${tunnelKey} already exists`);
        return resolve(this.tunnels.get(tunnelKey));
      }

      const server = net.createServer((socket) => {
        console.log(`Attempting to create forward connection from 0.0.0.0:${localPort} to ${remoteHost}:${remotePort}`);
        
        this.sshClient.forwardOut(
          '0.0.0.0', // source address, using all interfaces
          localPort,   // source port, this can be any valid port
          remoteHost,  // destination address (viewed from the SSH server)
          remotePort,  // destination port
          (err, stream) => {
            if (err) {
              console.error(`Tunnel error for ${tunnelKey}:`, err);
              console.error('Error details:', err);
              console.error(`Failed to forward port from 0.0.0.0:${localPort} to ${remoteHost}:${remotePort}`);
              socket.end();
              return;
            }
            
            console.log(`Successfully forwarded connection from 0.0.0.0:${localPort} to ${remoteHost}:${remotePort}`);

            socket.pipe(stream).pipe(socket);

            stream.on('close', () => {
              socket.end();
            });

            socket.on('close', () => {
              stream.end();
            });
          }
        );
      });

      server.on('error', (err) => {
        console.error(`Server error for tunnel ${tunnelKey}:`, err);
        reject(err);
      });

      try {
        // Handle port in use errors gracefully
        server.on('error', (err) => {
          if (err.code === 'EADDRINUSE') {
            console.error(`Port ${localPort} is already in use. Try a different port.`);
            console.error(`SUGGESTION: Use port 3001 or higher to avoid conflicts with the Node.js server.`);
            err.userMessage = `Port ${localPort} is already in use. Please try a different port (3001 or higher).`;
            reject(err);
          } else {
            console.error(`Server error for tunnel ${tunnelKey}:`, err);
            reject(err);
          }
        });
        
        server.listen(localPort, '0.0.0.0', () => {
          console.log(`Tunnel server listening on 0.0.0.0:${localPort}, ready to forward to ${remoteHost}:${remotePort}`);
          console.log(`IMPORTANT: Clients should now connect to localhost:${localPort} on the device running this app`);
          console.log(`IMPORTANT: Verify the Node.js server is also running on ${remoteHost}:${remotePort}`);
          
          // Store the server instance so we can close it later
          this.tunnels.set(tunnelKey, server);
          
          resolve(server);
        });
      } catch (err) {
        console.error(`Failed to listen on port ${localPort}:`, err);
        reject(err);
      }
    });
  }

  /**
   * Close a specific tunnel
   * 
   * @param {number} localPort Local port of the tunnel to close
   * @param {string} remoteHost Remote host of the tunnel to close
   * @param {number} remotePort Remote port of the tunnel to close
   * @returns {boolean} True if tunnel was closed, false if it didn't exist
   */
  closeTunnel(localPort, remoteHost, remotePort) {
    const tunnelKey = `${localPort}:${remoteHost}:${remotePort}`;
    
    if (!this.tunnels.has(tunnelKey)) {
      return false;
    }

    const server = this.tunnels.get(tunnelKey);
    server.close();
    this.tunnels.delete(tunnelKey);
    
    console.log(`Closed tunnel: ${tunnelKey}`);
    return true;
  }

  /**
   * Close all tunnels and disconnect from SSH
   */
  disconnect() {
    // Close all tunnels
    for (const [key, server] of this.tunnels.entries()) {
      server.close();
      console.log(`Closed tunnel: ${key}`);
    }
    
    this.tunnels.clear();

    // Disconnect SSH client
    if (this.sshClient && this.isConnected) {
      this.sshClient.end();
      this.isConnected = false;
    }
  }

  /**
   * Create a tunnel using GitHub SSH key
   * 
   * @param {number} localPort Local port to listen on
   * @param {string} remoteHost Remote host to forward to 
   * @param {number} remotePort Remote port to forward to
   * @param {string} githubUsername GitHub username
   * @param {string} sshKeyPath Path to the GitHub SSH key (default: ~/.ssh/id_rsa)
   * @param {string} passphrase Passphrase for the SSH key (optional)
   * @returns {Promise} Promise that resolves when tunnel is established
   */
  static async createGitHubTunnel(
    localPort, 
    remoteHost, 
    remotePort, 
    githubUsername, 
    sshKeyPath = '~/.ssh/id_rsa',
    passphrase = ''
  ) {
    const fs = require('fs');
    const os = require('os');
    const path = require('path');
    
    // Resolve path with home directory if necessary
    const expandedPath = sshKeyPath.replace(/^~/, os.homedir());
    
    console.log('GitHub SSH Connection Request:');
    console.log(`- GitHub Username: ${githubUsername}`);
    console.log(`- SSH Key Path: ${sshKeyPath}`);
    console.log(`- Expanded Path: ${expandedPath}`);
    console.log(`- Tunnel: localhost:${localPort} -> ${remoteHost}:${remotePort}`);
    
    try {
      // Check if file exists
      if (!fs.existsSync(expandedPath)) {
        console.error(`SSH key file not found at path: ${expandedPath}`);
        throw new Error(`SSH key not found at ${expandedPath}`);
      }
      
      // Read the private key
      const privateKey = fs.readFileSync(expandedPath, 'utf8');
      console.log(`- SSH Key loaded (${privateKey.length} bytes)`);
      
      // Create a new tunnel instance
      const tunnel = new SSHTunnel();
      
      // Configure SSH with GitHub credentials
      tunnel.setConfig({
        host: 'ssh.github.com',
        port: 22,
        username: githubUsername,
        privateKey: privateKey,
        passphrase: passphrase
      });
      
      console.log('- Attempting to connect to GitHub SSH server (ssh.github.com)');
      
      // Connect to SSH server
      await tunnel.connect();
      
      console.log('- Successfully connected to GitHub SSH');
      console.log(`- Creating tunnel: localhost:${localPort} -> ${remoteHost}:${remotePort}`);
      
      // Create the tunnel
      await tunnel.createTunnel(localPort, remoteHost, remotePort);
      
      console.log('- Tunnel successfully established');
      return tunnel;
    } catch (error) {
      console.error('Failed to create GitHub SSH tunnel:', error);
      console.error('Error details:', {
        message: error.message,
        code: error.code,
        path: expandedPath,
        username: githubUsername
      });
      throw error;
    }
  }
}

module.exports = SSHTunnel;