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
3. If using a physical device, update the server address in the settings menu
4. Select your preferred model from the model picker
5. Choose your favorite theme from the theme section in settings
6. Enter prompts in the input field and tap Send

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