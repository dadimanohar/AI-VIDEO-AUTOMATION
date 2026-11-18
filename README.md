# ShortsForge Mobile

**AI-powered long-video to Shorts editor for Android.**  
Turn a long interview, podcast, lecture, or other video file into short clips by finding useful moments in its timestamped transcript, reviewing the suggested time ranges, and exporting an MP4.

[Download the latest Android APK](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk) · [Latest build status](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44271305685)

| App detail | Value |
|---|---|
| App name | ShortsForge Mobile |
| Current release | 1.6.0 |
| Version code | 10 |
| Android package | `com.shortsforge.mobile` |
| Minimum Android version | Android 8.0 (API 26) |
| APK size | About 13.88 MB |
| Export | MP4 (H.264 video + AAC audio) |
| Build type | Debug-signed APK from GitHub Actions |

> **Important source-video note:** This version edits a video file selected from your device. YouTube URL import has intentionally been removed. A YouTube watch link is not an editable video file in this app. ShortsForge does not download public YouTube videos or bypass YouTube restrictions.

---

# ENGLISH

## 1. Project overview

ShortsForge Mobile is an Android-first video clipping studio for creators, students, podcasters, educators, and editors who work with long-form recordings.

The app analyses a timestamped transcript and suggests up to eight useful moments. Suggestions are designed to prioritise complete question-and-answer exchanges, important announcements, key insights, explanations, predictions, surprising facts, and practical advice. You can inspect each suggestion, choose the best one, preview its time range, adjust the start and end, and export a vertical or other aspect-ratio MP4.

The app is a **clip-selection and export tool**. It is not currently a full video-generation suite, a YouTube downloader, or an automatic YouTube publishing service.

## 2. Project goal and aim

The main goal is to reduce the time spent manually searching long recordings for useful short-form moments.

The intended workflow is:

1. Select a video file stored on the Android device.
2. Generate a timestamped transcript using cloud Whisper or on-device Whisper, or import/paste an existing timestamped transcript.
3. Ask an AI model to rank the most useful moments.
4. Review the suggestion cards and their timestamps, category, importance score, and explanation.
5. Select a suggestion and create a 60-second or 90-second clip window around that moment.
6. Preview the selected time range and adjust it manually when needed.
7. Choose an output aspect ratio, optionally add a text overlay, and export an MP4.
8. Find the exported file in Movies/ShortsForge and share it using the Android share sheet.

## 3. Features in version 1.6.0

### Source video
- Select a video from device storage using Android's file picker.
- Show the selected filename and detected duration.
- Preview the source video using the in-app player.
- The source file is not intentionally overwritten by the export process.
- Supported video formats depend on the Android device's media decoders.

### Transcription and transcript import
- Automatic cloud transcription using Groq Whisper or OpenAI Whisper.
- Automatic on-device transcription using Whisper Base after the offline model is downloaded.
- Paste a timestamped transcript directly into the transcript editor.
- Import subtitle/transcript files with SRT, VTT, or TXT extensions.
- SRT/VTT cue timestamps are converted into timestamped transcript lines.
- TXT files should contain usable timestamps if the AI is expected to identify exact moments.

### AI highlight discovery
- Analyse transcript text with Groq, OpenAI, Claude, or local Qwen3.
- Return up to eight highlight suggestions in the cloud analysis flow.
- Prioritise:
  - Complete question-and-answer exchanges, including the question when it is needed to understand the answer.
  - Important announcements and new facts.
  - Key insights and explanations.
  - Predictions and practical advice.
  - Surprising or informative statements that retain their context.
- Each suggestion can show:
  - A short hook/title.
  - Start and end timestamps for the key moment.
  - A category.
  - An importance score from 1 to 10 when returned by the model.
  - A short explanation of why the moment matters.
- The first suggestion is selected automatically when results arrive.
- The selection is announced in the status line above the cards and in the fixed bottom export bar. The cards keep the same neutral appearance as in the reference screen.
- Tap another suggestion card to change the selected moment.

### Clip editing and preview
- Choose a 60-second or 90-second AI clip window.
- The app centres the selected window around the AI-suggested key moment and keeps it inside the source-video duration.
- Manually edit the Start and End values in seconds.
- Quick duration buttons: 15s, 30s, 45s, and 60s.
- Preview the selected start-to-end range before export.
- The manual quick-export limit is 180 seconds (3 minutes).

### ShortsForge Studio timeline editor (new in 1.6.0)
Open **Studio Editor · Timeline, split, speed, text, audio** from the main editor after choosing an AI suggestion. The post-export dialog also offers **Edit in Studio** to reopen the rendered MP4.
- Preview the source video in a dedicated dark editing workspace.
- Work with an ordered timeline of clip segments selected from the same source video.
- Add another source-video range by entering its start and end times.
- Split the selected segment at the current playhead.
- Delete a segment, move segments left/right to reorder them, and undo recent timeline changes.
- Trim the selected segment by editing Start and End times in seconds.
- Scrub the selected segment with the timeline playhead and preview controls.
- Change playback/export speed: 0.5×, 0.75×, 1×, 1.25×, 1.5×, or 2×.
- Choose vertical 9:16, square 1:1, or landscape 16:9 output.
- Add a text overlay (up to 140 characters), choose top/centre/bottom position, clean/bold/large style, and white/yellow/cyan text.
- Mute the source video's audio or add one looping background music track. In this version the selected music track replaces the source audio; separate track mixing and volume automation are not provided.
- Export the ordered timeline as one MP4 (H.264/AAC). Combined timeline duration is limited to 180 seconds.
- After export, share the MP4 or open it again in Studio Editor for another edit.

