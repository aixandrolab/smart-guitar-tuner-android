# Smart Guitar Tuner Android <sup>v0.1.1</sup>


[![GitHub top language](https://img.shields.io/github/languages/top/aixandrolab/smart-guitar-tuner-android)](https://github.com/aixandrolab/smart-guitar-tuner-android)
[![GitHub license](https://img.shields.io/github/license/aixandrolab/smart-guitar-tuner-android)](https://github.com/aixandrolab/smart-guitar-tuner-android/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/aixandrolab/smart-guitar-tuner-android)](https://github.com/aixandrolab/smart-guitar-tuner-android/)
[![GitHub stars](https://img.shields.io/github/stars/aixandrolab/smart-guitar-tuner-android?style=social)](https://github.com/aixandrolab/smart-guitar-tuner-android/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/aixandrolab/smart-guitar-tuner-android?style=social)](https://github.com/aixandrolab/smart-guitar-tuner-android/network/members)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org)

---

## ⚠️ Disclaimer

**By using this software, you agree to the full disclaimer terms.**

**Summary:** Software provided "AS IS" without warranty. You assume all risks.

**Full legal disclaimer:** See [DISCLAIMER.md](DISCLAIMER.md)

---

A professional guitar tuning application that allows you to create custom tunings and record your own string reference sounds. Perfect for guitarists using alternate tunings (Drop D, Open G, DADGAD, etc.) or wanting personalized practice tools.

## 🎸 Features

- **Custom Tunings** – Create, edit, and manage unlimited custom tunings
- **Record Your Own Sounds** – Record 6 seconds of each string for perfect reference
- **Built-in Default Sounds** – Ready to use out of the box with standard tuning
- **Audio Playback** – Loop, pause, seek, and replay recorded sounds
- **Re-record Anytime** – Update individual strings without redoing everything
- **Storage Persistence** – Tunings survive app updates and reinstallation

## 📱 Screenshots

| Main Screen                                                                                                      | String Tuning                                                                                                        | Audio Player                                                                                                       | Record Screen                                                                                                        |
|------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| ![Main Screen](https://github.com/aixandrolab/smart-guitar-tuner-android/raw/master/data/images/main_screen.png) | ![String Tuning](https://github.com/aixandrolab/smart-guitar-tuner-android/raw/master/data/images/string_tuning.png) | ![Audio Player](https://github.com/aixandrolab/smart-guitar-tuner-android/raw/master/data/images/audio_player.png) | ![Record Screen](https://github.com/aixandrolab/smart-guitar-tuner-android/raw/master/data/images/record_screen.png) |

## 🚀 Quick Start

### For Users

1. **Create a tuning** – Tap "Add New Tuning" and enter a name (e.g., "Drop D")
2. **Record strings** – Select your tuning, tap "Record" for each string, and play your guitar
3. **Play back** – Use the audio player to listen and verify your recordings
4. **Re-record** – Not satisfied? Tap "Re-record" to capture a better take

### For Developers

#### Prerequisites

- Android Studio Ladybug
- Android SDK 24+ (Android 7.0)
- JDK 11 or higher

#### Building from Source

```bash
# Clone the repository
git clone https://github.com/aixandrolab/smart-guitar-tuner-android.git
cd smart-guitar-tuner-android
```

- Build debug APK (Build/Generate App Bundles or APKs/Generate APKs)
- Install on connected device

#### Project Structure

```
app/
├── src/main/java/com/example/smart_guitar_tuner_android/
│   ├── MainActivity.kt           # Tuning list management
│   ├── StringTunerActivity.kt    # Per-tuning string management
│   ├── RecordingActivity.kt      # Audio recording (6 seconds)
│   ├── AudioPlayerActivity.kt    # Playback with loop/seek
│   ├── SettingsManager.kt        # File-based tuning storage
│   └── ...
├── src/main/res/                 # Layouts, drawables, values
└── src/main/assets/              # Default string sounds (1_string.wav, etc.)
```

## 📂 Storage Location

Recorded tunings are stored in:
```
Documents/smart-guitar-tuner/tuners/[Tuning Name]/
├── string_1.wav
├── string_2.wav
└── ... string_6.wav
```

## 🔧 Permissions

- **RECORD_AUDIO** – Required for recording guitar strings (Android 8.0+)
- **MANAGE_EXTERNAL_STORAGE** – Required for Android 11+ to ensure tunings survive app updates

The app requests these permissions at runtime with clear explanations.

## 🛠️ Technical Details

- **Audio Format**: PCM 16-bit, 44.1 kHz, mono
- **Recording Duration**: 6 seconds per string
- **Playback Controls**: Play/Pause, Stop, Loop, Seek bar
- **Persistence**: WAV files stored in external storage (Documents directory)

## 📄 License BSD-3 Clause [LICENSE](LICENSE)

## 👤 Author

**Alexander Suvorov (aixandrolab)**
- GitHub: [@aixandrolab](https://github.com/aixandrolab)
- Project Repository: [smart-guitar-tuner-android](https://github.com/aixandrolab/smart-guitar-tuner-android)

## 🙏 Acknowledgments

- Built with [Material Design Components](https://material.io/develop/android)
- Audio processing using Android's native `AudioRecord` and `MediaPlayer`

## ⭐ Support

If you find this project useful, please consider:
- Starring the repository on GitHub
- Reporting issues or suggesting features
- Sharing with fellow guitarists

---

**Made with 🎸 for guitarists who love alternate tunings**
