# ShortsForge Mobile

**AI-powered long-video to Shorts editor for Android.**  
Turn a long interview, podcast, lecture, or other video file into short clips by finding useful moments in its timestamped transcript, reviewing the suggested time ranges, and exporting an MP4.

[Download the latest Android APK](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk) · [Latest build status](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44460324274)

| App detail | Value |
|---|---|
| App name | ShortsForge Mobile |
| Current release | 1.13.1 |
| Version code | 18 |
| Android package | `com.shortsforge.mobile` |
| Minimum Android version | Android 8.0 (API 26) |
| APK size | About 14 MB |
| Export | MP4 (H.264 video + AAC audio) |
| Build type | Debug-signed APK from GitHub Actions |
| Release APK size | 13,925,543 bytes (~13.9 MB) |
| Release APK SHA-256 | `9d22d7b8b1c495a4472dd323dc76dabf6a163da013a8127c34dc8ff5ff2be1c6` |

> **Important source-video note (1.13.0):** Paste a YouTube URL and tap **Send URL to YTDLnis for video download** to hand it to the separately installed YTDLnis app using its documented Android intent. Review and start the download in YTDLnis. When it finishes, share the saved video to ShortsForge or choose it from Downloads. ShortsForge does not bundle YTDLnis/yt-dlp or extract YouTube streams. YouTube preview and timestamp markers remain available inside ShortsForge. Only download and edit videos where you have the necessary rights and the source service permits it.

---

# ENGLISH

## 1. Project overview

ShortsForge Mobile is an Android-first video clipping studio for creators, students, podcasters, educators, and editors who work with long-form recordings.

The app analyses the full timestamped transcript in overlapping sections and creates a chronological Video Topic Map. It builds a chronological topic map without a fixed global 60-topic cutoff, including introductions, background, subtopics, question-and-answer exchanges, stories, announcements, explanations, examples, debates, predictions, lessons and conclusions. Every topic card has a title, exact suggested time range, category, importance score and explanation. Tap a card to select its timestamps or play that topic, queue individual topics, or export multiple topics as separate Shorts.

The app is a **clip-selection and export tool**. It is not currently a full video-generation suite, a YouTube downloader, or an automatic YouTube publishing service.

## 2. Project goal and aim

The main goal is to reduce the time spent manually searching long recordings for useful short-form moments.

The intended workflow is:

1. Select a local video file, or import a creator-authorized direct HTTPS video-file URL. A YouTube URL can be previewed and used to mark timestamps, but it is not itself an editable source file.
2. Generate a timestamped transcript using cloud Whisper or on-device Whisper, or import/paste an existing timestamped transcript.
3. Ask an AI model to map the transcript section by section and identify distinct topics across the recording.
4. Review the chronological topic cards and their timestamps, category, importance score, and explanation.
5. Select a topic to load its exact Start/End values, or optionally expand the selected topic to 60 or 90 seconds.
6. Preview the selected time range and adjust it manually when needed.
7. Choose an output aspect ratio, optionally add a text overlay, and export an MP4.
8. Find the exported file in Movies/ShortsForge and share it using the Android share sheet.

## 3. Features carried forward from version 1.10.0

### Source video
- Select a video from device storage using Android's file picker.
- Import a creator-authorized direct HTTPS video-file URL, after confirming that you own the video or have editing permission.
- Preview the selected source video in the app, and show its filename and detected duration.
- The source file is not intentionally overwritten by the export process.
- Supported video formats depend on the Android device's media decoders.

### YouTube URL preview and authorized direct-file import (introduced in 1.10.0)
- Paste a standard YouTube watch URL, Shorts URL, or youtu.be URL and open it in the in-app embedded YouTube player.
- Play, pause, and seek using the embedded player controls.
- Set an IN marker and an OUT marker at the current player position, or manually edit the marker times in seconds.
- Return the markers to the main editor. When you subsequently select/import the corresponding video file, the saved times are prefilled if they fit that file's duration. Make sure the previewed and imported files are the same recording.
- YouTube links are **preview and timestamp-marker only**. The app does not extract the YouTube video/audio stream, auto-fetch public captions, or send the watch URL to the AI transcription workflow.
- Paste a creator-authorized direct HTTPS video-file URL to import the actual media source into app-private storage.
- Direct-file import supports MP4, MOV, M4V, WebM, MKV, and 3GP when the server returns a supported video MIME type or recognizable file extension.
- The user must confirm ownership or permission before import. Maximum download size: 1 GB.
- Share pages (for example, a Google Drive/Dropbox HTML preview page) are not direct video files and are not accepted. Use a direct media-file URL that returns video bytes.
- Links that redirect to YouTube or YouTube's video-delivery hosts are rejected.

### Transcription and transcript import
- Automatic cloud transcription using Groq Whisper or OpenAI Whisper.
- Automatic on-device transcription using Whisper Base after the offline model is downloaded.
- Paste a timestamped transcript directly into the transcript editor.
- Import subtitle/transcript files with SRT, VTT, or TXT extensions.
- SRT/VTT cue timestamps are converted into timestamped transcript lines.
- TXT files should contain usable timestamps if the AI is expected to identify exact moments.

### Full-video topic discovery — Video Topic Map (introduced in 1.10.0)
- Analyse the entire timestamped transcript in overlapping chunks instead of asking for only the eight most important highlights.
- Cloud AI processes transcript windows of 80 non-empty lines with a 16-line overlap. It requests up to 16 topic candidates per window, then merges duplicate/overlapping results.
- Offline Qwen3 processes windows of 36 non-empty lines with an 8-line overlap and requests up to 10 topic candidates per window.
- Results are sorted chronologically by source-video start time, not ranked into a viral-first order.
- No fixed global 60-topic cutoff is applied. Cloud analysis requests up to 16 topic candidates per transcript window; offline Qwen3 requests up to 10 per window. The final number depends on transcript content, model output, and successful processing.
- Topic types include introduction, background, each distinct subject/subtopic, complete Q&A, stories, examples, announcements, explanations, opinions, debates, predictions, practical advice and conclusions.
- Each card shows a specific title, exact start/end time, category, importance (1–10 when returned), and a short explanation of what is discussed.
- Tap a card to load that topic's exact timestamps into the Start/End fields.
- Tap **Play this topic** to play that section in the source-video preview and stop at the topic's end time.
- Use **Add this topic to Shorts queue** on individual cards, or **Add all detected topics to Shorts queue** to queue every valid topic.
- Export queued topics as separate MP4 Shorts, or send selected topics together into the Studio timeline.
- Overlapping duplicate candidates from adjacent transcript chunks are merged before display.

Topic mapping is transcript-based, not visual scene understanding. Accuracy depends on transcript quality, timestamp accuracy and the AI model. There is no fixed global 60-card cap, but the map still cannot guarantee detection of every tiny conversational shift; results depend on transcript quality, timestamps, model output, and successful processing. Cloud topic mapping makes multiple AI requests for long transcripts, so provider token/request charges can be higher than the previous single-request, eight-highlight approach. The smaller offline model may miss topics that a larger cloud model identifies.



### Clip editing and preview
- Tap a highlight card to load its exact Start and End timestamps. The app does not silently replace the topic's time range with a generic 60/90-second window.
- Preview the selected start-to-end range before export.
- Manually edit the Start and End values in seconds.
- Choose an optional 60-second or 90-second expansion around the currently selected topic using **Expand selected topic to 60s / 90s**.
- Quick duration buttons: 15s, 30s, 45s, and 60s.
- A single quick export is limited to 180 seconds (3 minutes).
- Each queued highlight is checked against its own valid timestamp range before a batch export starts.

### Export each queued topic as a separate Short (introduced in 1.10.0)
- Add two or more topic cards to **Shorts queue** using their checkboxes.
- Tap **Export N queued topics as separate Shorts**.
- Confirm the batch. ShortsForge exports one MP4 for each selected topic using that card's exact start/end timestamps.
- Every output uses the current main-editor aspect ratio and optional text overlay.
- Each video is saved separately in **Movies / ShortsForge**, with a filename based on its topic title.
- The app shows progress such as “Exporting topic 2/5” and reports whether all or only some files were saved.
- Use **Share saved Shorts** to share the completed files together where the target Android app supports multiple-video sharing.
- Each individual Short can be up to 180 seconds; the total duration of all separate Shorts is not combined into one 180-second limit.

### ShortsForge Studio timeline editor
Open **Studio Editor · Timeline, split, speed, text, audio** after selecting a highlight. The Studio button also accepts all queued topics and places them together in one editing timeline.
- Preview the source video in a dedicated dark editing workspace.
- Work with an ordered timeline of clip segments from the same source video. Queued topic titles appear on their timeline clips.
- Split, delete, move, reorder, and undo timeline segments.
- Trim the selected segment using Start and End fields, and scrub it with the playhead.
- Change playback/export speed: 0.5×, 0.75×, 1×, 1.25×, 1.5×, or 2×.
- Choose vertical 9:16, square 1:1, or landscape 16:9 output.
- Add a text overlay (up to 140 characters), choose top/centre/bottom position, clean/bold/large style, and white/yellow/cyan text.
- Mute the source video's audio or add one looping background music track. The selected music track replaces the source audio; separate track mixing and volume automation are not provided.
- Export the ordered timeline as one MP4 (H.264/AAC). The combined Studio timeline is limited to 180 seconds.
- Use separate batch export when you want each topic to become its own MP4 rather than combining them into one timeline.
- After export, share the MP4 or open it again in Studio Editor for another edit.

This is a native timeline-editing stage, not full CapCut/KineMaster feature parity. Timeline segments currently reference ranges from one selected source video; this is not a multi-source, multi-track video compositor.


### Export
- Vertical 9:16 output for Shorts-style video: 720 × 1280.
- Square 1:1 output: 720 × 720.
- Landscape 16:9 output: 1280 × 720.
- H.264 video and AAC audio in an MP4 container.
- Optional text overlay burned into the exported clip (up to 140 characters).
- Save the result in Movies/ShortsForge on supported Android versions.
- Open the Android share sheet after export so you can share the MP4 with an app of your choice.
- Cancel an export while it is running.

### AI providers and offline processing
- Groq Cloud AI.
- OpenAI Cloud AI.
- Anthropic Claude.
- Offline local AI using Qwen3 0.6B Q4 and Whisper Base.
- Cloud API keys are encrypted locally using Android Keystore with AES-GCM.
- When a cloud network connection fails and the required local models are already present, the app can switch to local processing.

## 4. Requirements

### Android device
- Android 8.0 (API 26) or later.
- Enough free storage for the source video, export, temporary processing, and (optionally) offline AI models.
- Device support for the selected video's audio/video codecs.
- Internet access for cloud AI and the first offline-model download.

### Cloud AI
You need an API key from the provider you select in Settings:
- Groq: highlight analysis and automatic transcription.
- OpenAI: highlight analysis and automatic transcription.
- Claude: transcript analysis. Claude mode does not automatically transcribe the selected video in the current app; first create/import a timestamped transcript using another route.

Cloud services may charge for API usage. Check the provider's current pricing and usage terms before using them.

### Offline AI
To use offline transcription and highlight analysis:
- Download Qwen3 0.6B Q4 (approximately 429 MB).
- Download Whisper Base (approximately 148 MB).
- The combined download is approximately 577 MB.
- The app checks for at least 700 MB of free storage before downloading both models.
- Download the models once while online. They are stored in the app's private storage and are not bundled in the APK.

Actual speed and model quality depend on the phone's CPU, RAM, thermal limits, Android version, and available storage. Offline performance has not been benchmarked on every Android device.

## 5. Download and install the APK

1. Open the [ShortsForge Mobile release download](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk) on your Android device.
2. Download the file named **ShortsForge-Mobile.apk**.
3. Open the downloaded APK from the browser's download notification or the Downloads/Files app.
4. If Android asks, allow the browser or file manager you are using to install unknown apps.
5. Review the Android installation screen and tap **Install**.
6. Open **ShortsForge Mobile**.

This APK is debug-signed and is distributed directly from the project's GitHub release, not through Google Play.

**Updating an older build:** If Android refuses to install the new APK over an older build because of a signing-certificate conflict, you may need to uninstall the old app and then install this APK. Uninstalling removes app-private data, including saved API keys and downloaded offline models. Keep your API keys available and be prepared to download the offline models again if that happens.

## 6. Quick start

### Option A — Cloud AI (recommended for first use)
1. Install and open ShortsForge Mobile.
2. Tap **Settings**.
3. Select **Groq · Cloud AI** or **OpenAI · Cloud AI**.
4. Paste the matching API key and tap **Save AI settings**.
5. Return to the editor.
6. Tap **Choose video** and select a local video file.
7. Wait for the filename, duration, and video preview to appear.
8. Tap **Analyze entire video · transcribe + map every topic**.
9. Wait for transcription and highlight analysis to complete.
10. Review the suggestion cards. The first topic is selected automatically, and its exact timestamps are loaded into Start/End.
11. Tap other cards to preview their exact topic time ranges.
12. Tick **Add this topic to Shorts queue** on every topic you want to turn into a Short.
13. Choose the aspect ratio and optional text overlay.
14. Tap **Export N queued topics as separate Shorts** to create one MP4 per topic, or tap **Open Studio Editor · N topic(s) as one timeline** to edit them together.
15. For a separate batch, confirm the export and keep the app open while each topic is rendered and saved.
16. Share the saved Shorts from the completion dialog, or open Studio Editor to make timeline edits.

### Option B — Offline AI
1. Connect to the internet for the first model download.
2. Open **Settings**.
3. In **OFFLINE MODEL MANAGER**, tap **Download offline AI models · ~577 MB**.
4. Keep the app open while Qwen3 and Whisper Base download.
5. Confirm that both model statuses show **Downloaded**.
6. Select **Offline local AI · Qwen3 + Whisper** in **AI MODE**.
7. Tap **Save AI settings** and return to the editor.
8. Choose a local video and tap **Auto-transcribe + find clips**.
9. Wait while Whisper transcribes locally and Qwen3 analyses the timestamped transcript.
10. Select a suggestion, preview it, edit the range if required, and export.

After the models are downloaded, local transcription and transcript analysis can run without sending the audio/transcript to a cloud AI service. Internet is still needed for any later model downloads or cloud-provider use.

### Option C — Use an existing transcript
1. Choose the source video.
2. Paste a timestamped transcript into **TRANSCRIPT**, or tap **Import subtitles / transcript (SRT, VTT, TXT)**.
3. Select Groq, OpenAI, or Claude in Settings and save the relevant API key.
4. Return to the editor and tap **Analyze all topics in transcript**.
5. Review the ranked suggestions, choose one, preview the selected range, and export.

### Option D — YouTube preview + authorized video source
1. Paste a YouTube watch, Shorts, or youtu.be link in **YOUTUBE VIDEO URL**.
2. Tap **Preview YouTube URL + mark clip timestamps**.
3. Play or seek in the embedded player. Tap **Set IN at playhead** and **Set OUT at playhead**, or edit the seconds fields manually.
4. Tap **Use these clip markers** to return the timestamps to the main editor.
5. Obtain the same recording as a local file or a direct HTTPS video-file URL from the creator/rightsholder.
6. For a direct URL, paste it into **AUTHORIZED DIRECT VIDEO FILE URL**, tap **Import authorized video URL**, confirm permission, and wait for download/validation.
7. Check that the imported file is the same recording you previewed. The saved marker times are prefilled when they fit the imported file's duration.
8. Run **Auto-transcribe + find clips** or import a transcript, then use Studio Editor and export as usual.

**Important:** YouTube preview does not make the YouTube watch URL an editable source. AI transcription and MP4 export require the local or authorized direct media file. The app also does not automatically fetch public YouTube captions.

## 7. Detailed guide to the editor

### Step 1: Select the source video
Use **Choose video** for a local file, or use **AUTHORIZED DIRECT VIDEO FILE URL** to import a creator-authorized HTTPS media-file link. The app displays the filename and duration after validating the media. A YouTube URL can be opened through **Preview YouTube URL + mark clip timestamps**; it provides an embedded preview and time markers only, not an editable video file.

### Step 2: Create a transcript
Choose one of these approaches:
- Cloud: Groq or OpenAI can extract audio from the selected video and request timestamped transcription.
- Offline: Whisper Base transcribes the audio locally after the model is installed.
- Existing transcript: paste it into the text field or import an SRT/VTT/TXT file.

Cloud transcription sends the extracted audio to the selected provider. Offline Whisper processes the audio on the device.

### Step 3: Find important moments
Tap **Auto-transcribe + find clips** or **Analyze pasted transcript**. The AI returns suggestions with timestamps and a brief explanation. A transcript without reliable timestamps may not produce usable clip suggestions.

### Step 4: Select and queue highlight topics
The first suggestion is automatically selected. Its card has a highlighted background and a **SELECTED** marker. Tap another card to load that topic's exact start/end timestamps into the main editor. The displayed timestamps are the topic range, not a generic 60/90-second replacement.

Tick **Add this topic to Shorts queue** on each topic you want. You can queue several topics and export each one as a separate MP4, or send them together to Studio as a single timeline. The separate batch export preserves each topic's own start/end range.

**Optional:** Use **Expand selected topic to 60s / 90s** to create a longer window around the currently selected topic. Preview the resulting range and adjust Start/End manually if needed.

### Step 5: Preview and edit
Tap **Preview selected clip** to play from the chosen Start time and stop at the chosen End time. You can also:
- Type Start and End times in seconds.
- Use 15s, 30s, 45s, or 60s quick duration buttons.
- Change between 60-second and 90-second AI selection windows.

The time-range preview checks the chosen source segment. The final crop/aspect-ratio treatment and text overlay are applied during export, so review the exported MP4 as well.

### Step 6: Set output and export
Choose:
- Vertical 9:16 for a Shorts-style layout.
- Square 1:1.
- Landscape 16:9.

Add optional overlay text (maximum 140 characters), or leave it blank. For one selected clip, tap **Export Short**. For multiple queued topics as independent Shorts, tap **Export N queued topics as separate Shorts** and confirm. Each output is saved separately in Movies/ShortsForge. Keep the app open until the batch finishes; you can then share all saved outputs from the completion dialog. Studio Editor can instead combine queued topics into one timeline, with a 180-second combined export limit.