This is a first native timeline-editing stage, not full CapCut/KineMaster feature parity. The timeline segments currently reference ranges from one selected source video; this is not a multi-source multi-track video compositor.


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
8. Tap **Auto-transcribe + find clips**.
9. Wait for transcription and highlight analysis to complete.
10. Review the suggestion cards. The top suggestion is selected automatically.
11. Select a different card if preferred.
12. Choose 60 seconds or 90 seconds, then use **Preview selected clip**.
13. Adjust Start/End if necessary, select the aspect ratio, and optionally add a text overlay.
14. Tap **Open Studio Editor · Timeline, split, speed, text, audio** to refine the clip in the timeline editor, or use the main editor's **Export Short** for a quick export.
15. In Studio Editor, arrange/trim/split the clip segments, adjust speed, canvas, text and audio, then tap **Export edited video**.
16. After the result is saved, share it or open **Edit in Studio** to make another edit.

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
4. Return to the editor and tap **Analyze pasted transcript**.
5. Review the ranked suggestions, choose one, preview the selected range, and export.

## 7. Detailed guide to the editor

### Step 1: Select the source video
Tap **Choose video** and use the Android file picker. Choose a video for which you have editing rights. The app displays the filename and, when Android can read it, the source duration.

### Step 2: Create a transcript
Choose one of these approaches:
- Cloud: Groq or OpenAI can extract audio from the selected video and request timestamped transcription.
- Offline: Whisper Base transcribes the audio locally after the model is installed.
- Existing transcript: paste it into the text field or import an SRT/VTT/TXT file.

Cloud transcription sends the extracted audio to the selected provider. Offline Whisper processes the audio on the device.

### Step 3: Find important moments
Tap **Auto-transcribe + find clips** or **Analyze pasted transcript**. The AI returns suggestions with timestamps and a brief explanation. A transcript without reliable timestamps may not produce usable clip suggestions.

### Step 4: Select a suggestion
The first suggestion is automatically selected. Its card has a highlighted background and a **SELECTED** marker. Tap another card to use that moment instead. The selected suggestion sets the Start and End fields to a 60-second or 90-second window centred around the suggested moment, bounded by the source duration.

**Important:** The AI timestamp is an anchor around a key moment. If the complete answer or discussion is longer than the selected 60/90-second window, the complete context may not fit. Preview the selection and adjust Start/End manually before export.

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

Add optional overlay text (maximum 140 characters), or leave it blank. Tap **Export Short** and keep the app open while the export is in progress. When complete, the video is saved and the Android sharing options appear.

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
- Cloud transcription sends extracted audio to the provider you selected.
- Cloud highlight analysis sends transcript text to the selected AI provider.
- Local Whisper and Qwen3 inference run on the device after the model files are downloaded.
- The app's cloud API keys are stored in app preferences encrypted using AES-GCM and Android Keystore.
- Offline model files are stored in the app's private storage.
- The current app does not provide account-based cloud project sync or a remote media library.

Review the selected AI provider's privacy terms before sending private, confidential, or personal recordings to a cloud API. Do not put API keys in source code, README files, screenshots, or public issue reports.

## 11. Current limitations — what is not included

The following are **not** current 1.6.0 features:
- YouTube URL import or public YouTube video downloading.
- Google Drive/Dropbox share-link importing.
- Direct internet video URL importing.
- Automatic YouTube channel upload, scheduling, or publishing.
- Automatic YouTube caption-track downloading.
- AI-generated animated word-by-word subtitles or karaoke captions.
- Automatic B-roll, images, scene generation, or presenter generation.
- Batch export queue.
- Full multi-source, multi-layer CapCut/KineMaster editing: picture-in-picture tracks, transitions, keyframes, filters, advanced colour grading, waveform editing, independent audio volume automation, and frame-by-frame professional tools.
- Automatic video title, description, hashtags, or thumbnail generation.
- A server-side user account, cloud project sync, or team collaboration.

Studio Editor supports a styled static text overlay; it is not an animated subtitle editor. Studio timelines currently arrange ranges from one selected source video and support one added music track that replaces the source audio. They do not provide independent multi-track audio mixing. The AI analyses transcript text; it does not currently perform visual scene understanding or face tracking.

## 12. Future roadmap — ideas, not yet implemented

Potential future improvements, subject to design and testing:
- Animated word-level captions with editable styles and positioning.
- Better crop controls, subject-aware reframing, and safe-area guides.
- Multiple clip selection and batch export.
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
- OkHttp for cloud API requests and model downloads.
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
    │   │       └── java/com/shortsforge/mobile/MainActivity.kt
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

- Release: 1.6.0
- Version code: 10
- Build result: successful.
- Build run: [GitHub Actions run 44271305685](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44271305685)
- APK: [ShortsForge-Mobile.apk](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk)
- SHA-256: `864282ffd23129cdbf2cc25b18c8250528e10c6a65ec7e20a1964f2912f87ecd`

