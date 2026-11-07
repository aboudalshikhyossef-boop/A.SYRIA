<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# A.SYRIA SOVEREIGN OS v4.0.0

A cybersecurity-focused Android app with Gemini AI integration, prayer times, biometric authentication, and an encrypted vault.

## Quick Setup

### 1. Clone the repo
```bash
git clone https://github.com/abdelkadersovereign-commits/V4-.git
cd V4-
```

### 2. Run the setup script
```bash
bash setup.sh
```
This will:
- Decode `debug.keystore.base64` → `debug.keystore` automatically
- Create a `.env` file from the template

### 3. Add your Gemini API key
Open `.env` and replace `MY_GEMINI_API_KEY` with your real key:
```
GEMINI_API_KEY=your_actual_key_here
```
Get a free key from: https://aistudio.google.com/app/apikey

### 4. Open in Android Studio
- Select **Open** and choose this project directory
- Allow Gradle to sync
- Run on an emulator or physical device (Android 8.0+ required)

## Features
- 🔐 Biometric authentication on launch
- 🕌 Real-time prayer times (GPS-based, Damascus fallback)
- 🧠 Gemini AI cybersecurity academy
- 🔒 Encrypted vault with AES-128
- 📡 Sensor-powered parallax UI
- 🌙 Full Arabic/English support

## Fixes Applied (v4.0.1)
- Fixed: Double sensor registration causing performance issues
- Fixed: Location client now catches all exception types (not just SecurityException)
- Fixed: Keystore setup automated via `setup.sh`
