# A.SYRIA

A native Android application written in Kotlin with Jetpack Compose.

## Technology

- Kotlin and Android Gradle Plugin
- Jetpack Compose and Material 3
- Navigation Compose
- Room and Preferences DataStore
- Gemini API — pinned to `gemini-3.6-flash`
- WorkManager for scheduled notifications
- Adhan library for prayer-time calculations

## Project setup

Open the repository root in Android Studio and allow Gradle to synchronize.

Add the Gemini API key in the app's Settings screen. The app uses this one key for chat, cybersecurity assistance, academy content generation, link analysis, notifications, and UI customization.

Prayer-time coordinates are estimated through an HTTPS IP-based network lookup. The app does not request Android location permission and does not use GPS or fused-location services. If the lookup fails, calculations fall back to Damascus coordinates.

## Verification

Run the Gradle tasks `testDebugUnitTest` and `assembleDebug` from Android Studio's Gradle tool window or the Gradle integration in your development environment.

## Security notes

Do not commit API keys, signing keystores, or generated signing files. Release signing values must be supplied through protected environment variables or CI secrets. Removing a previously committed signing file does not remove it from Git history; rotate any release key that may have been exposed.
