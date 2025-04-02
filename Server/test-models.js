const axios = require('axios');

// Function to test the models endpoint
async function testModelsEndpoint() {
  try {
    console.log('Testing the models endpoint...');
    const response = await axios.get('http://localhost:3000/models');
    console.log('Response data:', JSON.stringify(response.data, null, 2));
  } catch (error) {
    console.error('Error testing models endpoint:', error.message);
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    } else if (error.request) {
      console.error('No response received');
    }
  }
}

// Start server in a separate process
const { spawn } = require('child_process');
const server = spawn('node', ['server.js'], { 
  stdio: ['inherit', 'inherit', 'inherit'],
  detached: false
});

// Give the server time to start
setTimeout(() => {
  testModelsEndpoint().finally(() => {
    console.log('Test complete, killing server...');
    server.kill();
    process.exit(0);
  });
}, 2000);