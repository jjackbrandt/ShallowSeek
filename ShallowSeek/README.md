# ShallowSeek Android App

A modern Android app that connects to a Node.js server on your local machine, which in turn communicates with Ollama running the DeepSeek model.

## Overview

ShallowSeek is a clean, user-friendly Android application that allows you to interact with DeepSeek, a powerful large language model. The app sends your prompts to a Node.js server running on your MacBook, which then forwards them to Ollama's API.

## Features

- Clean, intuitive UI using Jetpack Compose and Material 3
- Configurable server address to connect to your local machine
- Real-time text generation with the DeepSeek model
- Error handling and loading indicators
- Modern Android architecture with MVVM pattern

## Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 31+
- Node.js server running (see the Server directory)
- Ollama running with DeepSeek model pulled

## Setup

### Server Setup

1. Navigate to the Server directory
2. Run `npm install` to install dependencies
3. Start Ollama and pull the DeepSeek model: `ollama run deepseek`
4. Start the server: `npm start`
5. Note your computer's IP address on your local network

### Android App Setup

1. Open the project in Android Studio
2. Build and run the app on your device or emulator
3. In the app, tap the settings icon and update the server address to your computer's IP address (e.g., `http://192.168.1.100:3000`)

## Architecture

The app follows modern Android architecture principles:

- **UI Layer**: Jetpack Compose with Material 3 design
- **Logic Layer**: ViewModel manages UI state and business logic
- **Data Layer**: Repository pattern for data operations
- **Network Layer**: Retrofit for API communication

## Network Security

For development purposes, the app allows cleartext traffic to the local server. In a production environment, you would want to use HTTPS.

## Roadmap

Here are planned features and improvements for future versions:

### Continuous Chat Feature

A major planned enhancement is implementing a continuous chat feature similar to other LLM applications. This would involve:

1. **Chat History Management**:
   - Storing messages in a chat history data structure
   - Implementing a RecyclerView or LazyColumn to display messages with different styles for user and AI
   - Adding timestamp information to messages

2. **Context Window Management**:
   - Maintaining conversation context by sending previous messages to the API
   - Implementing token counting to stay within model context limits
   - Adding options to clear context or start new conversations

3. **UI Improvements**:
   - Message bubbles with visual differentiation between user and AI
   - Typing indicators during generation
   - Ability to copy individual messages
   - Support for markdown formatting in responses

4. **Implementation Plan**:
   - Create a Message data class with fields for content, sender, timestamp
   - Update MainViewModel to store List<Message> instead of single response
   - Modify ApiRepository to construct prompts that include conversation history
   - Create new UI components for chat bubbles and message list
   - Add storage for persisting conversations between sessions

Other planned features include:
- Multiple chat sessions/conversations
- Image generation support
- Speech-to-text input
- Local embedding of models
- Export/import conversation history

## License

This project is licensed under the MIT License - see the LICENSE file for details.