## 8. AI modes and model details

| Mode | What it does | Internet/API key |
|---|---|---|
| Groq | Groq chat model ranks highlights; Groq Whisper transcribes audio | Required |
| OpenAI | OpenAI chat model ranks highlights; Whisper API transcribes audio | Required |
| Claude | Claude analyses a supplied timestamped transcript | Required |
| Offline local AI | Whisper Base transcribes; Qwen3 0.6B Q4 ranks transcript moments | Models must be downloaded once |

Current model identifiers in the app source:
- Groq highlight analysis: openai/gpt-oss-120b.
- Groq transcription: whisper-large-v3-turbo.
- OpenAI highlight analysis: gpt-4o-mini.
- OpenAI transcription: whisper-1.
- Claude transcript analysis: claude-haiku-4-5-20251001.
- Local highlight analysis: Qwen3-0.6B-Q4_0.gguf.
- Local transcription: ggml-base.bin (Whisper Base).

Model names, API availability, usage limits, and prices may change. The identifiers above describe the current code, not a guarantee that a provider will keep the same API or pricing.

## 9. Transcript format tips

The most reliable transcript contains a timestamp for every spoken segment. For example:

    [00:01.20 - 00:04.50] Welcome to today's discussion.
    [00:04.60 - 00:09.10] Today we are talking about artificial intelligence.

SRT/VTT files that contain normal subtitle cue times are parsed into this style. A plain TXT file is retained as text; include timestamps in the TXT content if you want the AI to return clip times.

The transcript should:
- Match the selected source video.
- Keep timestamps in order.
- Include enough of the question and answer to preserve meaning.
- Avoid missing or invented timestamps.

## 10. Data handling and privacy

- Video selection and MP4 export are performed on the Android device.
- YouTube playback is displayed using the embedded YouTube player. Direct video-file URLs are requested over HTTPS and the returned media is stored in the app's private storage for editing.
- Cloud transcription sends extracted audio to the provider you selected.
- Cloud highlight analysis sends transcript text to the selected AI provider.
- Local Whisper and Qwen3 inference run on the device after the model files are downloaded.
- The app's cloud API keys are stored in app preferences encrypted using AES-GCM and Android Keystore.
- Offline model files are stored in the app's private storage.
- The current app does not provide account-based cloud project sync or a remote media library.

Review the selected AI provider's privacy terms before sending private, confidential, or personal recordings to a cloud API. Do not put API keys in source code, README files, screenshots, or public issue reports.

## 11. Current limitations — what is not included

The following are **not** current 1.13.0 features:
- Native YouTube stream extraction/downloading inside ShortsForge as an editable MP4/audio source. Version 1.12.0 can hand a pasted URL to the separately installed YTDLnis app; the user completes the download there and then shares/selects the saved file in ShortsForge. A YouTube URL by itself is not an editable media file.
- Automatic downloading of public YouTube caption tracks or transcripts.
- Importing Google Drive/Dropbox share-page URLs. The importer requires a direct HTTPS video-file response.
- Automatic YouTube channel upload, scheduling, or publishing.
- Automatic YouTube caption-track downloading.
- AI-generated animated word-by-word subtitles or karaoke captions.
- Automatic B-roll, images, scene generation, or presenter generation.
- Full multi-source, multi-layer CapCut/KineMaster editing: picture-in-picture tracks, transitions, keyframes, filters, advanced colour grading, waveform editing, independent audio volume automation, and frame-by-frame professional tools.
- Automatic video title, description, hashtags, or thumbnail generation.
- A server-side user account, cloud project sync, or team collaboration.

Studio Editor supports a styled static text overlay; it is not an animated subtitle editor. Studio timelines currently arrange ranges from one selected source video and support one added music track that replaces the source audio. They do not provide independent multi-track audio mixing. The AI analyses transcript text; it does not currently perform visual scene understanding or face tracking.

## 12. Future roadmap — ideas, not yet implemented

Potential future improvements, subject to design and testing:
- Animated word-level captions with editable styles and positioning.
- Better crop controls, subject-aware reframing, and safe-area guides.
- Advanced multi-layer timeline editing, transitions, keyframes, filters, independent audio mixing, and frame-accurate controls.
- Saved editing projects and export history.
- Audio cleanup and licensed background-music workflows.
- More language controls for transcription and transcript analysis.
- Optional integrations for publishing, only through supported and authorized platform APIs.

These items are plans/ideas only. They should not be interpreted as features already present in the APK.

## 13. Troubleshooting

**The app says an API key is missing**  
Open Settings, select the provider you want, enter that provider's own API key, tap Save AI settings, and return to the editor.

**Claude will not transcribe my video**  
That is expected in this version. Claude analyses a timestamped transcript. Use Groq/OpenAI or local Whisper to create the transcript first, or import/paste one.

**No highlight suggestions appear**  
Check that the transcript is not empty and includes reliable timestamps. Confirm that it belongs to the selected video. Try a longer transcript or a cloud model if the small local Qwen model does not return useful results.

**Offline AI asks me to download models**  
Open Settings → OFFLINE MODEL MANAGER and download both files. Make sure there is at least 700 MB of free storage and a stable internet connection.

**The video cannot be opened or exported**  
The file may use a codec unsupported by the Android device, may not contain an audio track, or may be damaged. Try an MP4 using H.264 video and AAC audio.

**The export is longer/shorter than expected**  
Check the Start and End fields. AI 60/90-second windows are bounded by the source-video length. Manual exports cannot exceed 180 seconds.

**The exported video looks cropped**  
The app scales to fill the selected aspect ratio. This can crop parts of the source frame. Try a different output ratio and review the final MP4.

**The new APK will not install over the previous version**  
Android may reject an APK signed with a different debug certificate. If that happens, uninstall the older build before installing the new one. This clears app-private API keys and model files.

## 14. Technology stack

- Kotlin and native Android Views.
- Android Activity Result API and Storage Access Framework for file selection.
- Android MediaCodec / MediaExtractor / MediaMuxer for media processing.
- Android MediaMetadataRetriever for source duration.
- AndroidX Media3 ExoPlayer for playback.
- AndroidX Media3 Transformer and Effects for MP4 export and overlay rendering.
- OkHttp for cloud API requests, offline-model downloads, and authorized direct video-file imports.
- Android WebView with the YouTube IFrame Player API for embedded preview and clip timestamp markers.
- Groq, OpenAI, and Anthropic APIs for cloud AI.
- llama.cpp Android integration for local Qwen3 inference.
- whisper.cpp Android integration for local Whisper transcription.
- Android Keystore and AES-GCM for API-key encryption.
- GitHub Actions for APK builds and release publication.

## 15. Repository layout

    .
    ├── android/
    │   ├── app/
    │   │   ├── build.gradle
    │   │   └── src/main/
    │   │       ├── AndroidManifest.xml
    │   │       └── java/com/shortsforge/mobile/
    │   │           ├── MainActivity.kt
    │   │           ├── YouTubePreviewActivity.kt
    │   │           └── StudioEditorActivity.kt
    │   ├── build.gradle
    │   └── settings.gradle
    ├── .github/
    │   └── workflows/
    │       └── android-apk.yml
    └── README.md

## 16. Build the Android app from source

### Build environment
- JDK 17.
- Android SDK platform 35.
- Android SDK Build Tools 35.0.0.
- Gradle 8.9 (the GitHub Actions workflow installs this version).
- Internet access to download Gradle dependencies and Android libraries.

### Build command

From the repository root:

    cd android
    gradle --no-daemon assembleDebug

The generated debug APK is normally located at:

    android/app/build/outputs/apk/debug/app-debug.apk

The GitHub Actions workflow:
1. Checks out the repository.
2. Sets up Java 17.
3. Installs Android SDK platform/build tools.
4. Sets up Gradle 8.9.
5. Runs assembleDebug.
6. Renames the APK to ShortsForge-Mobile.apk.
7. Uploads a workflow artifact.
8. Publishes the APK to the mobile-latest GitHub release.

A push that changes files under android/ or the workflow can trigger the build. The workflow can also be started manually with workflow_dispatch.

## 17. Latest release

- Release: 1.12.0
- Version code: 16
- Build result: successful.
- Build run: [GitHub Actions run 44459451960](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44459451960)
- APK: [ShortsForge-Mobile.apk](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk)
- SHA-256: `ac4a7e39a21ffffaf2d93516f7e89f6d1d9b3ab933a6c1ad511e9be051387e08`

To verify a downloaded file on a computer, calculate its SHA-256 hash and compare it with the value above.

## 18. Copyright, permissions, and project status

Only select, edit, and publish videos that you own, have permission to use, or are otherwise legally allowed to use. A short duration, credit line, or “no copyright intended” disclaimer does not automatically grant rights. AI highlight suggestions do not grant permission to reuse footage.

ShortsForge Mobile does not extract or download YouTube watch-page video streams. YouTube links are supported for embedded preview and timestamp markers only. AI processing and export require a local file or creator-authorized direct video-file source.

The APK build was verified through GitHub Actions. It has not been independently tested on every Android device. Device-specific playback, local-model performance, export time, and compatibility may vary.

No open-source software license is declared in this README. Do not assume that public repository visibility grants permission to reuse this project's source code; add or review a LICENSE file before distributing modified versions.

---

# తెలుగు

## 1. ప్రాజెక్ట్ పరిచయం

ShortsForge Mobile అనేది Android ఫోన్ల కోసం తయారు చేసిన AI ఆధారిత వీడియో క్లిప్పింగ్ యాప్. పొడవైన ఇంటర్వ్యూలు, పోడ్‌కాస్ట్‌లు, లెక్చర్లు లేదా ఇతర వీడియోల నుంచి ఉపయోగకరమైన ముఖ్యమైన భాగాలను గుర్తించి, వాటిని చిన్న వీడియోలుగా ఎడిట్ చేసి MP4గా ఎగుమతి చేయడానికి ఇది సహాయపడుతుంది.

ఈ యాప్ వీడియోలోని మాటలను timestamp ఉన్న transcript రూపంలో విశ్లేషించి గరిష్ఠంగా 8 ముఖ్యమైన moments సూచిస్తుంది. ప్రతి suggestionలో title/hook, timestamp, category, importance score, వివరణ ఉంటాయి. మొదటి suggestion ఆటోమేటిక్‌గా select అవుతుంది. మీరు వేరే suggestion ఎంచుకోవచ్చు, దాని సమయాన్ని preview చేయవచ్చు, Start/End మార్చవచ్చు, చివరికి MP4 export చేయవచ్చు.

ఇది **AI topic discovery, clip selection, editing మరియు MP4 export యాప్**. YouTube URLను appలో preview చేసి IN/OUT timestamps గుర్తించవచ్చు. Version 1.12.0లో ఆ linkను వేరుగా install చేసిన YTDLnisకి పంపి అక్కడ download ప్రారంభించవచ్చు; download పూర్తయ్యాక saved fileను ShortsForgeకి Share చేయాలి లేదా Downloads నుంచి ఎంచుకోవాలి. AI transcription/exportకు actual video file అవసరం.

## 2. ప్రాజెక్ట్ లక్ష్యం

పొడవైన వీడియోలో ముఖ్యమైన భాగాలను చేతితో వెతకడానికి పట్టే సమయాన్ని తగ్గించడం ఈ ప్రాజెక్ట్ ప్రధాన లక్ష్యం.

ఉద్దేశించిన workflow:

1. ఫోన్‌లోని video file ఎంచుకోవాలి లేదా creator అనుమతితో ఉన్న direct HTTPS video-file URL import చేయాలి. YouTube URL ద్వారా preview మరియు timestamps మాత్రమే తీసుకోవచ్చు; అది editable source file కాదు.
2. Groq/OpenAI cloud Whisper లేదా ఫోన్‌లోని local Whisper ద్వారా timestamp transcript తయారు చేయాలి; లేక ఇప్పటికే ఉన్న transcript import/paste చేయాలి.
3. AIతో ముఖ్యమైన moments‌ను rank చేయించాలి.
4. Suggestion cards, timestamps, category, importance score, explanation పరిశీలించాలి.
5. సరైన suggestion ఎంచుకుని దాని చుట్టూ 60 లేదా 90 సెకన్ల clip window తయారు చేయాలి.
6. Exportకు ముందు ఆ time range preview చేసి అవసరమైతే Start/End మార్చాలి.
7. Aspect ratio ఎంచుకుని, కావాలంటే text overlay జోడించి MP4 export చేయాలి.
8. Movies/ShortsForgeలో వీడియోను చూసి Android share menu ద్వారా share చేయాలి.

## 3. Version 1.10.0 నుంచి కొనసాగుతున్న ఫీచర్లు

### Source video
- ఫోన్ storage నుంచి Android file picker ద్వారా వీడియో ఎంచుకోవచ్చు.
- ఎంచుకున్న వీడియో పేరు మరియు గుర్తించిన duration చూపిస్తుంది.
- యాప్‌లోనే source video preview చూడవచ్చు.
- Export సమయంలో source file‌ను ఉద్దేశపూర్వకంగా overwrite చేయదు.
- ఏ video formats పని చేస్తాయో ఫోన్‌లో ఉన్న Android media decoders‌పై ఆధారపడి ఉంటుంది.

### YouTube URL preview మరియు authorized direct-file import (1.10.0లో కొత్తది)
- YouTube watch URL, Shorts URL లేదా youtu.be URLను paste చేసి appలో embedded YouTube playerలో preview చేయవచ్చు.
- Embedded player controlsతో play, pause, seek చేయవచ్చు.
- ప్రస్తుత playhead వద్ద IN marker మరియు OUT marker set చేయవచ్చు; లేదా secondsలో manually మార్చవచ్చు.
- **Use these clip markers** నొక్కితే timestamps ప్రధాన editorకు వస్తాయి. ఆ తర్వాత అదే recordingకు సంబంధించిన video file ఎంచుకుంటే, ఆ సమయాలు file durationలో సరిపడినప్పుడు Start/Endలో prefill అవుతాయి.
- YouTube URL **preview మరియు timestamp markers కోసం మాత్రమే**. YouTube video/audio stream extract చేయదు, public captionsను ఆటోమేటిక్‌గా తీసుకోదు, watch URLను AI transcription sourceగా ఉపయోగించదు.
- Creator/హక్కుదారుడి అనుమతి ఉన్న direct HTTPS video-file URLను paste చేసి అసలు media fileను app-private storageలో import చేయవచ్చు.
- Server supported video MIME type లేదా సరైన file extension ఇచ్చినప్పుడు MP4, MOV, M4V, WebM, MKV, 3GP formats import చేయవచ్చు.
- Import చేయడానికి ముందు ownership/permission confirmation అవసరం. గరిష్ఠ download పరిమితి 1 GB.
- Google Drive/Dropbox share preview webpage direct video file కాదు; అది import కాదు. Video bytes ఇచ్చే direct media-file link ఉపయోగించాలి.
- YouTube లేదా YouTube video-delivery hostకు redirect అయ్యే links reject అవుతాయి.

### Transcript మరియు speech-to-text
- Groq Whisper ద్వారా cloud transcription.
- OpenAI Whisper ద్వారా cloud transcription.
- Whisper Base ద్వారా on-device offline transcription.
- Timestamp ఉన్న transcript‌ను text boxలో paste చేయవచ్చు.
- SRT, VTT, TXT transcript/subtitle files import చేయవచ్చు.
- SRT/VTT subtitle cue timestamps‌ను transcript lines‌గా మార్చుతుంది.
- TXTలో AIకి ఖచ్చితమైన clip సమయాలు కనుగొనడానికి ఉపయోగపడే timestamps ఉండాలి.

### Full-video Topic Discovery — Video Topic Map (1.10.0లో కొత్తది)
- Timestamp ఉన్న మొత్తం transcript‌ను overlapping sectionsగా విశ్లేషిస్తుంది. పాత విధంగా 8 ముఖ్యమైన highlights మాత్రమే అడగదు.
- Cloud AI ప్రతి sectionలో 80 non-empty transcript linesను 16-line overlapతో process చేసి, ఒక్క sectionకు గరిష్ఠంగా 8 topic segments అడుగుతుంది. తర్వాత duplicate/overlap results merge అవుతాయి.
- Offline Qwen3 ప్రతి sectionలో 36 non-empty linesను 8-line overlapతో process చేసి, ఒక్క sectionకు గరిష్ఠంగా 5 topics అడుగుతుంది.
- Results importance ఆధారంగా కాకుండా source-video Start time ఆధారంగా chronological orderలో చూపబడతాయి.
- ఒక్క analysisలో fixed global 60-topic limit లేదు. Cloud AI ప్రతి transcript windowకు 16 వరకు candidates, Offline Qwen3 ప్రతి windowకు 10 వరకు candidates కోరుతుంది. మొత్తం topics సంఖ్య transcript మరియు model outputపై ఆధారపడి ఉంటుంది.
- Topic types: introduction, background, వేర్వేరు subjects/subtopics, complete Q&A, stories, examples, announcements, explanations, opinions, debates, predictions, practical advice మరియు conclusions.
- ప్రతి cardలో specific title, exact Start/End timestamps, category, importance score (model ఇచ్చినప్పుడు 1–10), ఆ topicలో ఏమి చర్చించారో చిన్న explanation ఉంటుంది.
- Card నొక్కితే ఆ topic exact timestamps Start/End fieldsలోకి వస్తాయి.
- **Play this topic** నొక్కితే source previewలో ఆ topic start నుంచి end వరకు play అవుతుంది.
- ఒక్కో cardలో **Add this topic to Shorts queue** tick చేయవచ్చు; లేదా **Add all detected topics to Shorts queue** ద్వారా అన్ని valid topicsను queueలో పెట్టవచ్చు.
- Queueలోని topicsను వేరువేరు MP4 Shortsగా export చేయవచ్చు లేదా selected topicsను Studio timelineలో కలిపి edit చేయవచ్చు.
- పక్కపక్కన ఉన్న transcript sectionsలో వచ్చిన overlapping duplicate topicsను displayకు ముందు merge చేస్తుంది.

