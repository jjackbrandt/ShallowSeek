# ShallowSeek

ShallowSeek is a mobile application that provides a user-friendly interface for interacting with Ollama's language models on your local machine. It consists of an Android client app built with Jetpack Compose and a Node.js server that acts as a middleware between the app and Ollama.

## Project Overview

This project combines modern Android development practices with a lightweight backend to create a seamless AI chat experience. All inference happens locally on your machine through Ollama, providing privacy and flexibility.

### Components

1. **Android App**: A native Android application built with Kotlin and Jetpack Compose
2. **Node.js Server**: A lightweight Express server that communicates with Ollama
3. **Ollama**: A local LLM runtime that provides access to various open-source models

## Features

- 🔄 **Model Selection**: Choose from all available Ollama models
- 🎨 **Theme Customization**: Multiple built-in themes including Light, Dark, Dracula, Material Deep Ocean, and more
- 🌐 **Configurable Server**: Connect to your Node.js server wherever it's running
- 🔒 **SSH Tunneling**: Securely connect to your server over SSH, including GitHub SSH support
- 💬 **Text Generation**: Send prompts and receive responses from Ollama models
- 🚀 **Responsive UI**: Clean, modern interface that adapts to your theme preference

## Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/) (v14 or later) for the server
- [Ollama](https://ollama.ai/) for running language models
- Android Studio for building and running the Android app

### Installation

1. **Clone the repository**

```bash
git clone <repository-url>
cd honors-contracting
```

2. **Server Setup**

```bash
cd Server
npm install
```

3. **Start Ollama**

Make sure you have Ollama installed and at least one model pulled:

```bash
ollama pull deepseek-r1:1.5b
```

4. **Start the Server**

```bash
cd Server
node server.js
```

5. **Build and Run the Android App**

Open the ShallowSeek directory in Android Studio and build/run the app on your device or emulator.

### Quick Start

For macOS users, you can use the provided startup script:

```bash
./start-shallowseek.sh
```

This script will:
1. Check if Ollama is running and start it if needed
2. Start the Node.js server
3. Provide instructions for running the Android app

## Usage

1. Launch the Android app on your device/emulator
2. The app will connect to the Node.js server (default is `http://10.0.2.2:3000/` for emulator)
3. If using a physical device, you have two connection options:
   - Direct connection: Update the server address in the settings menu to your server's IP address
   - SSH tunnel: Set up an SSH tunnel by tapping the expand arrow next to "SSH Tunnel" and choosing a connection method
4. Select your preferred model from the model picker
5. Choose your favorite theme from the theme section in settings
6. Enter prompts in the input field and tap Send

### SSH Connection Options

#### Standard SSH Connection
This option lets you connect to your server using standard SSH credentials:

1. In the Server Configuration section, tap the expand arrow next to "SSH Tunnel"
2. Tap "Connect via SSH"
3. Enter your SSH server details:
   - SSH Host: Your SSH server hostname or IP address
   - SSH Port: Usually 22 (default SSH port)
   - Username: Your SSH username
   - Password or Private Key: Choose your authentication method
4. Configure port forwarding:
   - Local Port: The port on your Android device (usually 3000)
   - Remote Host: The hostname your server needs to connect to (usually localhost)
   - Remote Port: The port your ShallowSeek server is running on (usually 3000)
5. Tap "Connect" to establish the tunnel

#### GitHub SSH Connection
This option lets you use your GitHub SSH credentials:

1. In the Server Configuration section, tap the expand arrow next to "SSH Tunnel"
2. Tap "Connect via GitHub SSH" 
3. Enter your GitHub details:
   - GitHub Username: Your GitHub username
   - SSH Key Path: Path to your SSH key (defaults to ~/.ssh/id_rsa)
   - Passphrase: Your SSH key passphrase (if needed)
4. Configure port forwarding:
   - Local Port: The port on your Android device (usually 3000)
   - Remote Host: The hostname your server needs to connect to (usually localhost)
   - Remote Port: The port your ShallowSeek server is running on (usually 3000)
5. Tap "Connect" to establish the tunnel through GitHub's SSH servers

### SSH Key Setup for Android

To use SSH tunneling on your Android device, you'll need to set up SSH keys:

1. **Generate SSH Keys** (if you don't already have them):
   - On your computer, run: `ssh-keygen -t ed25519 -C "your_email@example.com"`
   - Follow prompts to save the key (default location is `~/.ssh/id_ed25519`)
   - Set a passphrase if desired (you'll need this when connecting from the app)

2. **Add SSH Key to GitHub** (for GitHub SSH connection):
   - Copy your public key: `cat ~/.ssh/id_ed25519.pub`
   - Go to GitHub → Settings → SSH and GPG keys → New SSH key
   - Paste your public key and save

3. **Transfer Private Key to Android Device**:
   - Use a secure method like ADB or a password manager to transfer your private key
   - Example using ADB:
     ```bash
     adb push ~/.ssh/id_ed25519 /sdcard/Download/
     ```
   - In the app, specify this file location when connecting via SSH

## Architecture

### Android App (Kotlin + Jetpack Compose)

- **MVVM Architecture**: Separation of UI from business logic
- **Repository Pattern**: Clean data access layer
- **Retrofit**: Type-safe HTTP client for API communication
- **Coroutines**: Asynchronous programming for smooth UX
- **Material 3**: Modern design principles

### Server (Node.js + Express)

- **Middleware**: Bridges the Android app with Ollama
- **API Endpoints**: Simplified interface for model selection and text generation
- **Error Handling**: Graceful error management

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Ollama](https://ollama.ai/) for making local LLMs accessible
- [DeepSeek-Coder](https://github.com/deepseek-ai/DeepSeek-Coder) for the excellent LLM models
- Auburn University for supporting this project