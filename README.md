# claudeAssist

<a href="https://github.com/h0t5p0t18/claudeAssist/releases/latest">
  <img src="https://img.shields.io/github/v/release/h0t5p0t18/claudeAssist" alt="current claudeAssist release" title="current claudeAssist release" />
</a>

[![Build apk](https://github.com/h0t5p0t18/claudeAssist/workflows/build.yml/badge.svg)](https://github.com/h0t5p0t18/claudeAssist/workflows/build.yml)

[![Get it on Obtanium](https://img.shields.io/badge/Get%20it%20on-Obtanium-blue?logo=android)](https://github.com/h0t5p0t18/claudeAssist)

A lightweight, privacy-friendly **WebView wrapper for [claude.ai](https://claude.ai)** for Android.

Inspired by and based on the approach and style of [gptAssist](https://github.com/woheller69/gptAssist) and [geminiAssist](https://github.com/AcideFluorhydrique/geminiAssist) (both GPLv3).

## Features

- 🔒 **URL blocking**: By default, blocks all domains that are not required for claude.ai itself (trackers, analytics, and third-party advertising networks).
- 🔄 **Toggleable**: Tap the button in the top-right corner to enable or disable blocking if it interferes with a feature such as Google login.
- 👆 **Hideable**: Swipe the button upwards to hide it.
- 🗑️ **Reset**: Long-press the button to clear cookies, cache, and website data (complete logout).
- 📎 **File uploads**: Supports uploading files and images to chats.
- 🎙️ **Microphone support**: For voice features in the web UI.
- 🔐 **No telemetry, no own tracking, no advertising**: Pure FOSS.

## What this app is NOT

This is **not an official Anthropic product**. It is a simple WebView wrapper around the public claude.ai web interface — comparable to a Progressive Web App, packaged as a standalone APK. Login, accounts, and terms of use are governed by claude.ai / Anthropic.

There are other dedicated projects for Claude Code (the coding CLI) — this repository is exclusively for the **claude.ai chat web interface**.

## Building

Requirements: Android Studio (latest version) or Gradle + Android SDK (API 34).

```bash
git clone https://github.com/h0t5p0t18/claudeAssist.git
cd claudeAssist
./gradlew assembleDebug
````

The resulting APK can be found at:

`app/build/outputs/apk/debug/app-debug.apk`

For an unsigned release APK:

```bash
./gradlew assembleRelease
```

## License

GPLv3 — see [LICENSE](LICENSE).

## Contributing

Issues and pull requests are welcome. Please include the Android version and device when reporting bugs.
