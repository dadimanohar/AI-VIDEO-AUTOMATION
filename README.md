# ShortsForge Mobile

**AI-powered long-video to Shorts editor for Android.**

ShortsForge helps you turn a local video into short-form clips with timestamped AI highlight suggestions, transcript import, cloud and offline speech analysis, manual trimming, preview, and MP4 export.

## Android app
- App name: ShortsForge Mobile
- Package: `com.shortsforge.mobile`
- Current version: 1.5.0
- Android minimum: 8.0 (API 26)
- Build: GitHub Actions workflow at `.github/workflows/android-apk.yml`

## Features
- Select a local video and preview it in the editor
- Transcribe with Groq Whisper or OpenAI Whisper
- Analyse timestamped transcripts with Groq, OpenAI, Claude, or local Qwen3
- Import SRT, VTT, and TXT transcripts
- Rank up to eight highlights with hook, category, importance score, explanation, and timestamps
- Automatically select the top suggestion; tap other cards to change selection
- Preview the exact selected clip before export
- Choose 60-second or 90-second AI clip windows and adjust start/end manually
- Export vertical 9:16, square 1:1, or landscape 16:9 MP4
- Burn in optional text overlays and save/share the exported video
- Optional offline Qwen3 0.6B and Whisper Base models (downloaded separately)

## Source-video note
The mobile editor uses a video file selected from the device. YouTube watch URLs are not treated as editable MP4 sources. Use video files you own or have permission to edit.

## Build
Open the repository's Actions tab and run **Build ShortsForge Android APK**, or push a change under `android/` to trigger the workflow.

The release workflow publishes `ShortsForge-Mobile.apk` to the `mobile-latest` release.

## License and rights
Only edit and publish footage you own or are authorized to use. AI-generated clip suggestions do not grant copyright permission.