To verify a downloaded file on a computer, calculate its SHA-256 hash and compare it with the value above.

## 18. Copyright, permissions, and project status

Only select, edit, and publish videos that you own, have permission to use, or are otherwise legally allowed to use. A short duration, credit line, or “no copyright intended” disclaimer does not automatically grant rights. AI highlight suggestions do not grant permission to reuse footage.

ShortsForge Mobile does not provide a YouTube video downloader. It expects a local video file selected by the user.

The APK build was verified through GitHub Actions. It has not been independently tested on every Android device. Device-specific playback, local-model performance, export time, and compatibility may vary.

No open-source software license is declared in this README. Do not assume that public repository visibility grants permission to reuse this project's source code; add or review a LICENSE file before distributing modified versions.

---

# తెలుగు

## 1. ప్రాజెక్ట్ పరిచయం

ShortsForge Mobile అనేది Android ఫోన్ల కోసం తయారు చేసిన AI ఆధారిత వీడియో క్లిప్పింగ్ యాప్. పొడవైన ఇంటర్వ్యూలు, పోడ్‌కాస్ట్‌లు, లెక్చర్లు లేదా ఇతర వీడియోల నుంచి ఉపయోగకరమైన ముఖ్యమైన భాగాలను గుర్తించి, వాటిని చిన్న వీడియోలుగా ఎడిట్ చేసి MP4గా ఎగుమతి చేయడానికి ఇది సహాయపడుతుంది.

ఈ యాప్ వీడియోలోని మాటలను timestamp ఉన్న transcript రూపంలో విశ్లేషించి గరిష్ఠంగా 8 ముఖ్యమైన moments సూచిస్తుంది. ప్రతి suggestionలో title/hook, timestamp, category, importance score, వివరణ ఉంటాయి. మొదటి suggestion ఆటోమేటిక్‌గా select అవుతుంది. మీరు వేరే suggestion ఎంచుకోవచ్చు, దాని సమయాన్ని preview చేయవచ్చు, Start/End మార్చవచ్చు, చివరికి MP4 export చేయవచ్చు.

ఇది ప్రస్తుతం **వీడియో క్లిప్ ఎంచుకోవడం మరియు ఎగుమతి చేయడం కోసం ఉన్న యాప్**. ఇది పూర్తి video-generation suite కాదు, YouTube downloader కాదు, YouTubeలో ఆటోమేటిక్‌గా upload చేసే సేవ కూడా కాదు.

## 2. ప్రాజెక్ట్ లక్ష్యం

పొడవైన వీడియోలో ముఖ్యమైన భాగాలను చేతితో వెతకడానికి పట్టే సమయాన్ని తగ్గించడం ఈ ప్రాజెక్ట్ ప్రధాన లక్ష్యం.

ఉద్దేశించిన workflow:

1. Android ఫోన్‌లో ఉన్న వీడియో ఫైల్‌ను ఎంచుకోవాలి.
2. Groq/OpenAI cloud Whisper లేదా ఫోన్‌లోని local Whisper ద్వారా timestamp transcript తయారు చేయాలి; లేక ఇప్పటికే ఉన్న transcript import/paste చేయాలి.
3. AIతో ముఖ్యమైన moments‌ను rank చేయించాలి.
4. Suggestion cards, timestamps, category, importance score, explanation పరిశీలించాలి.
5. సరైన suggestion ఎంచుకుని దాని చుట్టూ 60 లేదా 90 సెకన్ల clip window తయారు చేయాలి.
6. Exportకు ముందు ఆ time range preview చేసి అవసరమైతే Start/End మార్చాలి.
7. Aspect ratio ఎంచుకుని, కావాలంటే text overlay జోడించి MP4 export చేయాలి.
8. Movies/ShortsForgeలో వీడియోను చూసి Android share menu ద్వారా share చేయాలి.

## 3. Version 1.6.0లో ప్రస్తుతం ఉన్న ఫీచర్లు

### Source video
- ఫోన్ storage నుంచి Android file picker ద్వారా వీడియో ఎంచుకోవచ్చు.
- ఎంచుకున్న వీడియో పేరు మరియు గుర్తించిన duration చూపిస్తుంది.
- యాప్‌లోనే source video preview చూడవచ్చు.
- Export సమయంలో source file‌ను ఉద్దేశపూర్వకంగా overwrite చేయదు.
- ఏ video formats పని చేస్తాయో ఫోన్‌లో ఉన్న Android media decoders‌పై ఆధారపడి ఉంటుంది.

### Transcript మరియు speech-to-text
- Groq Whisper ద్వారా cloud transcription.
- OpenAI Whisper ద్వారా cloud transcription.
- Whisper Base ద్వారా on-device offline transcription.
- Timestamp ఉన్న transcript‌ను text boxలో paste చేయవచ్చు.
- SRT, VTT, TXT transcript/subtitle files import చేయవచ్చు.
- SRT/VTT subtitle cue timestamps‌ను transcript lines‌గా మార్చుతుంది.
- TXTలో AIకి ఖచ్చితమైన clip సమయాలు కనుగొనడానికి ఉపయోగపడే timestamps ఉండాలి.