Topic Map transcript ఆధారంగా పని చేస్తుంది; videoలోని visual scenesను స్వయంగా అర్థం చేసుకోదు. Transcript quality, timestamp accuracy, AI model నాణ్యతపై ఫలితాలు ఆధారపడతాయి. ఇప్పుడు fixed global 60-topic cap లేదు; అయినప్పటికీ ప్రతి చిన్న conversational shift తప్పకుండా గుర్తిస్తుందని హామీ లేదు. Long transcriptsకు Cloud AI అనేక requests పంపుతుంది కాబట్టి పాత single-request 8-highlights విధానంతో పోలిస్తే API/token charges ఎక్కువ కావచ్చు. చిన్న offline model పెద్ద cloud model గుర్తించే కొన్ని topicsను మిస్ చేయవచ్చు.



### Clip editing మరియు preview
- ఏ highlight card నొక్కినా ఆ topic యొక్క exact Start/End timestamps load అవుతాయి.
- Preview selected clipతో ఆ exact time range చూడవచ్చు.
- Start/End valuesను secondsలో manualగా మార్చవచ్చు.
- **Expand selected topic to 60s / 90s** ద్వారా మాత్రమే ఎంచుకున్న topic చుట్టూ optional 60 లేదా 90 seconds window తయారు చేయవచ్చు.
- Quick duration buttons: 15s, 30s, 45s, 60s.
- ఒక్కో exported Short గరిష్ఠంగా 180 seconds.

### Queue చేసిన ప్రతి topicను వేరే Shortగా Export చేయడం (1.10.0లో కొత్తది)
- కావాల్సిన ప్రతి topic cardలో **Add this topic to Shorts queue** tick చేయండి.
- **Export N queued topics as separate Shorts** నొక్కండి.
- Confirm చేసిన తర్వాత ప్రతి topic తన exact Start/End సమయాలతో వేరే MP4గా export అవుతుంది.
- ప్రస్తుత aspect ratio మరియు optional text overlay ప్రతి outputకు వర్తిస్తాయి.
- ప్రతి MP4 **Movies / ShortsForge**లో topic title ఆధారంగా filenameతో save అవుతుంది.
- “Exporting topic 2/5” వంటి progress చూపిస్తుంది. అన్నీ లేదా కొన్నే save అయితే completion dialogలో తెలియజేస్తుంది.
- **Share saved Shorts** ద్వారా save అయిన videosను ఒకేసారి share చేయవచ్చు; share చేసే app multiple-video sharing support చేయాలి.
- ప్రతి Short గరిష్ఠం 180 seconds. వేర్వేరు Shorts durations మొత్తాన్ని కలిపి ఒకే 180 seconds పరిమితిగా లెక్కించదు.

### ShortsForge Studio Timeline Editor
**Open Studio Editor · Timeline, split, speed, text, audio** ద్వారా selected topic లేదా queueలోని అన్ని topicsను ఒకే editing timelineలో తెరవవచ్చు.
- ప్రతి queued topic దాని titleతో timeline clipగా కనిపిస్తుంది.
- Split, Delete, Move, Reorder, Undo, trim మరియు playhead scrubbing.
- Playback/export speed 0.5× నుంచి 2× వరకు.
- Vertical 9:16, Square 1:1, Landscape 16:9.
- Text overlay (140 characters), position, style, colour.
- Original audio mute చేయవచ్చు లేదా ఒక looping music track జోడించవచ్చు; music source audio స్థానంలో వస్తుంది.
- మొత్తం timelineను ఒకే MP4గా export చేయవచ్చు; combined Studio timeline గరిష్ఠంగా 180 seconds.
- ప్రతి topicకి separate MP4 కావాలంటే పైన ఉన్న batch export button ఉపయోగించండి.

ఇది native timeline editing దశ మాత్రమే; పూర్తి CapCut/KineMaster clone కాదు. ప్రస్తుత timeline ఒకే source videoలోని rangesనే ఉపయోగిస్తుంది; multi-source multi-track compositor ఇంకా లేదు.


### Export
- Vertical 9:16: 720 × 1280.
- Square 1:1: 720 × 720.
- Landscape 16:9: 1280 × 720.
- MP4 containerలో H.264 video + AAC audio.
- గరిష్ఠంగా 140 characters ఉన్న optional text overlay‌ను వీడియోపై burn చేయవచ్చు.
- Androidలో Movies/ShortsForge folderలో save చేస్తుంది.
- Export అయిన తర్వాత Android share sheet తెరుస్తుంది.
- Export జరుగుతున్నప్పుడు cancel చేయవచ్చు.

### AI మరియు Offline processing
- Groq Cloud AI.
- OpenAI Cloud AI.
- Anthropic Claude.
- Local Qwen3 0.6B Q4 + Whisper Base.
- Cloud API keys‌ను Android Keystoreలో AES-GCMతో encrypt చేసి ఉంచుతుంది.
- Cloud network connection విఫలమై, అవసరమైన local models ఇప్పటికే డౌన్‌లోడ్ అయి ఉంటే, యాప్ local processingకు మారగలదు.

## 4. అవసరాలు

### Android ఫోన్
- Android 8.0 (API 26) లేదా అంతకంటే కొత్త version.
- Source video, export, temporary processing files మరియు కావాలంటే offline models కోసం తగిన ఖాళీ storage.
- ఎంచుకున్న వీడియో codec‌కు ఫోన్‌లో Android media decoder support ఉండాలి.
- Cloud AI మరియు మొదటి offline model download కోసం Internet అవసరం.

### Cloud AI
Settingsలో ఎంచుకునే provider‌కు సంబంధించిన మీ API key అవసరం:
- Groq: highlight analysis మరియు automatic transcription.
- OpenAI: highlight analysis మరియు automatic transcription.
- Claude: timestamped transcript analysis. ప్రస్తుత యాప్‌లో Claude mode selected video నుంచి నేరుగా transcript తయారు చేయదు; ముందుగా మరో మార్గంలో transcript తయారు/import చేయాలి.

Cloud API usageకు ఛార్జీలు ఉండవచ్చు. ఉపయోగించే ముందు provider ప్రస్తుత pricing మరియు usage terms పరిశీలించండి.

### Offline AI
Offline transcription మరియు highlight analysis కోసం:
- Qwen3 0.6B Q4 సుమారు 429 MB.
- Whisper Base సుమారు 148 MB.
- మొత్తం సుమారు 577 MB download.
- రెండు models download చేయడానికి ముందు యాప్ కనీసం 700 MB ఖాళీ storage ఉందో చూస్తుంది.
- మొదటిసారి Internet ఉన్నప్పుడు models download చేయాలి. అవి యాప్ private storageలో ఉంటాయి; APKలో భాగంగా ఉండవు.

Offline వేగం మరియు ఫలితాల నాణ్యత ఫోన్ CPU, RAM, వేడి నియంత్రణ, Android version, ఖాళీ storageపై ఆధారపడి ఉంటాయి. ప్రతి Android పరికరంలో performance test చేయలేదు.

## 5. APK Download మరియు Install చేయడం

1. Android ఫోన్‌లో [ShortsForge Mobile APK download](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk) లింక్ తెరవండి.
2. **ShortsForge-Mobile.apk** ఫైల్ download చేయండి.
3. Browser download notification లేదా Downloads/Files app నుంచి APK తెరవండి.
4. Android అడిగితే, మీరు వాడుతున్న browser లేదా file managerకు “Install unknown apps” permission ఇవ్వండి.
5. Android installation screenలో **Install** నొక్కండి.
6. **ShortsForge Mobile** యాప్ తెరవండి.

ఈ APK debug-signed build. ఇది Google Play Store ద్వారా కాకుండా project GitHub release ద్వారా అందించబడుతోంది.

**పాత version నుంచి update చేసేటప్పుడు:** పాత APK మరియు కొత్త APK signing certificate వేరుగా ఉండటం వల్ల Android update‌ను నిరాకరించవచ్చు. అలా జరిగితే పాత యాప్ uninstall చేసి కొత్త APK install చేయాల్సి రావచ్చు. Uninstall చేస్తే app-private data, saved API keys, offline models తొలగిపోతాయి. అవసరమైతే API keys మళ్లీ నమోదు చేసి models మళ్లీ download చేయాలి.

## 6. Quick Start — త్వరగా ఉపయోగించడం

### Option A — Cloud AI (మొదటిసారి ఉపయోగించడానికి సులభమైన మార్గం)
1. ShortsForge Mobile install చేసి open చేయండి.
2. **Settings** నొక్కండి.
3. **Groq · Cloud AI** లేదా **OpenAI · Cloud AI** ఎంచుకోండి.
4. సరైన provider API key paste చేసి **Save AI settings** నొక్కండి.
5. Editorకి తిరిగి రండి.
6. **Choose video** నొక్కి ఫోన్‌లోని video file ఎంచుకోండి.
7. Filename, duration, video preview కనిపించే వరకు వేచి ఉండండి.
8. **Auto-transcribe + find clips** నొక్కండి.
9. Transcription మరియు AI highlight analysis పూర్తయ్యే వరకు వేచి ఉండండి.
10. Suggestion cards చూడండి. మొదటి suggestion ఆటోమేటిక్‌గా select అవుతుంది.
11. కావాలంటే వేరే topic card ఎంచుకోండి; **Play this topic** ద్వారా ఆ topic preview చేయండి.
12. 60 seconds లేదా 90 seconds ఎంచుకుని **Preview selected clip** నొక్కండి.
13. అవసరమైతే Start/End మార్చండి. Aspect ratio ఎంచుకోండి. కావాలంటే text overlay జోడించండి.
14. Timelineలో మరింత edit చేయడానికి **Open Studio Editor · Timeline, split, speed, text, audio** నొక్కండి; త్వరగా export చేయాలంటే ప్రధాన editorలో **Export Short** నొక్కవచ్చు.
15. Studio Editorలో clip segments క్రమం, trim/split, speed, canvas, text, audio సర్దుకుని **Export edited video** నొక్కండి.
16. Save అయిన తర్వాత share చేయండి లేదా **Edit in Studio**తో మళ్లీ edit చేయండి.

### Option B — Offline AI
1. మొదటి model download కోసం Internet కలిగి ఉండండి.
2. **Settings** తెరవండి.
3. **OFFLINE MODEL MANAGER**లో **Download offline AI models · ~577 MB** నొక్కండి.
4. Qwen3 మరియు Whisper Base download అయ్యేవరకు యాప్‌ను openగా ఉంచండి.
5. రెండు model statuses **Downloaded**గా ఉన్నాయో చూడండి.
6. **AI MODE**లో **Offline local AI · Qwen3 + Whisper** ఎంచుకోండి.
7. **Save AI settings** నొక్కి Editorకి తిరిగి రండి.
8. Local video ఎంచుకుని **Auto-transcribe + find clips** నొక్కండి.
9. Whisper localగా transcript తయారు చేస్తుంది; Qwen3 timestamp transcript‌ను విశ్లేషిస్తుంది.
10. Suggestion cardsలో ప్రతి topicకు exact timestamps చూడండి. కావాల్సిన cardsలో **Add this topic to Shorts queue** tick చేయండి.
11. ఒక్కో topicను వేరే MP4గా చేయడానికి **Export N queued topics as separate Shorts** నొక్కండి; ఒకే timelineగా edit చేయడానికి **Open Studio Editor · N topic(s) as one timeline** నొక్కండి.
12. Batch export confirm చేసి app openగా ఉంచండి. ప్రతి topic వేరే MP4గా Movies/ShortsForgeలో save అవుతుంది.

Models download చేసిన తర్వాత local transcription మరియు transcript analysis cloud AIకి audio/transcript పంపకుండా పనిచేయగలవు. తర్వాత models మళ్లీ download చేయడానికి లేదా cloud AI వాడడానికి Internet అవసరం.

### Option C — ఇప్పటికే transcript ఉంటే
1. Source video ఎంచుకోండి.
2. Timestamp ఉన్న transcript‌ను **TRANSCRIPT**లో paste చేయండి లేదా **Import subtitles / transcript (SRT, VTT, TXT)** నొక్కండి.
3. Settingsలో Groq, OpenAI లేదా Claude ఎంచుకుని అవసరమైన API key save చేయండి.
4. Editorకి తిరిగి వచ్చి **Analyze pasted transcript** నొక్కండి.
5. Suggestions పరిశీలించి ఒకటి select చేయండి, preview చేసి export చేయండి.

### Option D — YouTube preview + అనుమతి ఉన్న video source
1. **YOUTUBE VIDEO URL**లో YouTube watch, Shorts లేదా youtu.be link paste చేయండి.
2. **Preview YouTube URL + mark clip timestamps** నొక్కండి.
3. Embedded playerలో play/seek చేయండి. **Set IN at playhead**, **Set OUT at playhead** నొక్కండి లేదా seconds values manually మార్చండి.
4. **Use these clip markers** నొక్కి timestampsను ప్రధాన editorకు పంపండి.
5. అదే recordingకు సంబంధించిన original file లేదా creator/rightsholder ఇచ్చిన direct HTTPS video-file URL పొందండి.
6. Direct URL అయితే **AUTHORIZED DIRECT VIDEO FILE URL**లో paste చేసి **Import authorized video URL** నొక్కండి; permission confirm చేసి download/validation పూర్తయ్యే వరకు వేచి ఉండండి.
7. Preview చేసినదే imported video అని నిర్ధారించండి. Markers imported file durationలో సరిపడితే Start/Endలో prefill అవుతాయి.
8. **Auto-transcribe + find clips** నొక్కండి లేదా transcript import చేసి, Studio Editorలో edit చేసి export చేయండి.

**ముఖ్యమైన విషయం:** YouTube preview వల్ల watch URL editable sourceగా మారదు. AI transcription మరియు MP4 export కోసం local లేదా authorized direct media file అవసరం. Public YouTube captionsను app ఆటోమేటిక్‌గా fetch చేయదు.

## 7. Editor‌ను వివరంగా ఉపయోగించడం

### Step 1: Source video ఎంచుకోవడం
Local file కోసం **Choose video** వాడండి లేదా creator అనుమతితో ఉన్న HTTPS media-file linkను **AUTHORIZED DIRECT VIDEO FILE URL** ద్వారా import చేయండి. Media validate అయిన తర్వాత filename మరియు duration కనిపిస్తాయి. YouTube URLను **Preview YouTube URL + mark clip timestamps** ద్వారా embedded preview చేయవచ్చు; అది preview మరియు time markers మాత్రమే ఇస్తుంది, editable video file కాదు.

### Step 2: Transcript తయారు చేయడం
ఈ మార్గాల్లో ఒకటి ఎంచుకోండి:
- Cloud: Groq లేదా OpenAI ఎంచుకున్న వీడియో నుంచి audio extract చేసి timestamp transcription కోసం providerకు పంపగలవు.
- Offline: Model download అయిన తర్వాత Whisper Base ఫోన్‌లోనే transcription చేస్తుంది.
- Existing transcript: Text fieldలో paste చేయండి లేదా SRT/VTT/TXT file import చేయండి.

Cloud transcriptionలో extracted audio ఎంచుకున్న providerకు పంపబడుతుంది. Offline Whisper audio‌ను ఫోన్‌లోనే process చేస్తుంది.

### Step 3: ముఖ్యమైన moments కనుగొనడం
**Auto-transcribe + find clips** లేదా **Analyze pasted transcript** నొక్కండి. AI timestamps మరియు చిన్న వివరణతో suggestions ఇస్తుంది. సరైన timestamps లేని transcript నుంచి ఉపయోగకరమైన clip suggestions రాకపోవచ్చు.

### Step 4: Suggestion ఎంచుకోవడం
మొదటి suggestion ఆటోమేటిక్‌గా select అవుతుంది. దాని cardపై **SELECTED** కనిపిస్తుంది. ఇంకో card నొక్కితే ఆ topic యొక్క exact Start/End timestamps ప్రధాన editorలోకి వస్తాయి. Screenshotలో కనిపించే timestampsనే ఉపయోగిస్తుంది; defaultగా 60/90 seconds windowతో వాటిని మార్చదు.

కావాల్సిన topic cardsలో **Add this topic to Shorts queue** tick చేయండి. Queueలోని ప్రతి topicను వేరే MP4గా export చేయవచ్చు లేదా అన్నింటిని ఒకే Studio timelineలోకి పంపవచ్చు.

**Optional:** ప్రస్తుతం select చేసిన topic కోసం మాత్రమే **Expand selected topic to 60s / 90s** వాడవచ్చు. Preview చేసి అవసరమైతే Start/End మార్చండి.

### Step 5: Preview మరియు Edit
**Preview selected clip** నొక్కితే ఎంచుకున్న Start సమయం నుంచి playback ప్రారంభమై End సమయానికి ఆగుతుంది. అదనంగా:
- Start మరియు End secondsను manualగా type చేయవచ్చు.
- 15s, 30s, 45s, 60s quick duration buttons వాడవచ్చు.
- AI selection windowను 60 లేదా 90 secondsగా మార్చవచ్చు.

ఈ preview source videoలో ఎంచుకున్న time range‌ను మాత్రమే చెక్ చేస్తుంది. Final crop/aspect ratio మరియు text overlay export సమయంలో apply అవుతాయి. కాబట్టి Export అయిన MP4ను కూడా చూసి నిర్ధారించండి.

### Step 6: Output ఎంచుకుని Export చేయడం
- Shorts-style video కోసం Vertical 9:16.
- Square కోసం 1:1.
- Landscape కోసం 16:9.

గరిష్ఠంగా 140 characters ఉన్న optional text overlay జోడించండి. ఒక్క clip కోసం **Export Short** నొక్కండి. అనేక queued topicsను వేరువేరు MP4లుగా చేయడానికి **Export N queued topics as separate Shorts** నొక్కి confirm చేయండి. ప్రతి video Movies/ShortsForgeలో save అవుతుంది. Batch పూర్తయ్యే వరకు app openగా ఉంచి completion dialogలో **Share saved Shorts** ఉపయోగించండి. ఒకే 180 seconds కంటే ఎక్కువ కలిపిన timeline కావాలంటే వేర్వేరు MP4 batch export వాడండి; Studio combined timeline మాత్రం 180 seconds పరిమితిలో ఉంటుంది.

