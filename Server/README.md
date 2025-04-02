# ShallowSeek Server

A Node.js Express server that acts as middleware between the ShallowSeek Android app and Ollama running the DeepSeek model.

## Prerequisites

- Node.js (v14 or later)
- npm
- Ollama installed and running on your MacBook: https://github.com/ollama/ollama
- DeepSeek model pulled in Ollama (run `ollama pull deepseek`)

## Setup

1. Install dependencies:
   ```
   npm install
   ```

2. Make sure Ollama is running and has the DeepSeek model loaded:
   ```
   ollama run deepseek
   ```

3. Start the server:
   ```
   npm start
   ```

   For development with auto-restart:
   ```
   npm run dev
   ```

## API Endpoints

### Generate Text
- **URL**: `/generate`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "prompt": "Your text prompt here",
    "system": "Optional system prompt"
  }
  ```
- **Response**: JSON object containing the model's response

### Health Check
- **URL**: `/health`
- **Method**: `GET`
- **Response**: JSON with server status

## Configuration

The server runs on port 3000 by default and connects to Ollama at `http://localhost:11434/api/generate`.

## Troubleshooting

1. Make sure Ollama is running before starting the server
2. Check that the DeepSeek model is available in your Ollama installation
3. Review the console logs for detailed error information
4. Ensure your Android app is configured with the correct server address (your MacBook's IP on the local network)