### AI highlight detection
- Groq, OpenAI, Claude లేదా local Qwen3తో transcript విశ్లేషణ.
- Cloud analysisలో గరిష్ఠంగా 8 highlight suggestions.
- AI ప్రధానంగా గుర్తించడానికి ప్రయత్నించేవి:
  - పూర్తి question-and-answer exchanges.
  - ముఖ్యమైన announcements మరియు కొత్త facts.
  - Key insights మరియు explanations.
  - Predictions మరియు practical advice.
  - Context కోల్పోకుండా అర్థవంతంగా ఉండే surprising statements.
- ప్రతి suggestionలో ఉండేవి:
  - చిన్న hook/title.
  - ముఖ్యమైన భాగం start/end timestamps.
  - Category.
  - Model ఇచ్చినప్పుడు 1–10 importance score.
  - ఆ భాగం ఎందుకు ఉపయోగకరమో చిన్న explanation.
- ఫలితాలు వచ్చినప్పుడు మొదటి suggestion ఆటోమేటిక్‌గా select అవుతుంది.
- ఎంచుకున్న suggestion వివరాలు cards పైన ఉన్న status lineలో మరియు కింద స్థిరంగా ఉండే export barలో కనిపిస్తాయి. Screenshotలో ఉన్నట్లుగానే cards neutral designలో ఉంటాయి.
- ఇంకో card నొక్కి వేరే moment ఎంచుకోవచ్చు.

### Clip editing మరియు preview
- AI clip window కోసం 60 లేదా 90 seconds ఎంచుకోవచ్చు.
- ఎంచుకున్న AI moment మధ్య భాగంగా ఉండేలా clip window తయారవుతుంది; source video పరిమితుల్లోనే ఉంటుంది.
- Start మరియు End సమయాలను secondsలో manualగా మార్చవచ్చు.
- Quick duration buttons: 15s, 30s, 45s, 60s.
- Exportకు ముందు ఎంచుకున్న time range preview చేయవచ్చు.
- Main editor quick-export గరిష్ఠ పరిమితి 180 seconds (3 నిమిషాలు).

### ShortsForge Studio Timeline Editor (1.6.0లో కొత్తది)
AI suggestion ఎంచుకున్న తర్వాత ప్రధాన editorలో **Open Studio Editor · Timeline, split, speed, text, audio** నొక్కండి. Quick export పూర్తయ్యాక వచ్చే dialogలో **Edit in Studio** ఎంచుకుని export అయిన MP4ను మళ్లీ తెరవవచ్చు.
- ప్రత్యేక dark editing workspaceలో source video preview.
- ఒకే source video నుంచి ఎంచుకున్న clip segments‌తో timeline.
- అదే source videoలో మరో start/end rangeను జోడించవచ్చు.
- Playhead ఉన్న స్థానంలో selected segmentను Split చేయవచ్చు.
- Segment delete చేయడం, ఎడమ/కుడికి move చేసి క్రమం మార్చడం, ఇటీవలి timeline మార్పులను Undo చేయడం.
- Selected segment Start/End సమయాలను secondsలో trim చేయడం.
- Timeline playhead ద్వారా seek/scrub చేసి preview చేయడం.
- Playback/export speed: 0.5×, 0.75×, 1×, 1.25×, 1.5× లేదా 2×.
- Vertical 9:16, Square 1:1, Landscape 16:9 output formats.
- 140 characters వరకు text overlay; Top/Centre/Bottom స్థానం; Clean/Bold/Large style; White/Yellow/Cyan text colors.
- Original video audio mute చేయవచ్చు లేదా looping background music file జోడించవచ్చు. ప్రస్తుత versionలో music track source audio స్థానంలో వస్తుంది; రెండు tracks volume mixing లేదా automation ఇంకా లేదు.
- Timelineలోని clip segments‌ను ఒక MP4 (H.264/AAC)గా export చేయవచ్చు. మొత్తం timeline గరిష్ఠంగా 180 seconds.
- Export తర్వాత MP4 share చేయవచ్చు లేదా Studio Editorలో మళ్లీ తెరిచి edit చేయవచ్చు.

ఇది native timeline editingకు మొదటి దశ మాత్రమే; పూర్తి CapCut/KineMaster సమాన feature set కాదు. ప్రస్తుత timelineలోని అన్ని clip segments ఒకే selected source videoలోని ranges‌ను ఉపయోగిస్తాయి; వేర్వేరు వీడియోలతో multi-track compositor ఇంకా లేదు.


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
11. కావాలంటే వేరే card ఎంచుకోండి.
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
10. Suggestion ఎంచుకుని preview చేయండి. అవసరమైతే time range మార్చి export చేయండి.

Models download చేసిన తర్వాత local transcription మరియు transcript analysis cloud AIకి audio/transcript పంపకుండా పనిచేయగలవు. తర్వాత models మళ్లీ download చేయడానికి లేదా cloud AI వాడడానికి Internet అవసరం.

### Option C — ఇప్పటికే transcript ఉంటే
1. Source video ఎంచుకోండి.
2. Timestamp ఉన్న transcript‌ను **TRANSCRIPT**లో paste చేయండి లేదా **Import subtitles / transcript (SRT, VTT, TXT)** నొక్కండి.
3. Settingsలో Groq, OpenAI లేదా Claude ఎంచుకుని అవసరమైన API key save చేయండి.
4. Editorకి తిరిగి వచ్చి **Analyze pasted transcript** నొక్కండి.
5. Suggestions పరిశీలించి ఒకటి select చేయండి, preview చేసి export చేయండి.