## 8. AI Modes మరియు Models

| Mode | పని | Internet/API key |
|---|---|---|
| Groq | Groq chat model highlights rank చేస్తుంది; Groq Whisper transcription చేస్తుంది | అవసరం |
| OpenAI | OpenAI chat model highlights rank చేస్తుంది; Whisper API transcription చేస్తుంది | అవసరం |
| Claude | ఇచ్చిన timestamp transcript‌ను విశ్లేషిస్తుంది | అవసరం |
| Offline local AI | Whisper Base transcription; Qwen3 0.6B Q4 highlight analysis | Models మొదట download చేయాలి |

ప్రస్తుత source codeలో ఉన్న model IDs:
- Groq highlight analysis: openai/gpt-oss-120b.
- Groq transcription: whisper-large-v3-turbo.
- OpenAI highlight analysis: gpt-4o-mini.
- OpenAI transcription: whisper-1.
- Claude transcript analysis: claude-haiku-4-5-20251001.
- Local highlight analysis: Qwen3-0.6B-Q4_0.gguf.
- Local transcription: ggml-base.bin (Whisper Base).

Model పేర్లు, API availability, usage limits, ధరలు మారవచ్చు. ఇవి ప్రస్తుత codeలో ఉన్న IDs మాత్రమే; provider భవిష్యత్తులో ఇవే కొనసాగిస్తాడని హామీ కాదు.

## 9. Transcript Format సూచనలు

ప్రతి మాటల భాగానికి timestamp ఉంటే transcript బాగా పనిచేస్తుంది. ఉదాహరణ:

    [00:01.20 - 00:04.50] ఈరోజు చర్చకు స్వాగతం.
    [00:04.60 - 00:09.10] ఈరోజు మనం Artificial Intelligence గురించి మాట్లాడుకుందాం.

సాధారణ subtitle cue times ఉన్న SRT/VTT files ఈ formatకు మార్చబడతాయి. Plain TXT fileలో ఉన్న text యథాతథంగా ఉంచబడుతుంది. TXT నుంచి ఖచ్చితమైన clip times రావాలంటే అందులో timestamps ఉండాలి.

Transcript:
- ఎంచుకున్న source videoకే చెందినదై ఉండాలి.
- Timestamps వరుసక్రమంలో ఉండాలి.
- ప్రశ్న మరియు సమాధానం అర్థమయ్యేంత context కలిగి ఉండాలి.
- లేని timestamps‌ను ఊహించి రాయకూడదు.

## 10. Data మరియు Privacy

- Video selection మరియు MP4 export ఫోన్‌లోనే జరుగుతాయి.
- YouTube preview embedded YouTube player ద్వారా చూపబడుతుంది. Direct video-file URL HTTPS ద్వారా request అవుతుంది; media file editing కోసం app-private storageలో save అవుతుంది.
- Cloud transcription కోసం extracted audio ఎంపిక చేసిన providerకు పంపబడుతుంది.
- Cloud highlight analysis కోసం transcript text ఎంపిక చేసిన AI providerకు పంపబడుతుంది.
- Local Whisper మరియు Qwen3 models download అయిన తర్వాత ఫోన్‌లోనే inference చేస్తాయి.
- API keys AES-GCM మరియు Android Keystore ఉపయోగించి encrypt చేసి app preferencesలో ఉంచబడతాయి.
- Offline model files యాప్ private storageలో ఉంటాయి.
- ప్రస్తుత యాప్‌లో account-based cloud project sync లేదా remote media library లేదు.

Private లేదా confidential recordingsను cloud APIకి పంపే ముందు ఆ provider privacy terms చదవండి. API keysను source code, README, screenshots లేదా public issue reportsలో పెట్టకండి.

## 11. ప్రస్తుత యాప్‌లో లేని ఫీచర్లు

క్రింది అంశాలు **Version 1.13.0లో కూడా లేవు**:
- ShortsForgeలోపలే YouTube streamను editable MP4/audio sourceగా extract/download చేయడం లేదు. 1.12.0లో URLను వేరుగా install చేసిన YTDLnisకి పంపవచ్చు; అక్కడ మీరు download పూర్తి చేసి saved videoను Share/Downloads ద్వారా ShortsForgeకి తీసుకురావాలి. URL ఒక్కటే editable source file కాదు.
- Public YouTube caption tracks లేదా transcriptsను ఆటోమేటిక్‌గా download చేయడం.
- Google Drive/Dropbox share-page URLs import చేయడం. Importerకు direct HTTPS video-file response అవసరం.
- Automatic YouTube channel upload, scheduling లేదా publishing.
- AI animated word-by-word subtitles లేదా karaoke captions.
- Automatic B-roll, images, scene generation లేదా presenter generation.
- పూర్తి CapCut/KineMaster స్థాయి multi-source, multi-layer editing: picture-in-picture tracks, transitions, keyframes, filters, advanced colour grading, waveform editing, independent audio volume automation, frame-by-frame professional tools.
- Automatic video title, description, hashtags లేదా thumbnail generation.
- Server-side user account, cloud project sync లేదా team collaboration.

Studio Editorలో styles ఉన్న static text overlay ఉంది; animated subtitle editor ఇంకా లేదు. ప్రస్తుత timeline ఒకే selected source videoలోని ranges‌ను క్రమపరుస్తుంది; ఒక music track source audio స్థానంలో వస్తుంది. స్వతంత్ర multi-track audio mixing లేదు. AI transcript textనే విశ్లేషిస్తుంది; ప్రస్తుతం video visual scene understanding లేదా face tracking చేయదు.

## 12. భవిష్యత్తు Roadmap — ఇంకా నిర్మించని ఆలోచనలు

తరువాత పరిశీలించదగిన improvements:
- Edit చేయగల styles/positionsతో animated word-level captions.
- మెరుగైన crop controls, subject-aware reframing, safe-area guides.
- Multi-layer timeline editing, transitions, keyframes, filters, independent audio mixing, frame-accurate controls.
- Saved editing projects మరియు export history.
- Audio cleanup మరియు license ఉన్న background music workflows.
- Transcription మరియు transcript analysis కోసం మరిన్ని language controls.
- Supported మరియు authorized platform APIs ద్వారా మాత్రమే optional publishing integrations.

ఇవి భవిష్యత్తు ఆలోచనలు మాత్రమే. ప్రస్తుతం APKలో ఉన్న featuresగా భావించకండి.

## 13. సమస్యలు మరియు పరిష్కారాలు

**API key లేదు అని చూపిస్తే**  
Settings తెరిచి provider ఎంచుకోండి. దానికి సంబంధించిన API key నమోదు చేసి Save AI settings నొక్కండి.

**Claude వీడియోను transcribe చేయడం లేదు**  
ఇది ప్రస్తుత versionలో expected behaviour. Claude timestamp transcript‌ను విశ్లేషిస్తుంది. ముందుగా Groq/OpenAI లేదా local Whisperతో transcript తయారు చేయండి లేదా import/paste చేయండి.

**Highlight suggestions రావడం లేదు**  
Transcript ఖాళీగా లేదో, సరైన timestamps ఉన్నాయో, అది ఎంచుకున్న videoకే చెందినదో చూడండి. మరింత పొడవైన transcript లేదా Cloud model ప్రయత్నించండి.

**Offline AI models download చేయమంటోంది**  
Settings → OFFLINE MODEL MANAGER తెరిచి రెండు models download చేయండి. కనీసం 700 MB storage మరియు స్థిరమైన Internet ఉండాలి.

**Video open/export కావడం లేదు**  
ఆ video codec Android ఫోన్‌లో support కాకపోవచ్చు, audio track లేకపోవచ్చు లేదా file దెబ్బతిన్నదై ఉండవచ్చు. H.264 video మరియు AAC audio ఉన్న MP4 ప్రయత్నించండి.

**Export duration తప్పుగా ఉంది**  
Start మరియు End fields చూడండి. AI 60/90-second windows source video durationకు లోబడి ఉంటాయి. Manual export 180 seconds దాటకూడదు.

**Export అయిన వీడియో crop అయింది**  
ఎంచుకున్న aspect ratioను నింపేలా app వీడియోను scale చేస్తుంది. అందువల్ల source frameలోని కొన్ని భాగాలు crop కావచ్చు. మరో ratio ప్రయత్నించి final MP4 చూడండి.

**కొత్త APK పాత versionపై install కావడం లేదు**  
వేర్వేరు debug signing certificates కారణంగా Android installationను నిరాకరించవచ్చు. అవసరమైతే పాత version uninstall చేసి కొత్తది install చేయండి. అలా చేస్తే local API keys మరియు offline model files తొలగిపోతాయి.

## 14. Technology Stack

- Kotlin మరియు Native Android Views.
- File selection కోసం Android Activity Result API మరియు Storage Access Framework.
- Media processing కోసం Android MediaCodec / MediaExtractor / MediaMuxer.
- Video duration కోసం Android MediaMetadataRetriever.
- Playback కోసం AndroidX Media3 ExoPlayer.
- MP4 export మరియు overlays కోసం AndroidX Media3 Transformer మరియు Effects.
- Cloud API requests, offline model downloads, authorized direct video-file imports కోసం OkHttp.
- Embedded preview మరియు clip timestamp markers కోసం Android WebView + YouTube IFrame Player API.
- Cloud AI కోసం Groq, OpenAI, Anthropic APIs.
- Local Qwen3 inference కోసం llama.cpp Android integration.
- Local Whisper transcription కోసం whisper.cpp Android integration.
- API-key encryption కోసం Android Keystore మరియు AES-GCM.
- APK build/release కోసం GitHub Actions.

## 15. Repository నిర్మాణం

    .
    ├── android/
    │   ├── app/
    │   │   ├── build.gradle
    │   │   └── src/main/
    │   │       ├── AndroidManifest.xml
    │   │       └── java/com/shortsforge/mobile/
    │   │           ├── MainActivity.kt
    │   │           ├── YouTubePreviewActivity.kt
    │   │           └── StudioEditorActivity.kt
    │   ├── build.gradle
    │   └── settings.gradle
    ├── .github/
    │   └── workflows/
    │       └── android-apk.yml
    └── README.md

## 16. Source నుంచి Android app Build చేయడం

### Build Environment
- JDK 17.
- Android SDK platform 35.
- Android SDK Build Tools 35.0.0.
- Gradle 8.9 (GitHub Actions workflow ఇదే version install చేస్తుంది).
- Gradle dependencies మరియు Android libraries కోసం Internet.

### Build Command

Repository root నుంచి:

    cd android
    gradle --no-daemon assembleDebug

సాధారణంగా APK ఇక్కడ తయారవుతుంది:

    android/app/build/outputs/apk/debug/app-debug.apk

GitHub Actions workflow చేసే పనులు:
1. Repository checkout.
2. Java 17 setup.
3. Android SDK platform/build tools install.
4. Gradle 8.9 setup.
5. assembleDebug run.
6. APKని ShortsForge-Mobile.apkగా rename.
7. Workflow artifact upload.
8. APKని mobile-latest GitHub releaseలో publish.

android/లో లేదా workflow fileలో మార్పులు push చేస్తే build trigger అవుతుంది. workflow_dispatch ద్వారా manualగానూ ప్రారంభించవచ్చు.

## 17. తాజా Release

- Release: 1.12.0.
- Version code: 16.
- Build result: Successful.
- Build run: [GitHub Actions run 44459451960](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44459451960).
- APK: [ShortsForge-Mobile.apk](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk).
- SHA-256: `ac4a7e39a21ffffaf2d93516f7e89f6d1d9b3ab933a6c1ad511e9be051387e08`.

Computerలో download చేసిన file verify చేయాలంటే దాని SHA-256 hash calculate చేసి పై విలువతో పోల్చండి.

## 18. Copyright, Permissions మరియు Project Status

మీకు స్వంతమైన, అనుమతి ఉన్న లేదా చట్టపరంగా ఉపయోగించడానికి హక్కు ఉన్న వీడియోలనే ఎంచుకుని edit/publish చేయండి. చిన్న duration, credit ఇవ్వడం లేదా “no copyright intended” disclaimer ఉండటం వల్ల reuse permission ఆటోమేటిక్‌గా రాదు. AI highlight suggestions వల్ల వీడియోపై copyright permission లభించదు.

ShortsForge Mobile YouTube watch-page video streamను extract లేదా download చేయదు. YouTube link ద్వారా embedded preview మరియు timestamp markers మాత్రమే లభిస్తాయి. AI processing మరియు export కోసం local file లేదా creator-authorized direct video-file source అవసరం.

APK build GitHub Actionsలో విజయవంతంగా verify అయింది. ప్రతి Android పరికరంలో స్వతంత్రంగా test చేయలేదు. Playback, local model performance, export time మరియు compatibility పరికరాన్ని బట్టి మారవచ్చు.

ఈ READMEలో open-source software license ప్రకటించలేదు. Public repositoryగా కనిపించడం వల్ల ఈ source codeను స్వేచ్ఛగా reuse చేయడానికి permission వచ్చిందని భావించకండి. Modified versions పంపిణీ చేసే ముందు LICENSE file జోడించండి లేదా licenseను పరిశీలించండి.

---

# हिन्दी

## 1. परियोजना का परिचय

ShortsForge Mobile Android के लिए बनाया गया AI-सहायता प्राप्त वीडियो क्लिपिंग ऐप है। यह लंबे इंटरव्यू, पॉडकास्ट, लेक्चर या अन्य वीडियो से उपयोगी हिस्से पहचानने, उन्हें छोटे क्लिप में संपादित करने और MP4 के रूप में एक्सपोर्ट करने में मदद करता है।

ऐप timestamp वाले transcript का विश्लेषण करके अधिकतम 8 उपयोगी moments सुझाता है। हर suggestion में title/hook, timestamp, category, importance score और explanation हो सकते हैं। पहला suggestion अपने-आप select होता है। आप दूसरा suggestion चुन सकते हैं, उसका समय preview कर सकते हैं, Start/End बदल सकते हैं और अंत में MP4 export कर सकते हैं।

यह **AI topic discovery, clip selection, editing और MP4 export ऐप** है। YouTube URL को ऐप में preview करके IN/OUT timestamps mark कर सकते हैं। Version 1.12.0 में link को अलग से install किए गए YTDLnis app में भेजकर वहाँ download शुरू किया जा सकता है; download पूरा होने पर saved file को ShortsForge में Share करें या Downloads से चुनें। AI transcription/export के लिए actual video file आवश्यक है।

## 2. परियोजना का लक्ष्य

लंबी recording में उपयोगी short-form moments को हाथ से खोजने में लगने वाला समय कम करना इस परियोजना का मुख्य लक्ष्य है।

इसका intended workflow:

1. Android फोन में मौजूद local video चुनें या creator की अनुमति वाला direct HTTPS video-file URL import करें। YouTube URL से केवल preview और timestamps मिलते हैं; वह editable source file नहीं है।
2. Groq/OpenAI cloud Whisper या फोन में मौजूद local Whisper से timestamp transcript बनाएं, या अपना transcript paste/import करें।
3. AI से सबसे उपयोगी moments की ranking करवाएं।
4. Suggestion cards, timestamps, category, importance score और explanation देखें।
5. एक suggestion चुनें और उसके आसपास 60 या 90 सेकंड की clip window बनाएं।
6. Export से पहले उस समय-सीमा का preview करें और जरूरत पड़ने पर Start/End बदलें।
7. Aspect ratio चुनें, चाहें तो text overlay जोड़ें और MP4 export करें।
8. वीडियो को Movies/ShortsForge में ढूंढें और Android share sheet से साझा करें।

## 3. Version 1.10.0 से जारी सुविधाएँ

### Source video
- Android file picker से फोन की storage में मौजूद वीडियो चुनें।
- चुने गए वीडियो का filename और उपलब्ध duration दिखाता है।
- ऐप के अंदर source video preview कर सकते हैं।
- Export प्रक्रिया source file को जानबूझकर overwrite नहीं करती।
- कौन-से video formats चलेंगे, यह Android डिवाइस के media decoders पर निर्भर करता है।

### YouTube URL preview और authorized direct-file import (1.10.0 में जोड़ा गया)
- YouTube watch URL, Shorts URL या youtu.be URL paste करके ऐप के embedded YouTube player में preview करें।
- Embedded player controls से play, pause और seek करें।
- मौजूदा playhead पर IN और OUT markers लगाएं, या seconds में timestamps manually बदलें।
- **Use these clip markers** दबाने पर timestamps main editor में वापस आते हैं। बाद में उसी recording की video file चुनने पर, यदि timestamps उसकी duration के भीतर हैं, तो Start/End में prefill होते हैं। सुनिश्चित करें कि preview और imported file वही recording है।
- YouTube URL **केवल preview और timestamp markers** देता है। ऐप YouTube video/audio stream extract नहीं करता, public captions अपने-आप नहीं लाता और watch URL को AI transcription source नहीं बनाता।
- Creator/rightsholder की अनुमति वाला direct HTTPS video-file URL paste करके असली media file को app-private storage में import कर सकते हैं।
- Server supported video MIME type या पहचाने जाने योग्य extension लौटाए तो MP4, MOV, M4V, WebM, MKV और 3GP import किए जा सकते हैं।
- Import से पहले ownership/permission confirmation जरूरी है। अधिकतम download size 1 GB है।
- Google Drive/Dropbox share-preview webpage direct video file नहीं है और import नहीं होगी। ऐसा direct media-file link इस्तेमाल करें जो video bytes लौटाता हो।
- YouTube या YouTube video-delivery host पर redirect होने वाले links reject किए जाते हैं।

### Transcript और Speech-to-Text
- Groq Whisper से cloud transcription।
- OpenAI Whisper से cloud transcription।
- Whisper Base से फोन पर offline transcription।
- Timestamp वाला transcript सीधे text box में paste कर सकते हैं।
- SRT, VTT और TXT transcript/subtitle files import कर सकते हैं।
- SRT/VTT cue timestamps को timestamped transcript lines में बदला जाता है।
- TXT में AI द्वारा सटीक clip समय पहचानने के लिए उपयोगी timestamps होने चाहिए।

