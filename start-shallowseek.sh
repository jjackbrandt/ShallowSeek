#!/bin/bash

# ANSI color codes
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Print banner
echo -e "${BLUE}"
cat << 'EOF'
  _________.__           .__  .__                  ___________________________________  __.
 /   _____/|  |__ _____  |  | |  |   ______  _  __/   _____/\_   _____/\_   _____/    |/ _|
 \_____  \ |  |  \\__  \ |  | |  |  /  _ \ \/ \/ /\_____  \  |    __)_  |    __)_|      <
 /        \|   Y  \/ __ \|  |_|  |_(  <_> )     / /        \ |        \ |        \    |  \
/_______  /|___|  (____  /____/____/\____/ \/\_/ /_______  //_______  //_______  /____|__ \
        \/      \/     \/                                \/         \/         \/        \/
EOF
echo -e "${NC}"
echo -e "${CYAN}Honors Contracting Project${NC}\n"

# Function to check if a command exists
command_exists() {
    command -v "$1" &> /dev/null
}

# Check if Ollama is installed
if ! command_exists ollama; then
    echo -e "${RED}Error: Ollama is not installed.${NC}"
    echo -e "Please install Ollama from ${CYAN}https://ollama.ai${NC}"
    exit 1
fi

# Check if Node.js is installed
if ! command_exists node; then
    echo -e "${RED}Error: Node.js is not installed.${NC}"
    echo -e "Please install Node.js from ${CYAN}https://nodejs.org${NC}"
    exit 1
fi

# Check if Ollama is running
echo -e "${YELLOW}Checking if Ollama is running...${NC}"
if ! curl -s http://localhost:11434/api/tags &> /dev/null; then
    echo -e "${YELLOW}Ollama is not running. Attempting to start it...${NC}"
    
    # Try different methods to start Ollama
    if [ -d "/Applications/Ollama.app" ]; then
        # Try using open command if the app is in Applications
        echo -e "Found Ollama.app in Applications, launching..."
        open /Applications/Ollama.app
    elif command_exists ollama; then
        # Try starting Ollama using the CLI if installed
        echo -e "Starting Ollama using CLI..."
        ollama serve &>/dev/null &
    else
        echo -e "${RED}Could not find a way to start Ollama automatically.${NC}"
        echo -e "${YELLOW}Please start Ollama manually and then press Enter to continue...${NC}"
        read -r
    fi

    # Wait for Ollama to start
    echo -e "Waiting for Ollama to initialize..."
    for i in {1..45}; do
        if curl -s http://localhost:11434/api/tags &> /dev/null; then
            echo -e "${GREEN}Ollama is now running!${NC}"
            break
        fi

        if [ $i -eq 45 ]; then
            echo -e "${RED}Timed out waiting for Ollama to start.${NC}"
            echo -e "Please ensure Ollama is installed and running manually, then try again."
            exit 1
        fi

        sleep 1
        echo -n "."
    done
    echo ""
else
    echo -e "${GREEN}Ollama is already running!${NC}"
fi

# Check if at least one model is available
echo -e "${YELLOW}Checking available models...${NC}"
MODELS=$(curl -s http://localhost:11434/api/tags | grep -o '"name":"[^"]*' | grep -o '[^"]*$')

if [ -z "$MODELS" ]; then
    echo -e "${YELLOW}No models found. Pulling a small model (deepseek-r1:1.5b)...${NC}"
    ollama pull deepseek-r1:1.5b
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to pull model. Please run 'ollama pull deepseek-r1:1.5b' manually.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Model deepseek-r1:1.5b has been pulled successfully!${NC}"
else
    echo -e "${GREEN}Available models:${NC}"
    echo "$MODELS" | while read -r model; do
        echo -e "  - ${CYAN}$model${NC}"
    done
fi

# Navigate to the server directory and start the server
echo -e "\n${YELLOW}Starting the Node.js server...${NC}"
SERVER_DIR="$(dirname "$0")/Server"

# Check if server directory exists
if [ ! -d "$SERVER_DIR" ]; then
    echo -e "${RED}Error: Server directory not found at $SERVER_DIR${NC}"
    exit 1
fi

# Check if package.json exists and install dependencies if needed
if [ -f "$SERVER_DIR/package.json" ]; then
    if [ ! -d "$SERVER_DIR/node_modules" ]; then
        echo -e "${YELLOW}Installing server dependencies...${NC}"
        (cd "$SERVER_DIR" && npm install)
    fi
else
    echo -e "${RED}Error: package.json not found in $SERVER_DIR${NC}"
    exit 1
fi

# Start the server in the background
echo -e "${GREEN}Starting server at http://localhost:3000${NC}"
(cd "$SERVER_DIR" && node server.js) &
SERVER_PID=$!

# Register a cleanup function to stop the server when the script is terminated
cleanup() {
    echo -e "\n${YELLOW}Stopping Node.js server...${NC}"
    kill $SERVER_PID
    echo -e "${GREEN}Server stopped.${NC}"
    exit 0
}

trap cleanup INT TERM

# Display instructions for running the Android app
echo -e "\n${GREEN}Server is running! ${NC}"
echo -e "\n${YELLOW}To use ShallowSeek:${NC}"
echo -e "1. Open the Android project in Android Studio"
echo -e "   ${CYAN}cd \"$(dirname "$0")/ShallowSeek\"${NC}"
echo -e "   ${CYAN}open -a \"Android Studio\" .${NC}"
echo -e "2. Build and run the app on your device or emulator"
echo -e "3. If using an emulator, the server address should be ${CYAN}http://10.0.2.2:3000/${NC}"
echo -e "4. If using a physical device on the same network, update the server address to ${CYAN}http://<your-computer-ip>:3000/${NC}"
echo -e "5. If using a physical device remotely, you can use SSH tunneling:"
echo -e "   - In the app, tap the arrow next to 'SSH Tunnel'"
echo -e "   - Choose 'Connect via SSH' or 'Connect via GitHub SSH'"
echo -e "   - Enter your SSH credentials and configure port forwarding"
echo -e "   - The server will be accessible securely through the SSH tunnel\n"

echo -e "${YELLOW}Server logs:${NC}"
echo -e "${CYAN}(Press Ctrl+C to stop the server)${NC}\n"

# Wait for the server process to end.
wait $SERVER_PID