## 7. Editor‌ను వివరంగా ఉపయోగించడం

### Step 1: Source video ఎంచుకోవడం
**Choose video** నొక్కి Android file picker తెరవండి. మీకు ఎడిట్ చేసే హక్కు ఉన్న వీడియోను ఎంచుకోండి. App filename మరియు Android చదవగలిగితే video duration చూపిస్తుంది.

### Step 2: Transcript తయారు చేయడం
ఈ మార్గాల్లో ఒకటి ఎంచుకోండి:
- Cloud: Groq లేదా OpenAI ఎంచుకున్న వీడియో నుంచి audio extract చేసి timestamp transcription కోసం providerకు పంపగలవు.
- Offline: Model download అయిన తర్వాత Whisper Base ఫోన్‌లోనే transcription చేస్తుంది.
- Existing transcript: Text fieldలో paste చేయండి లేదా SRT/VTT/TXT file import చేయండి.

Cloud transcriptionలో extracted audio ఎంచుకున్న providerకు పంపబడుతుంది. Offline Whisper audio‌ను ఫోన్‌లోనే process చేస్తుంది.

### Step 3: ముఖ్యమైన moments కనుగొనడం
**Auto-transcribe + find clips** లేదా **Analyze pasted transcript** నొక్కండి. AI timestamps మరియు చిన్న వివరణతో suggestions ఇస్తుంది. సరైన timestamps లేని transcript నుంచి ఉపయోగకరమైన clip suggestions రాకపోవచ్చు.

### Step 4: Suggestion ఎంచుకోవడం
మొదటి suggestion ఆటోమేటిక్‌గా select అవుతుంది. దాని card highlighted backgroundతో **SELECTED** గుర్తు చూపిస్తుంది. ఇంకో card నొక్కి వేరే moment ఎంచుకోవచ్చు. Selected suggestion ఆధారంగా 60 లేదా 90 seconds clip window తయారవుతుంది. అది suggested moment చుట్టూ centre అవుతుంది మరియు source video duration దాటదు.

**ముఖ్యమైన విషయం:** AI timestamp అనేది ముఖ్యమైన moment‌కు anchor మాత్రమే. పూర్తి answer లేదా discussion 60/90 seconds కంటే ఎక్కువ ఉంటే మొత్తం context clipలో సరిపోకపోవచ్చు. Exportకు ముందు preview చేసి Start/End మార్చండి.

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

గరిష్ఠంగా 140 characters ఉన్న optional text overlay జోడించండి లేదా ఖాళీగా వదిలేయండి. **Export Short** నొక్కి export పూర్తయ్యే వరకు యాప్ openగా ఉంచండి. పూర్తయిన తర్వాత వీడియో save అవుతుంది; Android share options కనిపిస్తాయి.

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
- Cloud transcription కోసం extracted audio ఎంపిక చేసిన providerకు పంపబడుతుంది.
- Cloud highlight analysis కోసం transcript text ఎంపిక చేసిన AI providerకు పంపబడుతుంది.
- Local Whisper మరియు Qwen3 models download అయిన తర్వాత ఫోన్‌లోనే inference చేస్తాయి.
- API keys AES-GCM మరియు Android Keystore ఉపయోగించి encrypt చేసి app preferencesలో ఉంచబడతాయి.
- Offline model files యాప్ private storageలో ఉంటాయి.
- ప్రస్తుత యాప్‌లో account-based cloud project sync లేదా remote media library లేదు.

Private లేదా confidential recordingsను cloud APIకి పంపే ముందు ఆ provider privacy terms చదవండి. API keysను source code, README, screenshots లేదా public issue reportsలో పెట్టకండి.

## 11. ప్రస్తుత యాప్‌లో లేని ఫీచర్లు

క్రింది అంశాలు **Version 1.6.0లో లేవు**:
- YouTube URL import లేదా public YouTube video download.
- Google Drive/Dropbox share-link import.
- Direct internet video URL import.
- Automatic YouTube channel upload, scheduling లేదా publishing.
- YouTube caption-track automatic download.
- AI animated word-by-word subtitles లేదా karaoke captions.
- Automatic B-roll, images, scene generation లేదా presenter generation.
- Batch export queue.
- పూర్తి CapCut/KineMaster స్థాయి multi-source, multi-layer editing: picture-in-picture tracks, transitions, keyframes, filters, advanced colour grading, waveform editing, independent audio volume automation, frame-by-frame professional tools.
- Automatic video title, description, hashtags లేదా thumbnail generation.
- Server-side user account, cloud project sync లేదా team collaboration.

Studio Editorలో styles ఉన్న static text overlay ఉంది; animated subtitle editor ఇంకా లేదు. ప్రస్తుత timeline ఒకే selected source videoలోని ranges‌ను క్రమపరుస్తుంది; ఒక music track source audio స్థానంలో వస్తుంది. స్వతంత్ర multi-track audio mixing లేదు. AI transcript textనే విశ్లేషిస్తుంది; ప్రస్తుతం video visual scene understanding లేదా face tracking చేయదు.

## 12. భవిష్యత్తు Roadmap — ఇంకా నిర్మించని ఆలోచనలు