### पूरे वीडियो की Topic Discovery — Video Topic Map (1.10.0 में जोड़ा गया)
- पूरे timestamped transcript को overlapping sections में analyse करता है; अब केवल आठ सबसे महत्वपूर्ण highlights मांगने वाला तरीका नहीं है।
- Cloud AI हर section में 80 non-empty transcript lines को 16-line overlap के साथ process करता है और हर section से अधिकतम 8 topic segments मांगता है। बाद में duplicate/overlapping results merge किए जाते हैं।
- Offline Qwen3 हर section में 36 non-empty lines को 8-line overlap के साथ process करता है और हर section से अधिकतम 5 topics मांगता है।
- Results importance ranking के बजाय source-video के start time के अनुसार chronological order में दिखते हैं।
- एक analysis पर fixed global 60-topic limit नहीं है। Cloud AI हर transcript window से अधिकतम 16 candidates और Offline Qwen3 हर window से अधिकतम 10 candidates माँगता है। कुल topics transcript और model output पर निर्भर हैं।
- Topic types: introduction, background, अलग-अलग subjects/subtopics, complete Q&A, stories, examples, announcements, explanations, opinions, debates, predictions, practical advice और conclusions।
- हर card में specific title, exact Start/End timestamps, category, model द्वारा मिलने पर 1–10 importance score और topic में क्या चर्चा हुई उसका छोटा explanation होता है।
- Card दबाने पर उस topic के exact timestamps Start/End fields में आते हैं।
- **Play this topic** दबाने पर source preview उस topic के start से end तक चलता है।
- अलग-अलग cards पर **Add this topic to Shorts queue** tick करें, या **Add all detected topics to Shorts queue** से सभी valid topics queue में जोड़ें।
- Queue किए गए topics को अलग-अलग MP4 Shorts के रूप में export करें या selected topics को एक Studio timeline में edit करें।
- पास-पास के transcript sections से आए overlapping duplicate topics को display से पहले merge किया जाता है।

Topic Map transcript पर आधारित है, visual scene understanding पर नहीं। परिणाम transcript की quality, timestamp accuracy और AI model पर निर्भर करते हैं। अब fixed global 60-topic cap नहीं है; फिर भी हर छोटे conversational shift के detection की गारंटी नहीं है। लंबे transcript के लिए Cloud AI कई requests भेजता है, इसलिए पुराने single-request, आठ-highlight तरीके से अधिक API/token charges हो सकते हैं। छोटा offline model कुछ ऐसे topics छोड़ सकता है जिन्हें बड़ा cloud model पहचानता।



### Clip Editing और Preview
- किसी highlight card को दबाने पर उस topic के exact Start/End timestamps load होते हैं।
- **Preview selected clip** से वही exact time range देखें।
- Start और End seconds manually बदल सकते हैं।
- **Expand selected topic to 60s / 90s** से selected topic के आसपास optional 60 या 90-second window बनाएं।
- Quick duration buttons: 15s, 30s, 45s और 60s।
- हर individual exported Short अधिकतम 180 seconds हो सकता है।

### Queue किए गए हर Topic को अलग Short के रूप में Export करना (1.10.0 में जोड़ा गया)
- जिन topic cards की जरूरत है, उनमें **Add this topic to Shorts queue** tick करें।
- **Export N queued topics as separate Shorts** दबाएं।
- Confirm करने पर हर topic अपने exact Start/End timestamps के साथ अलग MP4 में export होता है।
- वर्तमान aspect ratio और optional text overlay हर output पर लागू होते हैं।
- हर MP4 **Movies / ShortsForge** में topic title पर आधारित filename से save होता है।
- “Exporting topic 2/5” जैसे progress messages दिखते हैं। सभी या कुछ ही files save होने पर completion dialog इसकी जानकारी देता है।
- **Share saved Shorts** से saved videos एक साथ share कर सकते हैं; target app को multiple-video sharing support करना होगा।
- हर Short की सीमा 180 seconds है। अलग-अलग Shorts की durations को जोड़कर एक combined 180-second limit नहीं लगती।

### ShortsForge Studio Timeline Editor
**Open Studio Editor · Timeline, split, speed, text, audio** दबाकर current topic या queue के सभी topics को एक ही editing timeline में खोलें।
- हर queued topic अपनी title के साथ timeline clip के रूप में दिखता है।
- Split, Delete, Move, Reorder, Undo, trim और playhead scrubbing।
- Playback/export speed 0.5× से 2× तक।
- Vertical 9:16, Square 1:1 और Landscape 16:9।
- Text overlay (140 characters), position, style और colour।
- Original audio mute करें या एक looping music track जोड़ें; music source audio को replace करता है।
- पूरी timeline को एक MP4 में export कर सकते हैं; combined Studio timeline अधिकतम 180 seconds है।
- हर topic का अलग MP4 चाहिए तो batch export button इस्तेमाल करें।

यह native timeline editing का चरण है, पूर्ण CapCut/KineMaster clone नहीं। अभी timeline एक ही source video के ranges इस्तेमाल करती है; multi-source multi-track compositor अभी उपलब्ध नहीं है।


### Export
- Shorts-style वीडियो के लिए Vertical 9:16: 720 × 1280।
- Square 1:1: 720 × 720।
- Landscape 16:9: 1280 × 720।
- MP4 container में H.264 video और AAC audio।
- अधिकतम 140 characters का optional text overlay export किए गए clip पर burn किया जा सकता है।
- समर्थित Android versions पर Movies/ShortsForge में save करता है।
- Export के बाद Android share sheet खोलता है ताकि आप MP4 किसी ऐप से share कर सकें।
- चल रहे export को cancel कर सकते हैं।

### AI Providers और Offline Processing
- Groq Cloud AI।
- OpenAI Cloud AI।
- Anthropic Claude।
- Offline local AI: Qwen3 0.6B Q4 और Whisper Base।
- Cloud API keys को Android Keystore और AES-GCM की मदद से locally encrypt किया जाता है।
- Cloud network connection विफल होने पर, यदि जरूरी local models पहले से मौजूद हैं, तो ऐप local processing पर switch कर सकता है।

## 4. आवश्यकताएँ

### Android डिवाइस
- Android 8.0 (API 26) या उससे नया version।
- Source video, export, temporary processing और (वैकल्पिक) offline models के लिए पर्याप्त खाली storage।
- चुने गए वीडियो codec के लिए डिवाइस में Android media decoder support।
- Cloud AI और पहली offline-model download के लिए Internet।

### Cloud AI
Settings में चुने गए provider की अपनी API key चाहिए:
- Groq: highlight analysis और automatic transcription।
- OpenAI: highlight analysis और automatic transcription।
- Claude: timestamped transcript analysis। मौजूदा ऐप में Claude mode चुने गए वीडियो से सीधे transcript नहीं बनाता; पहले किसी अन्य तरीके से timestamped transcript बनाएं या import करें।

Cloud service API usage के लिए शुल्क ले सकती है। उपयोग करने से पहले provider की मौजूदा pricing और usage terms देखें।

### Offline AI
Offline transcription और highlight analysis के लिए:
- Qwen3 0.6B Q4 लगभग 429 MB।
- Whisper Base लगभग 148 MB।
- कुल download लगभग 577 MB।
- दोनों models download करने से पहले ऐप कम-से-कम 700 MB खाली storage की जाँच करता है।
- पहली बार Internet रहते हुए models download करें। वे ऐप की private storage में रहते हैं और APK में शामिल नहीं हैं।

वास्तविक गति और model quality फोन के CPU, RAM, thermal limits, Android version और storage पर निर्भर करती है। हर Android डिवाइस पर offline performance का benchmark नहीं किया गया है।

## 5. APK Download और Install

1. Android फोन में [ShortsForge Mobile APK download](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk) खोलें।
2. **ShortsForge-Mobile.apk** डाउनलोड करें।
3. Browser की download notification या Downloads/Files ऐप से APK खोलें।
4. Android पूछे तो इस्तेमाल किए जा रहे browser या file manager को unknown apps install करने की अनुमति दें।
5. Android installation screen पर **Install** दबाएं।
6. **ShortsForge Mobile** खोलें।

यह APK debug-signed build है। इसे Google Play Store के बजाय project के GitHub release से सीधे वितरित किया जा रहा है।

**पुराने build को update करते समय:** यदि पुराने और नए APK के signing certificates अलग होने के कारण Android नया APK install नहीं करता, तो पुराने ऐप को uninstall करके नया APK install करना पड़ सकता है। Uninstall करने से app-private data, saved API keys और downloaded offline models हट जाएंगे। जरूरत पड़ने पर API keys दोबारा डालें और models फिर से डाउनलोड करें।

## 6. Quick Start — जल्दी शुरू करें

### Option A — Cloud AI (पहली बार इस्तेमाल करने का आसान तरीका)
1. ShortsForge Mobile install करके खोलें।
2. **Settings** दबाएं।
3. **Groq · Cloud AI** या **OpenAI · Cloud AI** चुनें।
4. उसी provider की API key paste करें और **Save AI settings** दबाएं।
5. Editor में वापस जाएं।
6. **Choose video** दबाकर फोन की local video file चुनें।
7. Filename, duration और video preview दिखने तक प्रतीक्षा करें।
8. **Auto-transcribe + find clips** दबाएं।
9. Transcription और highlight analysis पूरा होने दें।
10. Chronological Video Topic Map देखें। पहला topic अपने-आप select होता है। किसी card पर tap करके उसके exact timestamps load करें या **Play this topic** दबाकर topic preview करें।
11. चाहें तो दूसरा card चुनें।
12. 60 या 90 seconds चुनें और **Preview selected clip** दबाएं।
13. जरूरत पड़ने पर Start/End बदलें, aspect ratio चुनें और चाहें तो text overlay जोड़ें।
14. Timeline में और editing के लिए **Open Studio Editor · Timeline, split, speed, text, audio** दबाएं; जल्दी export के लिए main editor में **Export Short** दबा सकते हैं।
15. Studio Editor में clip segments का order, trim/split, speed, canvas, text और audio बदलकर **Export edited video** दबाएं।
16. Save होने के बाद share करें या **Edit in Studio** से फिर edit करें।

### Option B — Offline AI
1. पहली बार model download करने के लिए Internet से जुड़ें।
2. **Settings** खोलें।
3. **OFFLINE MODEL MANAGER** में **Download offline AI models · ~577 MB** दबाएं।
4. Qwen3 और Whisper Base download होते समय ऐप खुला रखें।
5. जाँचें कि दोनों model statuses **Downloaded** दिखा रहे हैं।
6. **AI MODE** में **Offline local AI · Qwen3 + Whisper** चुनें।
7. **Save AI settings** दबाकर Editor में वापस आएं।
8. Local video चुनें और **Auto-transcribe + find clips** दबाएं।
9. Whisper locally transcript बनाएगा और Qwen3 timestamped transcript का analysis करेगा।
10. Suggestion cards देखें; हर topic के exact timestamps दिखाई देंगे। जिन topics को चाहिए, उन पर **Add this topic to Shorts queue** tick करें।
11. हर topic के लिए अलग MP4 बनाने हेतु **Export N queued topics as separate Shorts** दबाएं; सभी को एक timeline में edit करने के लिए **Open Studio Editor · N topic(s) as one timeline** दबाएं।
12. Batch export confirm करें और ऐप खुला रखें। हर topic अलग MP4 के रूप में Movies/ShortsForge में save होगा।

Models download होने के बाद local transcription और transcript analysis बिना audio/transcript को cloud AI service भेजे चल सकते हैं। बाद में models दोबारा download करने या cloud AI उपयोग करने के लिए Internet चाहिए।

### Option C — पहले से मौजूद Transcript इस्तेमाल करें
1. Source video चुनें।
2. Timestamped transcript को **TRANSCRIPT** में paste करें या **Import subtitles / transcript (SRT, VTT, TXT)** दबाएं।
3. Settings में Groq, OpenAI या Claude चुनकर संबंधित API key save करें।
4. Editor में वापस आकर **Analyze pasted transcript** दबाएं।
5. Ranked suggestions देखें, एक चुनें, selected range preview करें और export करें।

### Option D — YouTube preview + authorized video source
1. **YOUTUBE VIDEO URL** में YouTube watch, Shorts या youtu.be link paste करें।
2. **Preview YouTube URL + mark clip timestamps** दबाएं।
3. Embedded player में play/seek करें। **Set IN at playhead** और **Set OUT at playhead** दबाएं, या seconds fields manually बदलें।
4. **Use these clip markers** दबाकर timestamps main editor में वापस लाएं।
5. उसी recording की original video file या creator/rightsholder से मिला direct HTTPS video-file URL प्राप्त करें।
6. Direct URL के लिए उसे **AUTHORIZED DIRECT VIDEO FILE URL** में paste करें, **Import authorized video URL** दबाएं, permission confirm करें और download/validation पूरा होने दें।
7. सुनिश्चित करें कि imported file वही recording है जिसे आपने preview किया था। Duration में fit होने पर saved markers Start/End में prefill होते हैं।
8. **Auto-transcribe + find clips** चलाएं या transcript import करें, फिर Studio Editor में edit करके export करें।

**महत्वपूर्ण:** YouTube preview से watch URL editable source नहीं बनता। AI transcription और MP4 export के लिए local या authorized direct media file जरूरी है। ऐप public YouTube captions अपने-आप fetch नहीं करता।

## 7. Editor का विस्तृत उपयोग

### Step 1: Source video चुनें
Local file के लिए **Choose video** इस्तेमाल करें या creator-authorized HTTPS media-file link को **AUTHORIZED DIRECT VIDEO FILE URL** से import करें। Media validation के बाद filename और duration दिखते हैं। YouTube URL को **Preview YouTube URL + mark clip timestamps** से embedded preview किया जा सकता है; यह केवल preview और time markers देता है, editable video file नहीं।

### Step 2: Transcript बनाएं
इन तरीकों में से एक चुनें:
- Cloud: Groq या OpenAI चुने गए वीडियो से audio निकालकर timestamped transcription के लिए provider को भेज सकते हैं।
- Offline: Model install होने के बाद Whisper Base फोन पर ही transcription करता है।
- Existing transcript: Text field में paste करें या SRT/VTT/TXT file import करें।

Cloud transcription में निकला audio चुने गए provider को भेजा जाता है। Offline Whisper audio को डिवाइस पर ही process करता है।

### Step 3: महत्वपूर्ण Moments खोजें
**Auto-transcribe + find clips** या **Analyze pasted transcript** दबाएं। AI timestamps और छोटे explanation के साथ suggestions लौटाता है। विश्वसनीय timestamps के बिना transcript से उपयोगी clip suggestions नहीं मिल सकते।

### Step 4: Suggestion चुनें
पहला suggestion अपने-आप select होता है और उसके card पर **SELECTED** दिखता है। दूसरा card दबाने पर उस topic के exact Start/End timestamps main editor में load होते हैं। Screenshot में दिखाए गए timestamps सीधे इस्तेमाल होते हैं; वे अपने-आप 60/90-second window में नहीं बदले जाते।

जिन topics को export करना है उन पर **Add this topic to Shorts queue** tick करें। Queue किए गए topics अलग-अलग MP4 Shorts बन सकते हैं या Studio में एक timeline बन सकते हैं।

**Optional:** वर्तमान selected topic के लिए **Expand selected topic to 60s / 90s** इस्तेमाल करें। Preview करें और जरूरत पर Start/End बदलें।

### Step 5: Preview और Edit
**Preview selected clip** दबाने पर playback चुने गए Start समय से शुरू होकर End समय पर रुकता है। इसके अलावा:
- Start और End समय seconds में type कर सकते हैं।
- 15s, 30s, 45s और 60s quick duration buttons इस्तेमाल कर सकते हैं।
- AI selection window को 60 या 90 seconds में बदल सकते हैं।

यह preview source video में चुनी गई time range को जाँचता है। Final crop/aspect ratio और text overlay export के समय apply होते हैं, इसलिए exported MP4 भी जांचें।

### Step 6: Output सेट करें और Export करें
- Shorts-style layout के लिए Vertical 9:16।
- Square के लिए 1:1।
- Landscape के लिए 16:9।

Optional text overlay जोड़ें (अधिकतम 140 characters)। एक clip के लिए **Export Short** दबाएं। कई queued topics को अलग-अलग MP4 बनाने के लिए **Export N queued topics as separate Shorts** दबाकर confirm करें। हर video Movies/ShortsForge में save होता है। Batch पूरा होने तक ऐप खुला रखें और completion dialog में **Share saved Shorts** चुनें। सभी clips को एक ही Studio timeline में जोड़ने पर combined timeline की सीमा 180 seconds है।

## 8. AI Modes और Model Details

| Mode | काम | Internet/API key |
|---|---|---|
| Groq | Groq chat model highlights rank करता है; Groq Whisper transcription करता है | आवश्यक |
| OpenAI | OpenAI chat model highlights rank करता है; Whisper API transcription करता है | आवश्यक |
| Claude | दिया गया timestamped transcript analyse करता है | आवश्यक |
| Offline local AI | Whisper Base transcription; Qwen3 0.6B Q4 transcript moments analyse करता है | Models पहले download करें |

मौजूदा source code में model identifiers:
- Groq highlight analysis: openai/gpt-oss-120b.
- Groq transcription: whisper-large-v3-turbo.
- OpenAI highlight analysis: gpt-4o-mini.
- OpenAI transcription: whisper-1.
- Claude transcript analysis: claude-haiku-4-5-20251001.
- Local highlight analysis: Qwen3-0.6B-Q4_0.gguf.
- Local transcription: ggml-base.bin (Whisper Base).

Model names, API availability, usage limits और prices बदल सकते हैं। ऊपर दिए गए IDs मौजूदा code का विवरण हैं; provider भविष्य में इन्हें जारी रखेगा, इसकी गारंटी नहीं है।

## 9. Transcript Format सुझाव

