# IPlayer - Complete Android Video & Audio Player

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/MinSDK-24-orange.svg" alt="MinSDK">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

Full-featured video and audio player for Android built with Kotlin and ExoPlayer.

## ✨ Features

### Video Player
- 📹 **Multiple View Modes**: List view with thumbnails and Grid view
- 🎬 **Advanced Playback Controls**:
  - A-B Repeat for looping sections
  - Variable playback speed (0.25x - 2.0x)
  - Repeat modes (Off, One, All)
  - Picture-in-Picture support
- 🎮 **Gesture Controls**:
  - Double tap sides to skip ±10 seconds
  - Swipe left for brightness control
  - Swipe right for volume control
  - Single tap to show/hide controls
- 🔒 **Screen Controls**:
  - Lock/unlock orientation
  - Manual rotation
  - Auto-rotate support
- 📊 **Video Information**: Resolution, duration, file size
- 🖼️ **Thumbnail Generation**: Automatic video preview thumbnails

### Audio Player
- 🎵 Support for MP3, WAV, FLAC, AAC, OGG formats
- 🎨 Album art display
- 📱 Background playback with notification controls
- 🔀 Shuffle and repeat modes

### Organization
- 📂 **Smart Sorting**:
  - Name (A-Z, Z-A)
  - Date added (Newest/Oldest)
  - Duration (Longest/Shortest)
  - File size (Largest/Smallest)
- 🎯 **Playlists**: Create and manage custom playlists
- 🔍 **Search**: Quick media search
- ⭐ **Favorites**: Mark favorite videos/audio

### Notification & Service
- 🔔 Media notification with playback controls
- ⏯️ Play, Pause, Next, Previous from notification
- 🎧 Background audio playback service

## 🛠️ Tech Stack

- **Language**: Kotlin 100%
- **UI**: Material Design 3, View Binding
- **Player**: Media3 ExoPlayer
- **Architecture**: MVVM pattern
- **Async**: Kotlin Coroutines
- **Storage**: SharedPreferences
- **Image Loading**: Glide

## 📦 Dependencies

```gradle
// Media3 ExoPlayer
androidx.media3:media3-exoplayer:1.2.1
androidx.media3:media3-ui:1.2.1
androidx.media3:media3-session:1.2.1

// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-*:2.7.0

// Image Loading
com.github.bumptech.glide:glide:4.16.0
🚀 CI/CD
Automatic APK builds via GitHub Actions:
✅ Auto-generates launcher icons
🔧 Builds debug APK on every push
📥 Downloads available in Actions artifacts
📱 Installation
Clone the repository:
git clone https://github.com/YOUR_USERNAME/IPlayer.git
cd IPlayer
Open in Android Studio
Build and run:
./gradlew assembleDebug
Or download the latest APK from GitHub Actions
🎯 Usage
Video Playback
Grant storage permissions
Videos automatically appear in library
Tap any video to play
Use gestures for quick controls
Access advanced features from player menu
Audio Playback
Switch to Audio tab
Tap any song to play
Control from notification or lock screen
Playlists
Go to Playlists tab
Create new playlist
Add media items
Play entire playlist
🔧 Configuration
Settings available in-app:
Default view mode (List/Grid)
Default sort order
Default playback speed
Orientation lock preference
📸 Screenshots
Coming soon
🤝 Contributing
Contributions welcome! Please:
Fork the repository
Create feature branch
Commit changes
Push to branch
Open Pull Request
📄 License
MIT License - see LICENSE file
👨‍💻 Author
Your Name - GitHub Profile
🙏 Acknowledgments
ExoPlayer by Google
Material Design Components
Android Jetpack Libraries
Made with ❤️ in Madagascar