తరువాత పరిశీలించదగిన improvements:
- Edit చేయగల styles/positionsతో animated word-level captions.
- మెరుగైన crop controls, subject-aware reframing, safe-area guides.
- Multiple clips selection మరియు batch export.
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
- Cloud API requests మరియు model downloads కోసం OkHttp.
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
    │   │       └── java/com/shortsforge/mobile/MainActivity.kt
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

- Release: 1.6.0.
- Version code: 10.
- Build result: Successful.
- Build run: [GitHub Actions run 44271305685](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44271305685).
- APK: [ShortsForge-Mobile.apk](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk).
- SHA-256: `864282ffd23129cdbf2cc25b18c8250528e10c6a65ec7e20a1964f2912f87ecd`.

Computerలో download చేసిన file verify చేయాలంటే దాని SHA-256 hash calculate చేసి పై విలువతో పోల్చండి.

## 18. Copyright, Permissions మరియు Project Status

మీకు స్వంతమైన, అనుమతి ఉన్న లేదా చట్టపరంగా ఉపయోగించడానికి హక్కు ఉన్న వీడియోలనే ఎంచుకుని edit/publish చేయండి. చిన్న duration, credit ఇవ్వడం లేదా “no copyright intended” disclaimer ఉండటం వల్ల reuse permission ఆటోమేటిక్‌గా రాదు. AI highlight suggestions వల్ల వీడియోపై copyright permission లభించదు.

ShortsForge Mobile YouTube video downloader కాదు. User ఎంచుకున్న local video fileనే sourceగా ఉపయోగిస్తుంది.

APK build GitHub Actionsలో విజయవంతంగా verify అయింది. ప్రతి Android పరికరంలో స్వతంత్రంగా test చేయలేదు. Playback, local model performance, export time మరియు compatibility పరికరాన్ని బట్టి మారవచ్చు.

ఈ READMEలో open-source software license ప్రకటించలేదు. Public repositoryగా కనిపించడం వల్ల ఈ source codeను స్వేచ్ఛగా reuse చేయడానికి permission వచ్చిందని భావించకండి. Modified versions పంపిణీ చేసే ముందు LICENSE file జోడించండి లేదా licenseను పరిశీలించండి.

---

# हिन्दी

## 1. परियोजना का परिचय

ShortsForge Mobile Android के लिए बनाया गया AI-सहायता प्राप्त वीडियो क्लिपिंग ऐप है। यह लंबे इंटरव्यू, पॉडकास्ट, लेक्चर या अन्य वीडियो से उपयोगी हिस्से पहचानने, उन्हें छोटे क्लिप में संपादित करने और MP4 के रूप में एक्सपोर्ट करने में मदद करता है।

ऐप timestamp वाले transcript का विश्लेषण करके अधिकतम 8 उपयोगी moments सुझाता है। हर suggestion में title/hook, timestamp, category, importance score और explanation हो सकते हैं। पहला suggestion अपने-आप select होता है। आप दूसरा suggestion चुन सकते हैं, उसका समय preview कर सकते हैं, Start/End बदल सकते हैं और अंत में MP4 export कर सकते हैं।

यह अभी **क्लिप चुनने और एक्सपोर्ट करने का टूल** है। यह पूर्ण video-generation suite, YouTube downloader या अपने-आप YouTube पर वीडियो प्रकाशित करने वाली सेवा नहीं है।

## 2. परियोजना का लक्ष्य

लंबी recording में उपयोगी short-form moments को हाथ से खोजने में लगने वाला समय कम करना इस परियोजना का मुख्य लक्ष्य है।

इसका intended workflow:

1. Android फोन में मौजूद वीडियो फ़ाइल चुनें।
2. Groq/OpenAI cloud Whisper या फोन में मौजूद local Whisper से timestamp transcript बनाएं, या अपना transcript paste/import करें।
3. AI से सबसे उपयोगी moments की ranking करवाएं।
4. Suggestion cards, timestamps, category, importance score और explanation देखें।
5. एक suggestion चुनें और उसके आसपास 60 या 90 सेकंड की clip window बनाएं।
6. Export से पहले उस समय-सीमा का preview करें और जरूरत पड़ने पर Start/End बदलें।
7. Aspect ratio चुनें, चाहें तो text overlay जोड़ें और MP4 export करें।
8. वीडियो को Movies/ShortsForge में ढूंढें और Android share sheet से साझा करें।

## 3. Version 1.6.0 में उपलब्ध सुविधाएँ

### Source video
- Android file picker से फोन की storage में मौजूद वीडियो चुनें।
- चुने गए वीडियो का filename और उपलब्ध duration दिखाता है।
- ऐप के अंदर source video preview कर सकते हैं।
- Export प्रक्रिया source file को जानबूझकर overwrite नहीं करती।
- कौन-से video formats चलेंगे, यह Android डिवाइस के media decoders पर निर्भर करता है।

### Transcript और Speech-to-Text
- Groq Whisper से cloud transcription।
- OpenAI Whisper से cloud transcription।
- Whisper Base से फोन पर offline transcription।
- Timestamp वाला transcript सीधे text box में paste कर सकते हैं।
- SRT, VTT और TXT transcript/subtitle files import कर सकते हैं।
- SRT/VTT cue timestamps को timestamped transcript lines में बदला जाता है।
- TXT में AI द्वारा सटीक clip समय पहचानने के लिए उपयोगी timestamps होने चाहिए।