सबसे भरोसेमंद transcript में हर बोले गए segment के साथ timestamp होना चाहिए। उदाहरण:

    [00:01.20 - 00:04.50] आज की चर्चा में आपका स्वागत है।
    [00:04.60 - 00:09.10] आज हम Artificial Intelligence पर बात कर रहे हैं।

सामान्य subtitle cue times वाली SRT/VTT files इसी style में बदली जाती हैं। Plain TXT file का text जैसा है वैसा रखा जाता है; TXT से सटीक clip times पाने के लिए उसमें timestamps शामिल करें।

Transcript:
- चुने गए source video से मेल खाना चाहिए।
- Timestamps सही क्रम में होने चाहिए।
- प्रश्न और उत्तर का अर्थ समझने लायक context होना चाहिए।
- Missing या मनगढ़ंत timestamps नहीं होने चाहिए।

## 10. Data Handling और Privacy

- Video selection और MP4 export Android डिवाइस पर होते हैं।
- YouTube preview embedded YouTube player से दिखता है। Direct video-file URL HTTPS के जरिए request होता है और media editing के लिए ऐप की private storage में save होता है।
- Cloud transcription में extracted audio चुने गए provider को भेजा जाता है।
- Cloud highlight analysis में transcript text चुने गए AI provider को भेजा जाता है।
- Local Whisper और Qwen3 inference model files download होने के बाद डिवाइस पर चलते हैं।
- ऐप की cloud API keys Android Keystore और AES-GCM के जरिए encrypt करके app preferences में रखी जाती हैं।
- Offline model files ऐप की private storage में रखी जाती हैं।
- मौजूदा ऐप में account-based cloud project sync या remote media library नहीं है।

Private या confidential recordings cloud API को भेजने से पहले provider की privacy terms पढ़ें। API keys को source code, README, screenshots या public issue reports में न डालें।

## 11. मौजूदा सीमाएँ — क्या शामिल नहीं है

निम्न सुविधाएँ **वर्तमान Version 1.13.0 में उपलब्ध नहीं हैं**:
- ShortsForge के अंदर native YouTube stream extraction/downloading करके editable MP4/audio source बनाना उपलब्ध नहीं है। Version 1.12.0 link को अलग से install किए गए YTDLnis app को भेज सकता है; download वहाँ पूरा करें, फिर saved file को ShortsForge में Share करें या Downloads से चुनें। URL अकेला editable source file नहीं है।
- Public YouTube caption tracks या transcripts का automatic download।
- Google Drive/Dropbox share-page URLs का import। Importer को direct HTTPS video-file response चाहिए।
- Automatic YouTube channel upload, scheduling या publishing।
- YouTube caption-track automatic download।
- AI-generated animated word-by-word subtitles या karaoke captions।
- Automatic B-roll, images, scene generation या presenter generation।
- पूर्ण CapCut/KineMaster स्तर का multi-source, multi-layer editing: picture-in-picture tracks, transitions, keyframes, filters, advanced colour grading, waveform editing, independent audio volume automation और frame-by-frame professional tools।
- Automatic video title, description, hashtags या thumbnail generation।
- Server-side user account, cloud project sync या team collaboration।

Studio Editor में styles वाला static text overlay है; animated subtitle editor अभी नहीं है। मौजूदा timeline एक selected source video के ranges को क्रमबद्ध करती है और एक music track source audio को replace करता है। अलग-अलग multi-track audio mixing नहीं है। AI transcript text का विश्लेषण करता है; अभी visual scene understanding या face tracking नहीं करता।

## 12. Future Roadmap — विचार, अभी लागू नहीं हुए

आगे विचार किए जा सकने वाले सुधार:
- Editable styles और positions के साथ animated word-level captions।
- बेहतर crop controls, subject-aware reframing और safe-area guides।
- Multi-layer timeline editing, transitions, keyframes, filters, independent audio mixing और frame-accurate controls वाला advanced timeline।
- Saved editing projects और export history।
- Audio cleanup और licensed background-music workflows।
- Transcription और transcript analysis के लिए अधिक language controls।
- केवल supported और authorized platform APIs से optional publishing integrations।

ये केवल भविष्य के ideas हैं। इन्हें अभी APK में उपलब्ध सुविधाएँ न समझें।

## 13. Troubleshooting

**ऐप कहता है कि API key उपलब्ध नहीं है**  
Settings खोलें, provider चुनें, उसी provider की API key डालें, Save AI settings दबाएं और Editor में लौटें।

**Claude मेरा वीडियो transcribe नहीं कर रहा**  
मौजूदा version में यह expected behaviour है। Claude timestamped transcript analyse करता है। पहले Groq/OpenAI या local Whisper से transcript बनाएं या import/paste करें।

**कोई highlight suggestions नहीं आतीं**  
जाँचें कि transcript खाली न हो और उसमें विश्वसनीय timestamps हों। सुनिश्चित करें कि transcript चुने गए वीडियो से संबंधित है। लंबा transcript या Cloud model आज़माएं।

**Offline AI models download करने को कहता है**  
Settings → OFFLINE MODEL MANAGER खोलकर दोनों files download करें। कम-से-कम 700 MB खाली storage और स्थिर Internet रखें।

**Video open/export नहीं हो रहा**  
हो सकता है video codec Android डिवाइस पर supported न हो, audio track न हो या file खराब हो। H.264 video और AAC audio वाला MP4 आज़माएं।

**Export duration गलत लग रही है**  
Start और End fields जांचें। AI की 60/90-second windows source-video duration के भीतर रहती हैं। Manual export 180 seconds से अधिक नहीं हो सकता।

**Export किया गया वीडियो crop दिख रहा है**  
ऐप चुने गए aspect ratio को भरने के लिए video scale करता है। इससे source frame के कुछ हिस्से कट सकते हैं। दूसरा ratio चुनें और final MP4 देखें।

**नया APK पुराने version पर install नहीं हो रहा**  
अलग debug signing certificates के कारण Android APK को reject कर सकता है। जरूरत पड़ने पर पुराने build को uninstall करके नया install करें। इससे local API keys और model files हट जाएंगी।

## 14. Technology Stack

- Kotlin और Native Android Views।
- File selection के लिए Android Activity Result API और Storage Access Framework।
- Media processing के लिए Android MediaCodec / MediaExtractor / MediaMuxer।
- Source duration के लिए Android MediaMetadataRetriever।
- Playback के लिए AndroidX Media3 ExoPlayer।
- MP4 export और overlays के लिए AndroidX Media3 Transformer और Effects।
- Cloud API requests, offline-model downloads और authorized direct video-file imports के लिए OkHttp।
- Embedded preview और clip timestamp markers के लिए Android WebView + YouTube IFrame Player API।
- Cloud AI के लिए Groq, OpenAI और Anthropic APIs।
- Local Qwen3 inference के लिए llama.cpp Android integration।
- Local Whisper transcription के लिए whisper.cpp Android integration।
- API-key encryption के लिए Android Keystore और AES-GCM।
- APK build और release publication के लिए GitHub Actions।

## 15. Repository Structure

    .
    ├── android/
    │   ├── app/
    │   │   ├── build.gradle
    │   │   └── src/main/
    │   │       ├── AndroidManifest.xml
    │   │       └── java/com/shortsforge/mobile/
    │   │           ├── MainActivity.kt
    │   │           ├── YouTubePreviewActivity.kt
    │   │           └── StudioEditorActivity.kt
    │   ├── build.gradle
    │   └── settings.gradle
    ├── .github/
    │   └── workflows/
    │       └── android-apk.yml
    └── README.md

## 16. Source से Android App Build करना

### Build Environment
- JDK 17।
- Android SDK platform 35।
- Android SDK Build Tools 35.0.0।
- Gradle 8.9 (GitHub Actions workflow यही version install करता है)।
- Gradle dependencies और Android libraries download करने के लिए Internet।

### Build Command

Repository root से:

    cd android
    gradle --no-daemon assembleDebug

आम तौर पर generated APK यहाँ मिलेगा:

    android/app/build/outputs/apk/debug/app-debug.apk

GitHub Actions workflow:
1. Repository checkout करता है।
2. Java 17 setup करता है।
3. Android SDK platform/build tools install करता है।
4. Gradle 8.9 setup करता है।
5. assembleDebug चलाता है।
6. APK का नाम ShortsForge-Mobile.apk करता है।
7. Workflow artifact upload करता है।
8. APK को mobile-latest GitHub release में publish करता है।

android/ या workflow file में बदलाव push करने पर build trigger हो सकता है। workflow_dispatch से इसे manually भी शुरू किया जा सकता है।

## 17. Latest Release

- Release: 1.12.0।
- Version code: 16।
- APK size: लगभग 13.93 MB।
- Build result: Successful।
- Build run: [GitHub Actions run 44459451960](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44459451960)।
- APK: [ShortsForge-Mobile.apk](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk)।
- SHA-256: `ac4a7e39a21ffffaf2d93516f7e89f6d1d9b3ab933a6c1ad511e9be051387e08`।

Computer पर downloaded file verify करने के लिए उसका SHA-256 hash calculate करके ऊपर दिए गए value से compare करें।

## 18. Copyright, Permissions और Project Status

केवल वही वीडियो चुनें, edit करें और publish करें जिनके आप owner हैं, जिनके लिए आपके पास permission है, या जिनका इस्तेमाल कानूनन कर सकते हैं। कम duration, credit देना या “no copyright intended” disclaimer अपने-आप permission नहीं देता। AI highlight suggestion से footage reuse करने का copyright अधिकार नहीं मिलता।

ShortsForge Mobile YouTube watch-page video stream को extract या download नहीं करता। YouTube link से केवल embedded preview और timestamp markers मिलते हैं। AI processing और export के लिए local file या creator-authorized direct video-file source जरूरी है।

APK build GitHub Actions में सफलतापूर्वक verify हुआ है। हर Android डिवाइस पर इसका स्वतंत्र परीक्षण नहीं हुआ है। Playback, local-model performance, export time और compatibility डिवाइस के अनुसार बदल सकते हैं।

इस README में कोई open-source software license घोषित नहीं है। केवल public repository होने का अर्थ यह नहीं है कि आपको source code reuse करने की अनुमति मिल गई है। Modified versions वितरित करने से पहले LICENSE file जोड़ें या license की समीक्षा करें।


---

## Version history — how the current app accumulated its features

This history is reconstructed from the repository's versioned source commits and README records. The GitHub `mobile-latest` release is a rolling download, so previous APK binaries are not all retained as separate downloadable releases. The current cumulative app feature set is documented below and in the 1.11.0 section.

| Version milestone | Feature work carried forward |
|---|---|
| 1.1.0 | Rebuilt Android editor workflow, local video selection and preview, media/audio handling, MP4 export and Android file sharing. |
| 1.2.0 | Timestamped key-moment selection, importance ranking, 60s/90s clip options, subtitle/transcript support and authorised direct-file source handling. |
| 1.3.0–1.3.1 | Offline Qwen3 + Whisper engines, local transcription/topic ranking, offline model manager, cloud-to-local fallback and processing-state fixes. |
| 1.4.0–1.4.1 | In-app YouTube preview/source handling was explored; unsupported URL-as-video-source behavior was removed rather than presented as working. |
| 1.5.0 | Automatic initial highlight selection, persisted selection state and exact selected-clip preview. |
| 1.5.1 | Restored screenshot-style suggestion cards with selected-state UI. |
| 1.6.0 | Studio timeline editor: split, trim, reorder, undo, speed, text and audio controls. |
| 1.7.0 | Embedded YouTube preview with IN/OUT timestamp markers and authorised direct video-file URL import. |
| 1.8.0 | Multi-topic queue, queue-based separate MP4 exports and multi-topic Studio timeline entry. |
| 1.9.0 | Full chronological Video Topic Map, overlapping transcript analysis, duplicate-topic merging, topic playback and queue-all controls. |
| 1.10.0 | Removed the fixed global 60-topic cutoff, expanded per-window topic extraction and added duration to each topic card. |
| 1.11.0 | Added Android file-share handoff and Downloads picker for videos already downloaded by YTDLnis or another app; preserved the existing feature set. |
| 1.12.0 | Added YouTube URL hand-off to separately installed YTDLnis; import completed download through Android Share or Downloads. |
| 1.13.0 | Fixed JSON/timestamp parsing, aligned chunk-relative times, retained long topics, and auto-started analysis after source/transcript import. |

### Version history — తెలుగు

ఈ చరిత్ర repositoryలోని version commits మరియు README ఆధారంగా తయారైంది. GitHubలో `mobile-latest` ఒక rolling download tag; అందువల్ల పాత APK binaries అన్నీ వేర్వేరు downloadable releasesగా నిల్వ ఉండవు. ప్రస్తుత cumulative feature set కింద మరియు 1.11.0 వివరాల్లో ఉంది.

| Version | ఈ versionలో వచ్చిన ప్రధాన పనులు |
|---|---|
| 1.1.0 | Android editor workflow పునర్నిర్మాణం, local video selection/preview, audio handling, MP4 export, Android sharing. |
| 1.2.0 | Timestamp key-moment selection, importance ranking, 60s/90s options, subtitles/transcripts, అనుమతితో ఉన్న direct-file import. |
| 1.3.0–1.3.1 | Offline Qwen3 + Whisper, local transcription/analysis, model manager, cloud నుంచి local fallback, processing fixes. |
| 1.4.0–1.4.1 | YouTube in-app preview/source handling ప్రయత్నం; పనిచేయని URL-to-video-source ప్రవర్తనను తీసివేసి, పని చేస్తుందని తప్పుగా చూపించలేదు. |
| 1.5.0 | మొదటి highlight ఆటోమేటిక్ select, selection state నిల్వ, ఎంచుకున్న clip exact preview. |
| 1.5.1 | Screenshot తరహా suggestion cards మరియు SELECTED UI తిరిగి చేర్చడం. |
| 1.6.0 | Studio timeline editor: split, trim, reorder, undo, speed, text, audio controls. |
| 1.7.0 | YouTube embedded preview, IN/OUT timestamp markers, permission ఉన్న direct video-file URL import. |
| 1.8.0 | అనేక topics queue, ప్రతి topicకు వేర్వేరు MP4 export, multi-topic Studio timeline. |
| 1.9.0 | పూర్తి chronological Video Topic Map, overlapping transcript analysis, duplicate merge, topic playback, queue-all. |
| 1.10.0 | Fixed global 60-topic పరిమితి తొలగింపు, chunk-wise topic extraction పెంపు, ప్రతి cardలో duration. |
| 1.11.0 | YTDLnis లేదా ఇతర appలో ముందే download అయిన videoను Android Share ద్వారా import చేయడం, Downloads picker; పాత features అన్నీ కొనసాగింపు. |
| 1.12.0 | YouTube URLను వేరుగా install చేసిన YTDLnisకి పంపడం; download పూర్తయ్యాక వీడియోను Share/Downloads ద్వారా import చేయడం. |
| 1.13.0 | JSON/timestamp parsing మెరుగుదల, chunk-relative timestamps సరిచేయడం, long topicsను mapలో ఉంచడం, source/transcript import తర్వాత auto-analysis. |

### संस्करण इतिहास — हिन्दी

यह इतिहास repository के version commits और README रिकॉर्ड से तैयार किया गया है। GitHub का `mobile-latest` एक rolling download tag है; इसलिए सभी पुराने APK binaries अलग-अलग downloadable releases के रूप में सुरक्षित नहीं हैं। मौजूदा cumulative feature set नीचे और Version 1.11.0 विवरण में दिया गया है।

| Version milestone | जोड़ी गई मुख्य सुविधाएँ |
|---|---|
| 1.1.0 | Android editor workflow का पुनर्निर्माण, local video चयन/preview, audio handling, MP4 export और Android sharing. |
| 1.2.0 | Timestamp key-moment selection, importance ranking, 60s/90s विकल्प, subtitle/transcript support और अधिकृत direct-file import. |
| 1.3.0–1.3.1 | Offline Qwen3 + Whisper, local transcription/analysis, model manager, cloud-to-local fallback और processing fixes. |
| 1.4.0–1.4.1 | YouTube in-app preview/source handling पर काम; unsupported URL-to-video-source व्यवहार हटाया गया ताकि उसे working feature बताकर भ्रम न हो. |
| 1.5.0 | पहला highlight अपने-आप select, selection state सुरक्षित रखना और exact clip preview. |
| 1.5.1 | Screenshot-जैसे suggestion cards और SELECTED status UI वापस जोड़ा गया. |
| 1.6.0 | Studio timeline editor: split, trim, reorder, undo, speed, text और audio controls. |
| 1.7.0 | YouTube embedded preview, IN/OUT timestamp markers और अधिकृत direct video-file URL import. |
| 1.8.0 | Multi-topic queue, हर topic का अलग MP4 export और multi-topic Studio timeline. |
| 1.9.0 | पूर्ण chronological Video Topic Map, overlapping transcript analysis, duplicate merge, topic playback और queue-all. |
| 1.10.0 | Fixed global 60-topic सीमा हटाई, per-window topic extraction बढ़ाया और हर topic card में duration जोड़ा. |
| 1.11.0 | YTDLnis या दूसरे app में पहले से डाउनलोड की गई video को Android Share और Downloads picker से import करना; पुराने features सुरक्षित रखे. |
| 1.12.0 | YouTube URL को अलग से install किए गए YTDLnis app में भेजना; download के बाद वीडियो को Share/Downloads से import करना. |
| 1.13.0 | JSON/timestamp parsing सुधार, chunk-relative timestamp alignment, लंबे topics को map में रखना और source/transcript import के बाद auto-analysis. |

---

# Version 1.11.0 update — English

## Import a video already downloaded by YTDLnis or another Android app

ShortsForge 1.11.0 adds Android video-share integration. It is designed for a video file that is already present on the device and that you are authorised to edit.