### AI Highlight Detection
- Groq, OpenAI, Claude या local Qwen3 से transcript का विश्लेषण।
- Cloud analysis flow में अधिकतम 8 highlight suggestions।
- AI प्राथमिकता देता है:
  - पूरे question-and-answer exchanges को, जहाँ उत्तर समझने के लिए प्रश्न भी जरूरी हो।
  - महत्वपूर्ण announcements और नए facts को।
  - Key insights और explanations को।
  - Predictions और practical advice को।
  - ऐसे surprising statements को जिनका context बना रहे।
- हर suggestion में हो सकते हैं:
  - छोटा hook/title।
  - मुख्य moment के start/end timestamps।
  - Category।
  - Model द्वारा दिए जाने पर 1 से 10 तक importance score।
  - उस moment की उपयोगिता बताने वाला छोटा explanation।
- Results आने पर पहला suggestion अपने-आप select होता है।
- Selected suggestion अलग highlight होता है।
- दूसरा card दबाकर दूसरा moment चुना जा सकता है।

### Clip Editing और Preview
- AI clip window के लिए 60 या 90 seconds चुनें।
- चुने गए AI moment के आसपास clip window बनाई जाती है और source-video duration के भीतर रखी जाती है।
- Start और End समय को seconds में manually बदलें।
- Quick duration buttons: 15s, 30s, 45s और 60s।
- Export से पहले चुनी गई time range preview करें।
- Main editor quick-export की अधिकतम सीमा 180 seconds (3 मिनट) है।

### ShortsForge Studio Timeline Editor (1.6.0 में नया)
AI suggestion चुनने के बाद main editor में **Open Studio Editor · Timeline, split, speed, text, audio** दबाएं। Quick export के बाद आने वाले dialog में **Edit in Studio** चुनकर exported MP4 को फिर से खोल सकते हैं।
- एक dedicated dark editing workspace में source video preview।
- एक ही selected source video से चुने गए clip segments की ordered timeline।
- उसी source video से नया start/end range जोड़ें।
- Playhead की वर्तमान position पर selected segment को split करें।
- Segment delete करें, left/right move करके order बदलें और हाल के timeline changes को Undo करें।
- Start/End seconds बदलकर selected segment trim करें।
- Timeline playhead से seek/scrub करके preview करें।
- Playback/export speed: 0.5×, 0.75×, 1×, 1.25×, 1.5× या 2×।
- Vertical 9:16, Square 1:1 या Landscape 16:9 output।
- 140 characters तक text overlay; Top/Centre/Bottom position; Clean/Bold/Large style; White/Yellow/Cyan text colour।
- Original video audio mute करें या looping background music जोड़ें। इस version में music track source audio को replace करता है; अलग tracks का audio mixing और volume automation उपलब्ध नहीं है।
- Ordered timeline को एक MP4 (H.264/AAC) के रूप में export करें। Combined timeline अधिकतम 180 seconds है।
- Export के बाद MP4 share करें या Studio Editor में फिर खोलकर दोबारा edit करें।

यह native timeline editing का पहला चरण है, पूर्ण CapCut/KineMaster feature parity नहीं। अभी timeline के सभी segments एक ही source video के ranges इस्तेमाल करते हैं; अलग-अलग source videos वाला multi-track compositor अभी उपलब्ध नहीं है।


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
10. Suggestion cards देखें। पहला suggestion अपने-आप select हो जाता है।
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
10. Suggestion चुनें, preview करें, जरूरत पर range edit करें और export करें।

Models download होने के बाद local transcription और transcript analysis बिना audio/transcript को cloud AI service भेजे चल सकते हैं। बाद में models दोबारा download करने या cloud AI उपयोग करने के लिए Internet चाहिए।

### Option C — पहले से मौजूद Transcript इस्तेमाल करें
1. Source video चुनें।
2. Timestamped transcript को **TRANSCRIPT** में paste करें या **Import subtitles / transcript (SRT, VTT, TXT)** दबाएं।
3. Settings में Groq, OpenAI या Claude चुनकर संबंधित API key save करें।
4. Editor में वापस आकर **Analyze pasted transcript** दबाएं।
5. Ranked suggestions देखें, एक चुनें, selected range preview करें और export करें।

## 7. Editor का विस्तृत उपयोग

### Step 1: Source video चुनें
**Choose video** दबाकर Android file picker खोलें। वही वीडियो चुनें जिसे edit करने का अधिकार आपके पास है। ऐप filename और Android द्वारा पढ़े जाने पर source duration दिखाता है।

### Step 2: Transcript बनाएं
इन तरीकों में से एक चुनें:
- Cloud: Groq या OpenAI चुने गए वीडियो से audio निकालकर timestamped transcription के लिए provider को भेज सकते हैं।
- Offline: Model install होने के बाद Whisper Base फोन पर ही transcription करता है।
- Existing transcript: Text field में paste करें या SRT/VTT/TXT file import करें।

Cloud transcription में निकला audio चुने गए provider को भेजा जाता है। Offline Whisper audio को डिवाइस पर ही process करता है।

### Step 3: महत्वपूर्ण Moments खोजें
**Auto-transcribe + find clips** या **Analyze pasted transcript** दबाएं। AI timestamps और छोटे explanation के साथ suggestions लौटाता है। विश्वसनीय timestamps के बिना transcript से उपयोगी clip suggestions नहीं मिल सकते।

### Step 4: Suggestion चुनें
पहला suggestion अपने-आप select होता है। उसके card पर highlighted background और **SELECTED** marker दिखेगा। दूसरा card दबाकर दूसरा moment चुन सकते हैं। चुना गया suggestion 60 या 90 seconds की clip window बनाता है, जो suggested moment के आसपास centre होती है और source-video duration से बाहर नहीं जाती।

**महत्वपूर्ण:** AI timestamp एक key moment का anchor है। यदि पूरा answer या discussion चुनी गई 60/90-second window से लंबा है, तो पूरा context clip में शामिल नहीं हो पाएगा। Export से पहले preview करें और Start/End manually बदलें।

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

Optional text overlay जोड़ें (अधिकतम 140 characters) या खाली छोड़ दें। **Export Short** दबाकर export पूरा होने तक ऐप खुला रखें। पूरा होने पर वीडियो save होता है और Android share options दिखते हैं।

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
- Cloud transcription में extracted audio चुने गए provider को भेजा जाता है।
- Cloud highlight analysis में transcript text चुने गए AI provider को भेजा जाता है।
- Local Whisper और Qwen3 inference model files download होने के बाद डिवाइस पर चलते हैं।
- ऐप की cloud API keys Android Keystore और AES-GCM के जरिए encrypt करके app preferences में रखी जाती हैं।
- Offline model files ऐप की private storage में रखी जाती हैं।
- मौजूदा ऐप में account-based cloud project sync या remote media library नहीं है।

Private या confidential recordings cloud API को भेजने से पहले provider की privacy terms पढ़ें। API keys को source code, README, screenshots या public issue reports में न डालें।

## 11. मौजूदा सीमाएँ — क्या शामिल नहीं है

निम्न सुविधाएँ **वर्तमान Version 1.6.0 में उपलब्ध नहीं हैं**:
- YouTube URL import या public YouTube video download।
- Google Drive/Dropbox share-link import।
- Direct internet video URL import।
- Automatic YouTube channel upload, scheduling या publishing।
- YouTube caption-track automatic download।
- AI-generated animated word-by-word subtitles या karaoke captions।
- Automatic B-roll, images, scene generation या presenter generation।
- Batch export queue।
- पूर्ण CapCut/KineMaster स्तर का multi-source, multi-layer editing: picture-in-picture tracks, transitions, keyframes, filters, advanced colour grading, waveform editing, independent audio volume automation और frame-by-frame professional tools।
- Automatic video title, description, hashtags या thumbnail generation।
- Server-side user account, cloud project sync या team collaboration।

Studio Editor में styles वाला static text overlay है; animated subtitle editor अभी नहीं है। मौजूदा timeline एक selected source video के ranges को क्रमबद्ध करती है और एक music track source audio को replace करता है। अलग-अलग multi-track audio mixing नहीं है। AI transcript text का विश्लेषण करता है; अभी visual scene understanding या face tracking नहीं करता।

## 12. Future Roadmap — विचार, अभी लागू नहीं हुए

आगे विचार किए जा सकने वाले सुधार:
- Editable styles और positions के साथ animated word-level captions।
- बेहतर crop controls, subject-aware reframing और safe-area guides।
- Multiple clips selection और batch export।
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
- Cloud API requests और model downloads के लिए OkHttp।
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
    │   │       └── java/com/shortsforge/mobile/MainActivity.kt
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

- Release: 1.6.0।
- Version code: 10।
- Build result: Successful।
- Build run: [GitHub Actions run 44271305685](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/actions/runs/44271305685)।
- APK: [ShortsForge-Mobile.apk](https://github.com/dadimanohar/AI-VIDEO-AUTOMATION/releases/download/mobile-latest/ShortsForge-Mobile.apk)।
- SHA-256: `864282ffd23129cdbf2cc25b18c8250528e10c6a65ec7e20a1964f2912f87ecd`।

Computer पर downloaded file verify करने के लिए उसका SHA-256 hash calculate करके ऊपर दिए गए value से compare करें।

## 18. Copyright, Permissions और Project Status

केवल वही वीडियो चुनें, edit करें और publish करें जिनके आप owner हैं, जिनके लिए आपके पास permission है, या जिनका इस्तेमाल कानूनन कर सकते हैं। कम duration, credit देना या “no copyright intended” disclaimer अपने-आप permission नहीं देता। AI highlight suggestion से footage reuse करने का copyright अधिकार नहीं मिलता।

ShortsForge Mobile YouTube video downloader नहीं है। यह user द्वारा चुनी गई local video file को source मानता है।

APK build GitHub Actions में सफलतापूर्वक verify हुआ है। हर Android डिवाइस पर इसका स्वतंत्र परीक्षण नहीं हुआ है। Playback, local-model performance, export time और compatibility डिवाइस के अनुसार बदल सकते हैं।

इस README में कोई open-source software license घोषित नहीं है। केवल public repository होने का अर्थ यह नहीं है कि आपको source code reuse करने की अनुमति मिल गई है। Modified versions वितरित करने से पहले LICENSE file जोड़ें या license की समीक्षा करें।