### Method A — Share a downloaded file from YTDLnis
1. Install YTDLnis separately from its [official GitHub repository](https://github.com/deniscerri/ytdlnis) if you use that app.
2. Only obtain and edit videos where you have the necessary rights and where the source service permits the download.
3. In YTDLnis, open the downloaded video and use Android's **Share** action when the app offers a video-file share.
4. Choose **ShortsForge Mobile** from Android's share sheet.
5. Confirm that you own the video or have permission to edit it.
6. ShortsForge copies the shared file into its private `shared-video-imports` folder (maximum 2 GB per shared import), selects it as the current source, reads its duration, and loads it into the preview.
7. Continue with transcription, Full Video Topic Map, topic cards, selection queue, Studio Editor, and MP4 export.

### Method B — Choose the downloaded video from storage
1. Open ShortsForge.
2. In **01 · SOURCE VIDEO**, tap **Choose downloaded video · YTDLnis / Downloads**.
3. Browse to the folder where the video was saved and select the video file.
4. The file becomes the selected source using the existing Android document picker.

The original file in Downloads is not modified. The shared-file method makes a private app copy, so it uses additional device storage. Clearing app data or uninstalling ShortsForge removes that private copy.

### What the integration does — and does not do
- Supports Android `ACTION_SEND`, `ACTION_SEND_MULTIPLE` (first shared video only), and `ACTION_VIEW` for video-file URIs.
- A shared video is copied locally before analysis so the temporary share permission does not expire during later editing.
- Shared text/YouTube links are recognised and can populate the URL field, but are **not** downloaded or converted into an editable media file.
- YouTube watch/Shorts URLs remain embedded-preview and timestamp-marker inputs only.
- ShortsForge does **not** bundle yt-dlp, YTDLnis, downloader cookies, PO-token handling, or YouTube stream extraction.
- This project does not copy or derive from YTDLnis source code. YTDLnis is a separate GPL-3.0 project; see its [license and project terms](https://github.com/deniscerri/ytdlnis). Its licence does not grant YouTube download permission or override the source service's terms.

## Cumulative feature set retained in 1.11.0

This version builds on the existing ShortsForge Android project. The following previously implemented features remain part of the app:
- Local video selection, preview, duration display, and authorised direct HTTPS video-file import.
- YouTube embedded preview, play/pause/seek, and manual IN/OUT timestamp markers.
- Groq, OpenAI, and Anthropic Claude transcript-analysis options, with cloud API keys encrypted using Android Keystore.
- Groq/OpenAI Whisper transcription and offline Whisper Base transcription.
- Offline Qwen3 0.6B topic analysis and offline model manager.
- SRT, VTT, and timestamped TXT transcript import/paste.
- Chronological Full Video Topic Map, topic title, category, importance, explanation, start, end, and duration.
- Topic playback, topic selection, manual range editing, per-topic queue, queue-all, and separate MP4 batch export.
- 60/90-second expansion controls and quick duration presets where available.
- Studio timeline editing: clip segments from the selected source, split, trim, reorder, delete, undo, speed control, aspect ratio, text overlay, source mute, and one looping music bed.
- MP4 H.264/AAC export, Movies/ShortsForge output, cancellation, and Android sharing.

The Studio editor is not a full CapCut/KineMaster replacement: it does not yet provide independent multi-source video tracks, picture-in-picture layers, keyframe animation, a transitions library, animated word-level captions, or advanced multi-track audio mixing.

---

# Version 1.11.0 నవీకరణ — తెలుగు

## YTDLnis లేదా ఇతర Android యాప్‌లో ఇప్పటికే డౌన్‌లోడ్ చేసిన వీడియోను ఇంపోర్ట్ చేయడం

ShortsForge 1.11.0లో Android Share ద్వారా వీడియో ఫైల్‌ను నేరుగా ShortsForgeకి పంపే అవకాశం జోడించబడింది. మీరు చట్టబద్ధంగా ఉపయోగించడానికి హక్కులు ఉన్న, ఇప్పటికే ఫోన్‌లో ఉన్న వీడియో ఫైల్‌ను మాత్రమే ఇంపోర్ట్ చేయాలి.

### విధానం A — YTDLnis నుంచి Share చేయడం
1. అవసరమైతే [అధికారిక YTDLnis GitHub repository](https://github.com/deniscerri/ytdlnis) నుంచి YTDLnisను వేరుగా ఇన్‌స్టాల్ చేయండి.
2. వీడియోను డౌన్‌లోడ్ చేయడానికి మరియు ఎడిట్ చేయడానికి మీకు అవసరమైన హక్కులు ఉన్నప్పుడు, అలాగే source service అనుమతించినప్పుడు మాత్రమే ఉపయోగించండి.
3. YTDLnisలో ఇప్పటికే ఉన్న వీడియోను తెరిచి, ఆ యాప్‌లో video-file **Share** option అందుబాటులో ఉంటే దాన్ని ఎంచుకోండి.
4. Android Share Sheetలో **ShortsForge Mobile** ఎంచుకోండి.
5. ఆ వీడియో మీ సొంతదని లేదా దాన్ని edit చేయడానికి అనుమతి ఉందని నిర్ధారించండి.
6. ShortsForge ఆ ఫైల్‌ను తన private `shared-video-imports` folderలోకి కాపీ చేస్తుంది (ఒక్క shared import గరిష్ఠ పరిమితి 2 GB), దాన్ని current sourceగా ఎంచుకుని duration మరియు preview సిద్ధం చేస్తుంది.
7. తర్వాత transcription, Full Video Topic Map, topic cards, selection queue, Studio Editor, MP4 export ఉపయోగించండి.

### విధానం B — Downloads నుంచి ఎంచుకోవడం
1. ShortsForge తెరవండి.
2. **01 · SOURCE VIDEO**లో **Choose downloaded video · YTDLnis / Downloads** నొక్కండి.
3. వీడియో సేవ్ చేసిన folderకు వెళ్లి ఫైల్‌ను ఎంచుకోండి.
4. Android document picker ద్వారా ఆ వీడియో sourceగా ఎంచుకోబడుతుంది.

Downloadsలోని అసలు ఫైల్‌ను మార్చదు. Share ద్వారా ఇంపోర్ట్ చేస్తే ShortsForge తన private storageలో మరో కాపీ ఉంచుతుంది; అందువల్ల అదనపు storage అవసరం. App data clear చేసినా లేదా app uninstall చేసినా ఆ private copy తొలగిపోతుంది.

### ఈ integration ఏమి చేస్తుంది / ఏమి చేయదు
- Video URIల కోసం Android `ACTION_SEND`, `ACTION_SEND_MULTIPLE` (మొదటి వీడియో మాత్రమే), `ACTION_VIEW` intentsను స్వీకరిస్తుంది.
- Share permission తర్వాత ముగిసిపోకుండా, తర్వాతి analysis/editingలో ఉపయోగించేందుకు video fileను app-private storageలో కాపీ చేస్తుంది.
- Shared text లేదా YouTube URLను గుర్తించి URL fieldలో చూపించవచ్చు; కానీ దాన్ని download చేసి editable videoగా మార్చదు.
- YouTube watch/Shorts URLలు embedded preview మరియు timestamp markers కోసమే.
- ShortsForgeలో yt-dlp, YTDLnis downloader engine, downloader cookies, PO-token handling లేదా YouTube stream extraction కలపలేదు.
- YTDLnis source codeను ShortsForgeలో కాపీ చేయలేదు లేదా దాని derivativeను తయారు చేయలేదు. YTDLnis వేరే GPL-3.0 project; దాని [license మరియు project terms](https://github.com/deniscerri/ytdlnis) చూడండి. GPL license వల్ల YouTube వీడియోలు download చేయడానికి ప్రత్యేక అనుమతి లభించదు; source service నియమాలు కూడా వర్తిస్తాయి.

## 1.11.0లో కొనసాగుతున్న పాత ఫీచర్ల సమగ్ర జాబితా

ఈ version ఇప్పటికే ఉన్న ShortsForge Android project మీద నిర్మించబడింది. పాత versionsలో అమలు చేసిన కింది ఫీచర్లు అలాగే కొనసాగుతాయి:
- Local video ఎంపిక, preview, duration, అనుమతితో ఉన్న direct HTTPS video-file import.
- YouTube embedded preview, play/pause/seek, manual IN/OUT timestamp markers.
- Groq, OpenAI, Anthropic Claude analysis options; API keys Android Keystore AES-GCMతో encrypted.
- Groq/OpenAI Whisper transcription మరియు offline Whisper Base transcription.
- Offline Qwen3 0.6B topic analysis మరియు offline model manager.
- SRT, VTT, timestamped TXT transcript import/paste.
- Chronological Full Video Topic Map: title, category, importance, explanation, start, end, duration.
- Topic playback, topic selection, manual time editing, per-topic queue, queue-all, separate MP4 batch export.
- అవసరమైన చోట 60/90-second expansion మరియు quick duration presets.
- Studio timeline: source segments, split, trim, reorder, delete, undo, speed, aspect ratio, text overlay, source mute, ఒక looping music bed.
- H.264/AAC MP4 export, Movies/ShortsForge save, cancel, Android share.

Studio Editor ఇంకా పూర్తి CapCut/KineMaster ప్రత్యామ్నాయం కాదు. Independent multi-source video tracks, picture-in-picture, keyframe animation, transitions library, animated word-level captions, advanced multi-track audio mixing ఇంకా లేవు.

---

# Versión 1.11.0 — हिन्दी

## YTDLnis या किसी अन्य Android ऐप में पहले से डाउनलोड की गई वीडियो इम्पोर्ट करें

ShortsForge 1.11.0 में Android Share integration जोड़ा गया है। यह उस वीडियो फ़ाइल के लिए है जो पहले से आपके डिवाइस में मौजूद है और जिसे एडिट करने के अधिकार आपके पास हैं।

### तरीका A — YTDLnis से Share करें
1. जरूरत पड़ने पर [आधिकारिक YTDLnis GitHub repository](https://github.com/deniscerri/ytdlnis) से YTDLnis अलग से इंस्टॉल करें।
2. वीडियो केवल तभी डाउनलोड और एडिट करें जब आपके पास आवश्यक अधिकार हों और source service इसकी अनुमति देती हो।
3. YTDLnis में डाउनलोड की गई वीडियो खोलें और यदि video-file **Share** विकल्प उपलब्ध हो तो उसे चुनें।
4. Android Share Sheet में **ShortsForge Mobile** चुनें।
5. पुष्टि करें कि वीडियो आपका है या आपको उसे एडिट करने की अनुमति है।
6. ShortsForge फ़ाइल को अपने private `shared-video-imports` folder में कॉपी करता है (प्रति shared import अधिकतम 2 GB), उसे current source बनाता है और duration तथा preview लोड करता है।
7. इसके बाद transcription, Full Video Topic Map, topic cards, selection queue, Studio Editor और MP4 export इस्तेमाल करें।

### तरीका B — Downloads से वीडियो चुनें
1. ShortsForge खोलें।
2. **01 · SOURCE VIDEO** में **Choose downloaded video · YTDLnis / Downloads** दबाएँ।
3. उस folder में जाएँ जहाँ वीडियो सेव है और फ़ाइल चुनें।
4. Android document picker उस वीडियो को source के रूप में चुन लेगा।

Downloads में मौजूद original file बदली नहीं जाती। Share से import करने पर ShortsForge private storage में एक copy रखता है, इसलिए अतिरिक्त storage लगेगा। App data साफ़ करने या ऐप uninstall करने पर यह private copy हट जाएगी।

### यह integration क्या करता है और क्या नहीं
- Video URI के लिए Android `ACTION_SEND`, `ACTION_SEND_MULTIPLE` (केवल पहली वीडियो), और `ACTION_VIEW` intents स्वीकार करता है।
- आगे analysis और editing के दौरान share permission समाप्त न हो, इसलिए वीडियो फ़ाइल को app-private storage में कॉपी करता है।
- Shared text या YouTube URL को पहचानकर URL field में भर सकता है, लेकिन उसे डाउनलोड करके editable video file नहीं बनाता।
- YouTube watch/Shorts URL केवल embedded preview और timestamp markers के लिए हैं।
- ShortsForge में yt-dlp, YTDLnis downloader engine, downloader cookies, PO-token handling या YouTube stream extraction शामिल नहीं है।
- YTDLnis का source code ShortsForge में कॉपी नहीं किया गया है और यह उसका derivative नहीं है। YTDLnis एक अलग GPL-3.0 project है; इसकी [license और project terms](https://github.com/deniscerri/ytdlnis) देखें। GPL license YouTube से वीडियो डाउनलोड करने की विशेष अनुमति नहीं देता; source service की शर्तें भी लागू होती हैं।

## 1.11.0 में सुरक्षित रखे गए पुराने सभी प्रमुख फीचर

यह version पहले से मौजूद ShortsForge Android project पर आधारित है। पुराने versions के ये फीचर बने हुए हैं:
- Local video चुनना, preview, duration और अनुमति प्राप्त direct HTTPS video-file import.
- YouTube embedded preview, play/pause/seek और manual IN/OUT timestamp markers.
- Groq, OpenAI और Anthropic Claude analysis; API keys Android Keystore AES-GCM से encrypted.
- Groq/OpenAI Whisper transcription और offline Whisper Base transcription.
- Offline Qwen3 0.6B topic analysis और offline model manager.
- SRT, VTT और timestamped TXT transcript import/paste.
- Chronological Full Video Topic Map: title, category, importance, explanation, start, end और duration.
- Topic playback, चयन, manual timestamp editing, per-topic queue, queue-all और अलग-अलग MP4 batch export.
- जहाँ उपलब्ध हो 60/90-second expansion और quick duration presets.
- Studio timeline: source segments, split, trim, reorder, delete, undo, speed, aspect ratio, text overlay, source mute और एक looping music track.
- H.264/AAC MP4 export, Movies/ShortsForge में save, cancel और Android sharing.

Studio Editor अभी पूर्ण CapCut/KineMaster replacement नहीं है। Independent multi-source video tracks, picture-in-picture, keyframe animation, transitions library, animated word-level captions और advanced multi-track audio mixing अभी उपलब्ध नहीं हैं.


---

# Version 1.12.0 update — English

## Send a pasted video link to YTDLnis, then use the downloaded file as the ShortsForge source

Version 1.12.0 adds an app-to-app hand-off. Paste a YouTube URL in YOUTUBE VIDEO URL and tap Send URL to YTDLnis for video download.

1. Install YTDLnis separately from its official GitHub releases (https://github.com/deniscerri/ytdlnis/releases) if it is not already installed.
2. Paste the YouTube link in ShortsForge.
3. Tap Send URL to YTDLnis for video download. ShortsForge opens the installed YTDLnis package com.deniscerri.ytdl using its documented Android share intent with video mode and foreground interaction.
4. Review the download format/options in YTDLnis and start the download there. ShortsForge does not silently download in the background.
5. When the file is saved, use YTDLnis Share and choose ShortsForge Mobile, or return to ShortsForge and use Choose downloaded video · YTDLnis / Downloads.
6. Confirm you own the video or have permission to edit it. ShortsForge copies a shared file into its private import folder (2 GB maximum per shared import), selects it as the source, and loads its duration/preview.
7. Run transcription and the Full Video Topic Map, select topic cards, queue multiple topics, edit in Studio, and export MP4 Shorts.

If YTDLnis is not installed, the button offers to open the official releases page. Installation is a separate, user-controlled Android install; ShortsForge does not silently install another APK.

### Integration boundary
- This is an intent integration, not a bundled downloader. YTDLnis remains a separate GPL-3.0 project.
- ShortsForge does not copy YTDLnis source code, bundle yt-dlp, downloader cookies, PO-token handling, or YouTube stream extraction.
- You review and start each YTDLnis download there. The saved video then enters ShortsForge through Android file/share APIs.
- A successful download does not establish copyright permission. Use only videos you own or are authorised to download, edit, and publish, and follow source-service terms.

## Cumulative feature history — previous APK versions

- 1.1.0 — Native Android baseline: source-video selection, playback, editor/media workflow, export and Android file sharing.
- 1.2.0 — Timestamped clip workflow: key-moment suggestions, importance ranking, 60/90-second clip modes, transcript/subtitle import and authorised source-link handling.
- 1.3.0 — Offline AI engines: local Qwen3 and Whisper engines plus offline model controls.
- 1.3.1 — Offline fallback stability: local AI fallback when supported cloud calls fail, overlapping transcript analysis and busy-state fix.
- 1.4.0 — YouTube preview: embedded player, play/pause/seek and manual clip IN/OUT markers.
- 1.4.1 — Source-flow cleanup: removed unsupported generic source-URL import UI while preserving local video editing.
- 1.5.0 — Highlight selection: persistent selected highlight, automatic top suggestion selection and exact clip-range preview.
- 1.5.1 — Restored suggestion cards: screenshot-matched highlight cards, selected status and card details.
- 1.6.0 — Studio timeline editor: split, trim, reorder, delete, undo, speed, aspect ratio, text overlay, audio controls and MP4 export.
- 1.7.0 — Source-link handling: YouTube preview markers plus authorised direct HTTPS video-file import.
- 1.8.0 — Multi-topic Shorts: topic queue, multiple selections, Studio timeline hand-off and separate MP4 batch export with progress/cancel handling.
- 1.9.0 — Full Video Topic Map: chronological topic cards, duplicate merging, per-topic playback, queue-all and topic-wise selection.
- 1.10.0 — Expanded topic analysis: removed fixed global 60-topic cap; up to 16 Cloud and 10 Offline Qwen3 candidates per transcript window; content-led boundaries and topic duration display.
- 1.11.0 — YTDLnis file import bridge: choose saved files from Android storage or share downloaded video files from YTDLnis/other apps into ShortsForge.
- 1.12.0 — YTDLnis URL hand-off: send the pasted link to the separately installed YTDLnis app using its documented Android intent, then share/import the completed video file into ShortsForge.

The retained feature set includes Groq, OpenAI and Claude cloud analysis; cloud Whisper; offline Whisper Base and Qwen3 0.6B; SRT/VTT/TXT transcripts; chronological topic cards with title/time/duration/category/importance/explanation; topic preview/selection/queue; separate Shorts batch export; duration presets; Studio timeline editing; YouTube embedded preview; authorised direct-file import; MP4 export; Gallery save and Android sharing.

---

# Version 1.12.0 నవీకరణ — తెలుగు

## YTDLnisకి YouTube లింక్ పంపి, డౌన్‌లోడ్ అయిన వీడియోను ShortsForge Sourceగా ఎంచుకోవడం

Version 1.12.0లో URL hand-off జోడించబడింది. ShortsForgeలో YOUTUBE VIDEO URL బాక్స్‌లో YouTube లింక్ paste చేసి Send URL to YTDLnis for video download నొక్కండి.

1. YTDLnis ఫోన్‌లో లేకపోతే, అధికారిక GitHub Releases (https://github.com/deniscerri/ytdlnis/releases) నుంచి వేరుగా install చేయండి.
2. ShortsForgeలో YouTube URL paste చేయండి.
3. Send URL to YTDLnis for video download నొక్కండి. ShortsForge, YTDLnis documented Android share intent ద్వారా video modeలో ఆ యాప్‌ను తెరుస్తుంది.
4. Download format/optionsను YTDLnisలో పరిశీలించి, అక్కడే మీరు download ప్రారంభించండి. ShortsForge backgroundలో లేదా మీకు తెలియకుండా download చేయదు.
5. Download పూర్తయ్యాక YTDLnisలో Share ద్వారా ShortsForge Mobile ఎంచుకోండి. లేదా ShortsForgeకి తిరిగి వచ్చి Choose downloaded video · YTDLnis / Downloads నొక్కండి.
6. వీడియో మీ సొంతదని లేదా edit చేయడానికి అనుమతి ఉందని నిర్ధారించండి. ShortsForge shared fileను private import folderలో కాపీ చేస్తుంది (ఒక్క shared importకు గరిష్ఠం 2 GB), దాన్ని sourceగా ఎంచుకుని duration/preview లోడ్ చేస్తుంది.
7. Transcription, Full Video Topic Map, topic cards, multi-topic queue, Studio Editor, MP4 export కొనసాగించండి.

YTDLnis install చేయకపోతే, బటన్ అధికారిక releases page తెరవడానికి అవకాశం చూపిస్తుంది. మరో APK install చేయడం Androidలో మీరే చేసే వేరు చర్య; ShortsForge స్వయంగా install చేయదు.

### Integration పరిమితి
- ఇది రెండు apps మధ్య Android intent integration మాత్రమే; downloaderను ShortsForgeలో bundle చేయలేదు. YTDLnis వేరే GPL-3.0 project.
- YTDLnis source code, yt-dlp, downloader cookies, PO-token handling లేదా YouTube stream extractionను ShortsForgeలో కాపీ చేయలేదు.
- Download format ఎంచుకుని download ప్రారంభించడం YTDLnisలో మీరే చేయాలి. ఆ తర్వాత video fileను Android Share/File Picker ద్వారా ShortsForgeకి తీసుకురావచ్చు.
- Download చేయగలగడం వల్ల copyright permission ఆటోమేటిక్‌గా రాదు. మీ సొంత లేదా download/edit/publish అనుమతి ఉన్న వీడియోలనే ఉపయోగించండి; source service నియమాలను పాటించండి.

## పాత APK వెర్షన్ల నుంచి వచ్చిన ఫీచర్ల చరిత్ర

- 1.1.0 — Android ప్రాథమిక వెర్షన్: Local source video ఎంపిక, playback, editing/media workflow, export, Android file sharing.
- 1.2.0 — Timestamped clips: Key moments, importance ranking, 60/90-second modes, transcript/subtitle import, అనుమతితో source-link handling.
- 1.3.0 — Offline AI: Local Qwen3/Whisper engines, offline model settings.
- 1.3.1 — Offline fallback మెరుగుదల: Cloud request విఫలమైనప్పుడు local fallback, overlapping transcript analysis, busy-state fix.
- 1.4.0 — YouTube preview: In-app player, play/pause/seek, manual IN/OUT markers.
- 1.4.1 — Source UI cleanup: Unsupported generic source URL import తొలగింపు; local editing అలాగే ఉంచడం.
- 1.5.0 — Highlight selection: Selected card persistence, మొదటి suggestion auto-select, exact clip preview.
- 1.5.1 — Suggestion cards తిరిగి: Screenshot తరహా cards, selected status, card details.
- 1.6.0 — Studio Timeline: Split, trim, reorder, delete, undo, speed, aspect ratio, text overlay, audio controls, MP4 export.
- 1.7.0 — Source links: YouTube preview markers, అనుమతితో direct HTTPS video-file import.
- 1.8.0 — Multi-topic Shorts: Topic queue, multiple selections, Studio timeline hand-off, separate MP4 batch export, progress/cancel.
- 1.9.0 — Full Video Topic Map: Chronological cards, duplicate merge, per-topic playback, queue-all.
- 1.10.0 — Expanded Topic Analysis: Fixed global 60-topic cap తొలగింపు; ప్రతి transcript windowకు Cloud 16 / Offline Qwen3 10 candidates; content-led boundaries, card duration.
- 1.11.0 — YTDLnis file import: Android storage నుంచి saved video ఎంచుకోవడం లేదా YTDLnis/ఇతర app నుంచి video file Share చేయడం.
- 1.12.0 — YTDLnis URL hand-off: Paste చేసిన linkను YTDLnisకి పంపి download పూర్తయ్యాక video fileను ShortsForgeకి Share/import చేయడం.

ప్రస్తుత cumulative features: Groq/OpenAI/Claude cloud analysis, cloud Whisper, offline Whisper Base/Qwen3 0.6B, SRT/VTT/TXT transcript, chronological topic cards (title/time/duration/category/importance/explanation), topic preview/selection/queue, separate Shorts batch export, duration presets, Studio timeline, YouTube embedded preview, authorised direct-file import, MP4 export, Gallery save, Android sharing.

---

# Versión 1.12.0 — हिन्दी

## YTDLnis को YouTube लिंक भेजें और डाउनलोड की गई वीडियो को ShortsForge Source बनाएं

Version 1.12.0 में URL hand-off जोड़ा गया है। ShortsForge के YOUTUBE VIDEO URL बॉक्स में YouTube लिंक paste करें और Send URL to YTDLnis for video download दबाएँ।

1. यदि YTDLnis फोन में install नहीं है, तो इसे आधिकारिक GitHub Releases (https://github.com/deniscerri/ytdlnis/releases) से अलग से install करें।
2. ShortsForge में YouTube URL paste करें।
3. Send URL to YTDLnis for video download दबाएँ। ShortsForge, YTDLnis के documented Android share intent के जरिए video mode में उस app को खोलता है।
4. YTDLnis में download format/options देखें और download वहीं आप शुरू करें। ShortsForge background में या बिना आपकी पुष्टि download नहीं करता।
5. Download पूरा होने पर YTDLnis में Share चुनकर ShortsForge Mobile भेजें। या ShortsForge में लौटकर Choose downloaded video · YTDLnis / Downloads दबाएँ।
6. पुष्टि करें कि वीडियो आपकी है या आपको edit करने की अनुमति है। ShortsForge shared file को private import folder में copy करता है (प्रति shared import अधिकतम 2 GB), उसे source बनाता है और duration/preview लोड करता है।
7. इसके बाद transcription, Full Video Topic Map, topic cards, multi-topic queue, Studio Editor और MP4 export इस्तेमाल करें।

यदि YTDLnis install नहीं है, तो बटन official releases page खोलने का विकल्प दिखाता है। दूसरी APK install करना Android पर आपका अलग user-controlled कदम है; ShortsForge उसे अपने-आप install नहीं करता।

### Integration की सीमा
- यह Android intent के जरिए दो apps के बीच hand-off है; downloader ShortsForge में bundle नहीं है। YTDLnis अलग GPL-3.0 project है।
- YTDLnis source code, yt-dlp, downloader cookies, PO-token handling या YouTube stream extraction ShortsForge में copy नहीं किए गए हैं।
- Download format चुनना और download शुरू करना YTDLnis में आपको करना होगा। उसके बाद video file Android Share/File Picker से ShortsForge में लाई जा सकती है।
- किसी app से download संभव होना copyright permission नहीं देता। केवल वही वीडियो उपयोग करें जिसे download, edit और publish करने का अधिकार आपके पास है; source service की शर्तें मानें।

## पुराने APK Versions से आए Features का इतिहास

- 1.1.0 — Android आधार: Local source video चयन, playback, editing/media workflow, export, Android file sharing.
- 1.2.0 — Timestamp clips: Key moments, importance ranking, 60/90-second modes, transcript/subtitle import, authorised source-link handling.
- 1.3.0 — Offline AI: Local Qwen3/Whisper engines और offline model settings.
- 1.3.1 — Offline fallback सुधार: Cloud request failure पर local fallback, overlapping transcript analysis, busy-state fix.
- 1.4.0 — YouTube preview: In-app player, play/pause/seek, manual IN/OUT markers.
- 1.4.1 — Source UI cleanup: Unsupported generic source-URL import हटाया और local editing बरकरार रखी.
- 1.5.0 — Highlight selection: Selected card persistence, पहली suggestion auto-select, exact clip preview.
- 1.5.1 — Suggestion cards वापस: Screenshot-जैसे cards, selected status और details.
- 1.6.0 — Studio Timeline: Split, trim, reorder, delete, undo, speed, aspect ratio, text overlay, audio controls, MP4 export.
- 1.7.0 — Source links: YouTube preview markers और authorised direct HTTPS video-file import.
- 1.8.0 — Multi-topic Shorts: Topic queue, multiple selection, Studio hand-off, अलग MP4 batch export, progress/cancel.
- 1.9.0 — Full Video Topic Map: Chronological cards, duplicate merging, per-topic playback, queue-all.
- 1.10.0 — Expanded topic analysis: Fixed global 60-topic cap हटाया; Cloud 16 / Offline Qwen3 10 candidates प्रति transcript window; content-led boundaries और card duration.
- 1.11.0 — YTDLnis file import: Android storage से saved video चुनना या YTDLnis/अन्य app से video file Share करना.
- 1.12.0 — YTDLnis URL hand-off: Paste किए link को YTDLnis भेजना; download पूरा होने पर file को ShortsForge में Share/import करना.

वर्तमान cumulative features: Groq/OpenAI/Claude cloud analysis, cloud Whisper, offline Whisper Base/Qwen3 0.6B, SRT/VTT/TXT transcript, chronological topic cards (title/time/duration/category/importance/explanation), topic preview/selection/queue, separate Shorts batch export, duration presets, Studio timeline, YouTube embedded preview, authorised direct-file import, MP4 export, Gallery save और Android sharing.


---

# Version 1.13.0 update — English

This update addresses the supplied screen recording where timestamped transcript text was visible but the Video Topic Map showed no cards.

- Selecting a local video or importing a downloaded file shared from YTDLnis automatically starts transcription and topic mapping when Groq/OpenAI credentials or both offline models are already configured. Otherwise the app explains the required setup; it does not initiate cloud analysis without a key.
- Importing timestamped SRT/VTT/TXT starts topic mapping automatically. For manually pasted text, use **Analyze all topics in transcript**.
- The parser accepts JSON arrays and wrapper objects such as `topics`, `topic_map`, `segments`, `highlights`, and `results`.
- Timestamp values can be numeric seconds or MM:SS / HH:MM:SS strings; common title and description field aliases are also supported.
- Chunk-relative AI timestamps are shifted to the original video timeline when detected.
- Coherent topics longer than 180 seconds remain in the topic index. The 180-second restriction remains for a single MP4 Short export, so trim a longer topic before exporting it as one Short.
- The previous card design remains: title, start/end, duration, category, importance, explanation, selected state, topic playback and queue controls.
- For a topic longer than 180 seconds, select its card, edit Start/End to a range of 180 seconds or less, then queue it. The edited range is preserved for batch MP4 export and Studio; the original full-topic card timestamps remain unchanged.
- YTDLnis stays a separate app. ShortsForge sends a link to it and imports the completed video through Android Share or Downloads; native YouTube stream extraction is not bundled.

# Version 1.13.0 నవీకరణ — తెలుగు

మీ screen recordingలో timestamped transcript కనిపించినా Video Topic Mapలో cards రాకపోవడాన్ని ఈ update లక్ష్యంగా తీసుకుంది.

- Local video ఎంచుకున్నప్పుడు లేదా YTDLnis నుంచి download చేసిన fileను Share చేసినప్పుడు, Groq/OpenAI key లేదా రెండు offline models ముందుగానే సిద్ధంగా ఉంటే transcription మరియు topic mapping ఆటోమేటిక్‌గా ప్రారంభమవుతాయి. Setup లేకపోతే app అవసరమైన సూచనలు చూపిస్తుంది.
- Timestamp ఉన్న SRT/VTT/TXT import చేసిన వెంటనే topic mapping ప్రారంభమవుతుంది. Manually paste చేసిన transcript కోసం **Analyze all topics in transcript** నొక్కండి.
- JSON arraysతో పాటు `topics`, `topic_map`, `segments`, `highlights`, `results` వంటి wrapper objectsను parser అంగీకరిస్తుంది.
- Numeric seconds, MM:SS, HH:MM:SS time formats మరియు title/description aliasesను అంగీకరిస్తుంది.
- Chunk-relative timestampsను గుర్తిస్తే original video timelineకు మార్చుతుంది.
- 180 seconds కంటే ఎక్కువ ఉండే coherent topic కూడా Topic Mapలో కనిపిస్తుంది. ఒక్క Short exportకు 180-second limit కొనసాగుతుంది; పొడవైన topicను exportకు ముందు trim చేయాలి.
- పాత card design అలాగే ఉంటుంది: title, start/end, duration, category, importance, explanation, selected status, topic playback మరియు queue controls.
- 180 seconds కంటే ఎక్కువ topic కోసం card select చేసి Start/Endను 180 seconds లోపు మార్చి queue చేయండి. ఆ custom range batch MP4 export మరియు Studioలో అలాగే ఉపయోగించబడుతుంది; అసలు పూర్తి topic timestamps cardలో మారవు.
- YTDLnis వేరే appగానే ఉంటుంది. ShortsForge linkను దానికి పంపి, download అయిన fileను Android Share లేదా Downloads ద్వారా import చేస్తుంది; native YouTube stream downloaderను bundle చేయదు.

# Version 1.13.0 अपडेट — हिन्दी

यह update उस समस्या को ध्यान में रखकर बनाया गया है जिसमें screen recording में timestamp वाला transcript दिखा, लेकिन Video Topic Map में cards नहीं आए।

- Local video चुनने या YTDLnis से download की गई file Share करने पर, Groq/OpenAI key या दोनों offline models configured हों तो transcription और topic mapping अपने-आप शुरू होते हैं। Setup न होने पर ऐप निर्देश दिखाता है।
- Timestamp वाले SRT/VTT/TXT import के बाद topic mapping अपने-आप शुरू होती है। Manually paste किए transcript के लिए **Analyze all topics in transcript** दबाएँ।
- Parser JSON arrays और `topics`, `topic_map`, `segments`, `highlights`, `results` जैसे wrapper objects स्वीकार करता है।
- Numeric seconds, MM:SS, HH:MM:SS timestamps तथा title/description aliases स्वीकार किए जाते हैं।
- Chunk-relative timestamps पहचाने जाने पर original video timeline से align किए जाते हैं।
- 180 सेकंड से लंबे coherent topics भी Topic Map में रहेंगे। एक Short export की सीमा 180 सेकंड ही है; लंबे topic को export से पहले trim करें।
- पुराना card design बना रहेगा: title, start/end, duration, category, importance, explanation, selected status, topic playback और queue controls।
- 180 सेकंड से लंबे topic के लिए card चुनें, Start/End को 180 सेकंड या उससे कम करें और queue में जोड़ें। यह custom range batch MP4 export और Studio में सुरक्षित रहेगा; मूल full-topic timestamps card में नहीं बदलेंगे।
- YTDLnis अलग app रहेगा। ShortsForge link को YTDLnis भेजता है और download की गई file Android Share या Downloads से import करता है; native YouTube stream downloader शामिल नहीं है।


---

# Version 1.13.1 update — audio extraction compatibility

The screenshot supplied for this update showed that some videos played in ShortsForge but cloud transcription failed with `Failed to add the track to the muxer`. The former cloud transcription path copied the source audio codec directly into an MP4/M4A container using Android MediaMuxer. Some source audio codecs/containers are not accepted by that muxer.

Version 1.13.1 changes cloud transcription to use the same Android MediaCodec decode path already used by offline Whisper: source audio is decoded, downmixed to mono, resampled to 16 kHz, and written as a WAV file. The WAV is sent to Groq/OpenAI as `audio/wav`; the source video is not modified. This removes the source-codec-to-MP4 remux step that caused the reported failure. The 24 MB transcription upload safety limit remains.

This is an Android source/build fix. Build success confirms compilation and APK packaging, not a real-device test against the exact uploaded video. Videos whose audio codec cannot be decoded by the device's MediaCodec may still need a compatible source file or an imported timestamped transcript.

# Version 1.13.1 — తెలుగు

మీ screenshotలో వీడియో preview అవుతున్నా Groq/OpenAI transcription సమయంలో `Failed to add the track to the muxer` error వచ్చింది. పాత cloud path అసలు audio codecను MediaMuxer ద్వారా MP4/M4Aలో నేరుగా copy చేయడానికి ప్రయత్నించేది. కొన్ని source codecsను ఆ muxer అంగీకరించదు.

Version 1.13.1లో cloud transcription కూడా offline Whisper ఉపయోగించే Android MediaCodec decode మార్గాన్నే ఉపయోగిస్తుంది: source audioను decode చేసి monoగా మార్చి, 16 kHzకు resample చేసి WAVగా తయారు చేస్తుంది. ఆ WAVను Groq/OpenAIకు `audio/wav`గా పంపుతుంది. Source video మారదు; సమస్య కలిగించిన direct audio remux దశ తొలగించబడింది. 24 MB upload safety limit అలాగే ఉంది.

ఇది Android source/build fix. Build success అనేది compile/APK packaging విజయాన్ని మాత్రమే నిర్ధారిస్తుంది; మీ అసలు videoతో physical device test చేసినట్లు కాదు. Device MediaCodec decode చేయలేని audio codec ఉన్న వీడియోలకు compatible source file లేదా timestamped transcript అవసరం కావచ్చు.
