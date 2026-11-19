package com.shortsforge.mobile

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.Presentation
import androidx.media3.effect.TextOverlay
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.whisper.Whisper
import dev.ffmpegkit.whisper.WhisperConfig
import kotlinx.coroutines.runBlocking
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.ByteOrder

@OptIn(UnstableApi::class)
class MainActivity : ComponentActivity() {
    private val ink = Color.rgb(25, 31, 48)
    private val muted = Color.rgb(105, 116, 138)
    private val accent = Color.rgb(83, 72, 225)
    private val page = Color.rgb(245, 247, 252)
    private val border = Color.rgb(229, 233, 242)
    private val prefs by lazy { getSharedPreferences("shortsforge_secure_settings", MODE_PRIVATE) }
    private val executor = Executors.newSingleThreadExecutor()
    private val http = OkHttpClient.Builder().connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(150, TimeUnit.SECONDS).writeTimeout(150, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS).build()
    private val modelHttp = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.MINUTES)
        .callTimeout(35, TimeUnit.MINUTES)
        .build()
    private val sourceDownloadHttp = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .callTimeout(40, TimeUnit.MINUTES)
        .build()
    private val qwenModelName = "Qwen3-0.6B-Q4_0.gguf"
    private val videoExtensions = setOf("mp4", "mov", "m4v", "webm", "mkv", "3gp")
    private val whisperModelName = "ggml-base.bin"
    private val handler = Handler(Looper.getMainLooper())
    private val progressHolder = ProgressHolder()

    private var selectedVideo: android.net.Uri? = null
    private var videoName = ""
    private var youtubeUrlText = ""
    private var directVideoUrlText = ""
    private var youtubeTitle = ""
    private var pendingYoutubeStartSeconds: Double? = null
    private var pendingYoutubeEndSeconds: Double? = null
    private var durationMs = 0L
    private var transcript = ""
    private var highlightItems: List<Highlight> = emptyList()
    private var selectedHighlightIndex = -1
    private val queuedHighlightIndices = linkedSetOf<Int>()
    private val queuedHighlightRanges = linkedMapOf<Int, Pair<Double, Double>>()
    private var previewStopTask: Runnable? = null
    private var clipDurationSeconds = 60
    private var startText = "0"
    private var endText = "30"
    private var overlayText = ""
    private var provider = 0
    private var busy = false
    private var transformer: Transformer? = null
    private var outputFile: File? = null
    private var statusMessage = "Choose a video to start."
    private var player: ExoPlayer? = null

    private lateinit var root: LinearLayout
    private lateinit var transcriptEdit: EditText
    private lateinit var clipDurationSpinner: Spinner
    private lateinit var startEdit: EditText
    private lateinit var endEdit: EditText
    private lateinit var overlayEdit: EditText
    private lateinit var ratioSpinner: Spinner
    private lateinit var playerView: PlayerView
    private lateinit var videoNameView: TextView
    private lateinit var videoDurationView: TextView
    private lateinit var youtubeUrlEdit: EditText
    private lateinit var directVideoUrlEdit: EditText
    private lateinit var statusView: TextView
    private lateinit var footerStatus: TextView
    private lateinit var exportButton: Button
    private lateinit var cancelButton: Button
    private lateinit var highlightsView: LinearLayout
    private lateinit var topicMapSummaryView: TextView
    private lateinit var queueSummaryView: TextView
    private lateinit var studioQueueButton: Button
    private lateinit var batchExportButton: Button
    private var batchExportActive = false
    private var batchExportItems: List<Highlight> = emptyList()
    private var batchExportIndex = 0
    private val batchExportUris = mutableListOf<android.net.Uri>()

    private val pickTranscript = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importTranscriptFile(uri)
    }

    private val youtubePreviewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            youtubeTitle = data.getStringExtra(YouTubePreviewActivity.EXTRA_TITLE).orEmpty()
            pendingYoutubeStartSeconds = data.getDoubleExtra(YouTubePreviewActivity.EXTRA_START_SECONDS, 0.0)
            pendingYoutubeEndSeconds = data.getDoubleExtra(YouTubePreviewActivity.EXTRA_END_SECONDS, 0.0)
            val durationSeconds = data.getDoubleExtra(YouTubePreviewActivity.EXTRA_DURATION_SECONDS, 0.0)
            startText = fmtNumber(pendingYoutubeStartSeconds ?: 0.0)
            endText = fmtNumber(pendingYoutubeEndSeconds ?: 0.0)
            if (::startEdit.isInitialized) startEdit.setText(startText)
            if (::endEdit.isInitialized) endEdit.setText(endText)
            val timeText = if (durationSeconds > 0) " · video " + clock(durationSeconds) else ""
            setStatus("YouTube markers saved for " + (youtubeTitle.ifBlank { "the video" }) + ": " +
                clock(pendingYoutubeStartSeconds ?: 0.0) + " → " + clock(pendingYoutubeEndSeconds ?: 0.0) +
                timeText + ". Import an authorized video file to edit and export this segment.")
        }
    }

    private val pickVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedVideo = uri
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            videoName = displayName(uri)
            durationMs = readDuration(uri)
            applyPendingMarkersOrDefault()
            if (::videoNameView.isInitialized) {
                videoNameView.text = videoName
                videoDurationView.text = if (durationMs > 0) "Duration  " + clock(durationMs / 1000.0) else "Android-supported video"
                playerView.visibility = View.VISIBLE
                player?.setMediaItem(MediaItem.fromUri(uri))
                player?.prepare()
                player?.playWhenReady = false
                startEdit.setText(startText)
                endEdit.setText(endText)
                refreshFooter()
            }
            setStatus("Video ready. Starting full-video analysis when the selected AI provider is configured.")
            autoStartAnalysisForSelectedSource()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider = prefs.getInt("provider", 0).coerceIn(0, 3)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(page) }
        setContentView(root)
        showEditor()
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onPause() {
        player?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        try { transformer?.cancel() } catch (_: Exception) { }
        player?.release()
        executor.shutdownNow()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun firstSharedStream(incoming: Intent): android.net.Uri? {
        return if (Build.VERSION.SDK_INT >= 33) {
            if (incoming.action == Intent.ACTION_SEND_MULTIPLE) {
                incoming.getParcelableArrayListExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)?.firstOrNull()
            } else {
                incoming.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
            }
        } else {
            if (incoming.action == Intent.ACTION_SEND_MULTIPLE) {
                incoming.getParcelableArrayListExtra<android.net.Uri>(Intent.EXTRA_STREAM)?.firstOrNull()
            } else {
                incoming.getParcelableExtra(Intent.EXTRA_STREAM) as? android.net.Uri
            }
        }
    }

    private fun handleIncomingIntent(incoming: Intent?) {
        if (incoming == null) return
        val action = incoming.action.orEmpty()
        val sharedUri = when (action) {
            Intent.ACTION_VIEW -> incoming.data ?: incoming.clipData?.getItemAt(0)?.uri
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE ->
                firstSharedStream(incoming) ?: incoming.clipData?.getItemAt(0)?.uri
            else -> incoming.clipData?.getItemAt(0)?.uri
        }
        if (sharedUri != null && (sharedUri.scheme == "content" || sharedUri.scheme == "file")) {
            confirmAndImportSharedVideo(sharedUri)
            return
        }

        val sharedText = (incoming.getStringExtra(Intent.EXTRA_TEXT) ?: incoming.data?.toString()).orEmpty().trim()
        if (sharedText.isNotBlank()) {
            val candidate = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)
                .find(sharedText)?.value?.trimEnd('.', ',', ')', ']', '>')
            if (!candidate.isNullOrBlank()) {
                val uri = android.net.Uri.parse(candidate)
                if (isYouTubeHost(uri.host.orEmpty().lowercase(Locale.ROOT))) {
                    youtubeUrlText = candidate
                    if (::youtubeUrlEdit.isInitialized) youtubeUrlEdit.setText(candidate)
                    setStatus("YouTube link received. It can be previewed and used for timestamp markers, but it is not an editable video file. Share or select a video file you are authorised to edit.")
                } else if (uri.scheme == "https") {
                    directVideoUrlText = candidate
                    if (::directVideoUrlEdit.isInitialized) directVideoUrlEdit.setText(candidate)
                    setStatus("Video link received. Review it and use the authorised direct-file import control if it points directly to a video file you are allowed to use.")
                } else {
                    setStatus("Shared text received. Choose the downloaded video file to use it as a source.")
                }
            } else {
                setStatus("Shared text received. Choose the downloaded video file to use it as a source.")
            }
        }
    }

    private fun confirmAndImportSharedVideo(uri: android.net.Uri) {
        val check = CheckBox(this).apply {
            text = "I own this video or have permission from the rights holder to edit it."
            setTextColor(ink)
            textSize = 13f
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Import shared video")
            .setMessage("ShortsForge will copy the shared video file into its private storage so it remains available for topic analysis and editing. This imports a file shared from YTDLnis or another Android app; it does not download YouTube streams.")
            .setView(check)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Import video") { _, _ ->
                if (!check.isChecked) {
                    setStatus("Confirm that you have permission to edit this video before importing it.")
                } else {
                    importSharedVideo(uri)
                }
            }
            .show()
    }

    private fun importSharedVideo(uri: android.net.Uri) {
        if (busy) {
            setStatus("Another operation is running. Wait for it to finish before importing a shared video.")
            return
        }
        val sourceName = try { displayName(uri) } catch (_: Exception) { "shared-video.mp4" }
        val mime = try { contentResolver.getType(uri).orEmpty() } catch (_: Exception) { "" }
        val sourceExtension = sourceName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (!mime.startsWith("video/") && sourceExtension !in videoExtensions) {
            setStatus("The shared item is not recognised as a video. Share a video file, not a YouTube page or text link.")
            return
        }

        busy = true
        refreshFooter()
        setStatus("Importing " + sourceName + " from the shared app…")
        executor.execute {
            var destination: File? = null
            try {
                val importDir = File(filesDir, "shared-video-imports")
                if (!importDir.exists() && !importDir.mkdirs()) throw IOException("Could not create the import folder.")
                val outputExtension = sourceExtension.takeIf { it in videoExtensions } ?: when (mime.lowercase(Locale.ROOT)) {
                    "video/webm" -> "webm"
                    "video/quicktime" -> "mov"
                    "video/x-matroska" -> "mkv"
                    "video/3gpp" -> "3gp"
                    else -> "mp4"
                }
                val safeBase = sourceName.substringBeforeLast('.', sourceName)
                    .replace(Regex("[^A-Za-z0-9._-]"), "_").take(70).ifBlank { "shared-video" }
                val outFile = File(importDir, System.currentTimeMillis().toString() + "_" + safeBase + "." + outputExtension)
                destination = outFile
                val input = contentResolver.openInputStream(uri) ?: throw IOException("Android could not open the shared video.")
                var totalBytes = 0L
                input.use { source ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(128 * 1024)
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            totalBytes += count.toLong()
                            if (totalBytes > 2L * 1024L * 1024L * 1024L) throw IOException("Shared video exceeds the 2 GB import safety limit.")
                            output.write(buffer, 0, count)
                        }
                        output.flush()
                    }
                }
                if (outFile.length() <= 0L) throw IOException("The shared video file is empty.")
                val localUri = android.net.Uri.fromFile(outFile)
                val importedDuration = readDuration(localUri)
                runOnUiThread {
                    selectedVideo = localUri
                    videoName = outFile.name
                    durationMs = importedDuration
                    applyPendingMarkersOrDefault()
                    if (::videoNameView.isInitialized) {
                        videoNameView.text = videoName
                        videoDurationView.text = if (durationMs > 0) "Duration  " + clock(durationMs / 1000.0) else "Imported video · Android-supported format"
                        playerView.visibility = View.VISIBLE
                        player?.setMediaItem(MediaItem.fromUri(localUri))
                        player?.prepare()
                        player?.playWhenReady = false
                        startEdit.setText(startText)
                        endEdit.setText(endText)
                    }
                    busy = false
                    refreshFooter()
                    setStatus("Downloaded video imported successfully. Starting full-video topic discovery…")
                    autoStartAnalysisForSelectedSource()
                }
            } catch (e: Exception) {
                destination?.delete()
                runOnUiThread {
                    busy = false
                    refreshFooter()
                    setStatus("Could not import the shared video: " + (e.message ?: "unknown error") + ". You can also select it from Downloads.")
                }
            }
        }
    }

    private fun autoStartAnalysisForSelectedSource() {
        when (provider) {
            0, 1 -> {
                if (readKey(provider).isNotBlank()) {
                    setStatus("Source ready. Automatically transcribing the full video and mapping every topic…")
                    transcribeAndAnalyze()
                } else {
                    setStatus("Source ready. Add your Groq or OpenAI API key in Settings, then tap Analyze entire video.")
                }
            }
            3 -> {
                if (offlineModelsAvailable()) {
                    setStatus("Source ready. Starting on-device Whisper transcription and Qwen3 topic mapping…")
                    transcribeAndAnalyze()
                } else {
                    setStatus("Source ready. Download both Whisper and Qwen3 models in Settings, then run full-video analysis.")
                }
            }
            else -> setStatus("Source ready. Claude can map topics from a timestamped transcript. Import or paste subtitles, then tap Analyze all topics.")
        }
    }

    private fun showEditor() {
        root.removeAllViews()
        root.addView(toolbar("ShortsForge", "MOBILE SHORTS STUDIO", "Settings") { showSettings() })
        val scroll = ScrollView(this).apply { clipToPadding = false }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(8), dp(18), dp(24)) }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        body.addView(text("Turn footage into a Short.", 25f, ink, true).apply { setPadding(0, dp(12), 0, dp(4)) })
        body.addView(text("Choose a source, find a moment, then edit and export.", 14f, muted))

        val source = card(body, "01  •  SOURCE VIDEO", "Choose a local file, preview a YouTube link, or import a creator-authorized direct video file URL.")
        source.addView(button("Choose video", true) { pickVideo.launch(arrayOf("video/*")) }, full())
        source.addView(button("Choose downloaded video · YTDLnis / Downloads", false) {
            pickVideo.launch(arrayOf("video/*"))
        }, full(dp(7)))
        source.addView(text("You can also open YTDLnis → Share → ShortsForge to send a video file directly into this app. ShortsForge imports the shared file; it does not include YTDLnis or download YouTube streams.", 11f, muted)
            .apply { setPadding(0, dp(6), 0, dp(7)) })

        source.addView(label("YOUTUBE VIDEO URL"))
        youtubeUrlEdit = input("Paste a YouTube watch, Shorts, or youtu.be link", youtubeUrlText, false)
        youtubeUrlEdit.addTextChangedListener(watcher { youtubeUrlText = it })
        source.addView(youtubeUrlEdit, full())
        source.addView(button("Preview YouTube URL + mark clip timestamps", false) {
            openYouTubePreview()
        }, full(dp(7)))
        source.addView(text("Embedded preview and timestamp markers only. YouTube watch URLs do not provide an editable video file.", 11f, muted)
            .apply { setPadding(0, dp(5), 0, dp(5)) })
        source.addView(button("Send URL to YTDLnis for video download", true) {
            sendYouTubeLinkToYtdlnis()
        }, full(dp(7)))
        source.addView(text("YTDLnis must be installed separately. You will review and start the download there. When it finishes, use Share → ShortsForge or choose the saved file from Downloads to use it as the source video.", 11f, muted)
            .apply { setPadding(0, dp(5), 0, dp(7)) })

        source.addView(label("AUTHORIZED DIRECT VIDEO FILE URL"))
        directVideoUrlEdit = input("Paste creator-authorized HTTPS MP4 / MOV / WebM / MKV link", directVideoUrlText, false)
        directVideoUrlEdit.addTextChangedListener(watcher { directVideoUrlText = it })
        source.addView(directVideoUrlEdit, full())
        source.addView(button("Import authorized video URL", false) {
            importAuthorizedVideoUrl()
        }, full(dp(7)))
        videoNameView = text(if (videoName.isBlank()) "No video selected" else videoName, 14f, ink, true).apply { setPadding(0, dp(12), 0, dp(3)) }
        source.addView(videoNameView)
        videoDurationView = text(if (durationMs > 0) "Duration  " + clock(durationMs / 1000.0) else "MP4, MOV and Android-supported formats", 12f, muted)
        source.addView(videoDurationView)
        player = ExoPlayer.Builder(this).build()
        playerView = PlayerView(this).apply {
            player = this@MainActivity.player
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            visibility = if (selectedVideo == null) View.GONE else View.VISIBLE
            setBackgroundColor(Color.BLACK)
        }
        source.addView(playerView, LinearLayout.LayoutParams(-1, dp(210)).apply { topMargin = dp(12) })
        if (selectedVideo != null) {
            player?.setMediaItem(MediaItem.fromUri(selectedVideo!!))
            player?.prepare()
        }

        val ai = card(body, "02  •  DISCOVER EVERY VIDEO TOPIC", "Map the whole transcript into chronological, timestamped topics — not just the top few highlights.")
        ai.addView(button("Analyze entire video · transcribe + map every topic", true) { transcribeAndAnalyze() }, full())
        ai.addView(button("Analyze all topics in transcript", false) { analyzeTranscript() }, full(dp(8)))
        ai.addView(button("Import subtitles / transcript (SRT, VTT, TXT)", false) {
            pickTranscript.launch(arrayOf("text/*", "application/x-subrip", "application/octet-stream"))
        }, full(dp(8)))
        ai.addView(label("TRANSCRIPT"))
        transcriptEdit = input("Paste a timestamped transcript here", transcript, true).apply { minLines = 4 }
        transcriptEdit.addTextChangedListener(watcher { transcript = it })
        ai.addView(transcriptEdit, full())
        ai.addView(text("Auto-transcription needs a Groq or OpenAI key. You can also import subtitle or caption files as SRT/VTT. Claude can analyse a timestamped transcript.", 12f, muted).apply { setPadding(0, dp(8), 0, 0) })
        statusView = text(statusMessage, 13f, accent, true).apply { setPadding(0, dp(12), 0, dp(2)) }
        ai.addView(statusView)
        topicMapSummaryView = text("VIDEO TOPIC MAP  ·  Waiting for transcript analysis", 12f, muted, true).apply { setPadding(0, dp(11), 0, dp(5)) }
        ai.addView(topicMapSummaryView)
        highlightsView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        ai.addView(highlightsView)
        queueSummaryView = text("No topics added to your Shorts queue yet.", 12f, muted).apply {
            setPadding(0, dp(8), 0, dp(2))
        }
        ai.addView(queueSummaryView)
        ai.addView(button("Add all detected topics to Shorts queue", false) {
            var added = 0
            highlightItems.indices.forEach { index -> if (setHighlightQueued(index, true)) added++ }
            val longTopics = highlightItems.count { item ->
                item.start != null && item.end != null && item.end - item.start > 180.0
            }
            showHighlights(highlightItems)
            setStatus(added.toString() + " topic(s) queued. " + longTopics +
                " long topic(s) stay in the map; select one, trim Start/End to 180 seconds or less, then add it separately.")
        }, full(dp(6)))

        val edit = card(body, "03  •  EDIT YOUR SHORT", "Tap a topic to load its exact timestamps. Add multiple topics to a single Studio timeline or export each topic separately.")
        studioQueueButton = button("Open Studio Editor · current highlight", false) { openStudioEditor() }
        edit.addView(studioQueueButton, full(dp(8)))
        batchExportButton = button("Export each queued topic as a separate Short", true) {
            startQueuedHighlightsBatchExport()
        }
        edit.addView(batchExportButton, full(dp(7)))
        edit.addView(button("Clear Shorts topic queue", false) {
            queuedHighlightIndices.clear()
            queuedHighlightRanges.clear()
            refreshQueueControls()
            showHighlights(highlightItems)
            setStatus("Shorts topic queue cleared. The current highlight remains selected.")
        }, full(dp(6)))
        edit.addView(button("Preview selected clip", false) { previewSelectedClip() }, full())
        edit.addView(label("AI HIGHLIGHT CLIP LENGTH"))
        clipDurationSpinner = spinner(listOf("60 seconds", "90 seconds"))
        clipDurationSpinner.setSelection(if (clipDurationSeconds == 90) 1 else 0)
        clipDurationSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) { }
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                clipDurationSeconds = if (position == 1) 90 else 60
            }
        }
        edit.addView(clipDurationSpinner, full())
        edit.addView(button("Expand selected topic to 60s / 90s", false) {
            val item = highlightItems.getOrNull(selectedHighlightIndex)
            if (item?.start != null && item.end != null) {
                applyHighlightWindow(item.start, item.end)
                setStatus("Selected topic expanded to a " + clipDurationSeconds + "-second window. This does not change the queued exact ranges.")
            } else {
                setStatus("Select a timestamped topic first.")
            }
        }, full(dp(6)))
        edit.addView(text("Tap a suggestion to use its exact timestamps. The 60s/90s control is an optional expansion around the selected topic.", 12f, muted).apply { setPadding(0, dp(7), 0, dp(2)) })
        edit.addView(label("START AND END · SECONDS"))
        val range = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        startEdit = input("Start", startText, false).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        endEdit = input("End", endText, false).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        startEdit.addTextChangedListener(watcher { startText = it })
        endEdit.addTextChangedListener(watcher { endText = it })
        range.addView(fieldColumn("START", startEdit), LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(6) })
        range.addView(fieldColumn("END", endEdit), LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(6) })
        edit.addView(range)
        val presets = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(15, 30, 45, 60).forEach { seconds ->
            presets.addView(button(seconds.toString() + "s", false) {
                val start = startEdit.text.toString().toDoubleOrNull() ?: 0.0
                val end = if (durationMs > 0) minOf(start + seconds, durationMs / 1000.0) else start + seconds
                endEdit.setText(fmtNumber(end))
            }, LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                if (seconds != 15) leftMargin = dp(4)
                if (seconds != 60) rightMargin = dp(4)
            })
        }
        edit.addView(presets, full(dp(8)))
        edit.addView(label("OUTPUT FORMAT"))
        ratioSpinner = spinner(listOf("Vertical 9:16 · YouTube Shorts", "Square 1:1", "Landscape 16:9"))
        edit.addView(ratioSpinner, full())
        edit.addView(label("OPTIONAL TEXT OVERLAY"))
        overlayEdit = input("Text burned into the video", overlayText, true).apply { minLines = 2 }
        overlayEdit.addTextChangedListener(watcher { overlayText = it })
        edit.addView(overlayEdit, full())
        edit.addView(text("This text is burned into the entire exported clip. Leave blank for no overlay.", 12f, muted).apply { setPadding(0, dp(6), 0, 0) })

        // Re-render previous AI suggestions when returning from Settings.
        if (highlightItems.isNotEmpty()) showHighlights(highlightItems)

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(10), dp(18), dp(12))
            setBackgroundColor(Color.WHITE)
            elevation = dp(10).toFloat()
        }
        footerStatus = text(statusMessage, 12f, muted).apply { maxLines = 2 }
        footer.addView(footerStatus)
        exportButton = button("Export Short", true) { exportClip() }
        footer.addView(exportButton, full(dp(8)))
        cancelButton = button("Cancel export", false) { cancelExport() }.apply { visibility = View.GONE }
        footer.addView(cancelButton, full(dp(6)))
        root.addView(footer)
        refreshFooter()
    }

    private fun showSettings() {
        player?.pause()
        root.removeAllViews()
        root.addView(toolbar("Settings", "AI PROVIDER & API ACCESS", "Back") { showEditor() })
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(28)) }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val settings = card(body, "AI CONNECTION", "Choose cloud AI or fully on-device AI.")
        settings.addView(label("AI MODE"))
        val providerSpinner = spinner(listOf("Groq · Cloud AI", "OpenAI · Cloud AI", "Claude · Cloud AI", "Offline local AI · Qwen3 + Whisper")).apply { setSelection(provider) }
        settings.addView(providerSpinner, full())
        settings.addView(label("API KEY"))
        val apiKey = input("Paste your provider API key", if (provider == 3) "" else readKey(provider), false).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            visibility = if (provider == 3) View.GONE else View.VISIBLE
        }
        settings.addView(apiKey, full())
        val apiKeyHelp = text("Cloud keys are encrypted with Android Keystore and stored on this device.", 12f, muted).apply {
            setPadding(0, dp(10), 0, 0)
            visibility = if (provider == 3) View.GONE else View.VISIBLE
        }
        settings.addView(apiKeyHelp)
        providerSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) { }
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                provider = position
                apiKey.visibility = if (position == 3) View.GONE else View.VISIBLE
                apiKeyHelp.visibility = if (position == 3) View.GONE else View.VISIBLE
                apiKey.setText(if (position == 3) "" else readKey(position))
            }
        }
        settings.addView(button("Save AI settings", true) {
            try {
                if (provider != 3) saveKey(provider, apiKey.text.toString().trim())
                prefs.edit().putInt("provider", provider).apply()
                setStatus(if (provider == 3) "Offline local AI mode selected." else "AI settings saved securely.")
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Could not save settings: " + (e.message ?: "error"), Toast.LENGTH_LONG).show()
            }
        }, full(dp(14)))
        val modelInfo = when (provider) {
            0 -> "Groq cloud model for highlight analysis and Groq Whisper for transcription. Internet and your API key are required."
            1 -> "OpenAI cloud model for highlight analysis and Whisper API for transcription. Internet and your API key are required."
            2 -> "Claude cloud model for highlight analysis. Paste a timestamped transcript or use another transcription provider."
            else -> "Qwen3 0.6B (Q4) selects key moments locally. Whisper Base creates timestamped transcripts locally. After downloading both models once, these features work offline."
        }
        card(body, "MODEL CONFIGURATION", modelInfo).addView(text(
            if (provider == 3) "Offline model downloads are about 577 MB in total: Qwen3 (~429 MB) + Whisper Base (~148 MB). Models stay in app storage and are not included in the APK."
            else "Cloud AI requires internet access and may incur charges from your provider. Video preview, trimming, text overlay, and export run on your phone.",
            13f, muted
        ))
        val modelCard = card(body, "OFFLINE MODEL MANAGER", "Download once while connected to Wi-Fi. Then choose Offline local AI to transcribe and find highlights without cloud APIs.")
        val modelStatus = text(localModelStatus(), 12f, muted).apply { setPadding(0, 0, 0, dp(10)) }
        modelCard.addView(modelStatus)
        modelCard.addView(button("Download offline AI models · ~577 MB", true) {
            downloadOfflineModels(modelStatus)
        }, full())
        body.addView(button("Return to editor", false) { showEditor() }, full(dp(8)))
    }

    private fun toolbar(title: String, subtitle: String, action: String, click: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            setBackgroundColor(Color.WHITE)
        }
        row.addView(TextView(this).apply {
            text = "SF"; textSize = 17f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE); gravity = Gravity.CENTER; background = shape(accent, 14)
        }, LinearLayout.LayoutParams(dp(46), dp(46)))
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titles.addView(text(title, 18f, ink, true))
        titles.addView(text(subtitle, 9f, muted, true))
        row.addView(titles, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(12) })
        row.addView(button(action, false, click).apply { minWidth = dp(72); textSize = 12f })
        return row
    }

    private fun card(parent: LinearLayout, title: String, subtitle: String): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(17), dp(17), dp(17))
            background = shape(Color.WHITE, 20, border)
            elevation = dp(2).toFloat()
        }
        parent.addView(box, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        box.addView(text(title, 11f, accent, true))
        box.addView(text(subtitle, 12f, muted).apply { setPadding(0, dp(5), 0, dp(14)) })
        return box
    }

    private fun label(value: String) = text(value, 10f, muted, true).apply { setPadding(0, dp(12), 0, dp(7)) }
    private fun text(value: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private fun input(hintText: String, value: String = "", multiline: Boolean = false) = EditText(this).apply {
        hint = hintText
        setText(value)
        textSize = 14f
        setTextColor(ink)
        setHintTextColor(Color.rgb(150, 158, 176))
        setPadding(dp(13), dp(11), dp(13), dp(11))
        background = shape(Color.rgb(248, 249, 252), 12, border)
        if (multiline) {
            setSingleLine(false)
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        } else setSingleLine(true)
    }

    private fun fieldColumn(title: String, field: EditText) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(text(title, 10f, muted, true))
        addView(field, full(dp(5)))
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
        background = shape(Color.rgb(248, 249, 252), 12, border)
        setPadding(dp(9), 0, dp(9), 0)
    }

    private fun button(title: String, primary: Boolean, click: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 14f
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        setTextColor(if (primary) Color.WHITE else Color.rgb(65, 55, 194))
        background = shape(if (primary) accent else Color.rgb(242, 242, 255), 13, if (primary) accent else Color.rgb(224, 222, 255))
        minHeight = dp(48)
        setPadding(dp(12), dp(7), dp(12), dp(7))
        setOnClickListener { click() }
    }

    private fun shape(fill: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun full(top: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { if (top > 0) topMargin = top }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun watcher(onChange: (String) -> Unit) = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onChange(s?.toString() ?: "") }
        override fun afterTextChanged(s: android.text.Editable?) { }
    }

    private fun refreshFooter() {
        if (::footerStatus.isInitialized) footerStatus.text = statusMessage
        if (::statusView.isInitialized) statusView.text = statusMessage
        if (::exportButton.isInitialized) {
            exportButton.isEnabled = selectedVideo != null && !busy
            exportButton.alpha = if (exportButton.isEnabled) 1f else 0.45f
        }
        if (::cancelButton.isInitialized) cancelButton.visibility = if (busy && transformer != null) View.VISIBLE else View.GONE
        if (::batchExportButton.isInitialized) {
            batchExportButton.isEnabled = queuedHighlightIndices.isNotEmpty() && !busy
            batchExportButton.alpha = if (batchExportButton.isEnabled) 1f else 0.45f
        }
    }

    private fun setStatus(message: String) {
        statusMessage = message
        runOnUiThread { refreshFooter() }
    }

    private fun displayName(uri: android.net.Uri): String {
        var name = "Selected video"
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) name = it.getString(0) ?: name
        }
        return name
    }

    private fun readDuration(uri: android.net.Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            if (uri.scheme == "file") retriever.setDataSource(uri.path ?: throw IOException("Missing video path"))
            else retriever.setDataSource(this, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) { 0L } finally { try { retriever.release() } catch (_: Exception) { } }
    }

    private fun previewSelectedClip() {
        val uri = selectedVideo
        if (uri == null) { setStatus("Choose a video first."); return }
        val from = startEdit.text.toString().toDoubleOrNull()
        val to = endEdit.text.toString().toDoubleOrNull()
        if (from == null || to == null || from < 0 || to <= from || (durationMs > 0 && to * 1000 > durationMs + 250)) {
            setStatus("Set a valid clip range before previewing."); return
        }
        val exo = player ?: run { setStatus("Video preview is not ready."); return }
        try {
            previewStopTask?.let { handler.removeCallbacks(it) }
            exo.seekTo((from * 1000).toLong())
            exo.playWhenReady = true
            exo.play()
            val stop = Runnable {
                exo.pause()
                exo.seekTo((to * 1000).toLong())
                setStatus("Preview finished. Export when ready.")
            }
            previewStopTask = stop
            handler.postDelayed(stop, ((to - from) * 1000).toLong())
            setStatus("Previewing selected clip: " + clock(from) + " to " + clock(to))
        } catch (e: Exception) { setStatus("Preview failed: " + (e.message ?: "unknown error")) }
    }

    private fun playTopicRange(item: Highlight) {
        val uri = selectedVideo
        if (uri == null) {
            setStatus("Choose the source video before playing a topic.")
            return
        }
        val from = item.start
        val to = item.end
        if (from == null || to == null || to <= from) {
            setStatus("This topic does not have valid timestamps.")
            return
        }
        val exo = player ?: run {
            setStatus("Video preview is not ready.")
            return
        }
        try {
            previewStopTask?.let { handler.removeCallbacks(it) }
            selectedHighlightIndex = highlightItems.indexOf(item).coerceAtLeast(0)
            applyExactHighlightRange(from, to)
            showHighlights(highlightItems)
            exo.seekTo((from * 1000.0).toLong())
            exo.playWhenReady = true
            exo.play()
            val stop = Runnable {
                exo.pause()
                exo.seekTo((to * 1000.0).toLong())
                setStatus("Topic preview finished: " + item.hook)
            }
            previewStopTask = stop
            handler.postDelayed(stop, ((to - from) * 1000.0).toLong())
            setStatus("Playing topic: " + item.hook + " · " + clock(from) + " → " + clock(to))
        } catch (e: Exception) {
            setStatus("Could not play topic: " + (e.message ?: "unknown player error"))
        }
    }

    private fun applyExactHighlightRange(start: Double, end: Double) {
        if (!start.isFinite() || !end.isFinite() || start < 0 || end <= start) return
        startEdit.setText(fmtNumber(start))
        endEdit.setText(fmtNumber(end))
    }

    private fun applyHighlightWindow(anchorStart: Double, anchorEnd: Double) {
        val target = clipDurationSeconds.toDouble()
        val durationSeconds = if (durationMs > 0) durationMs / 1000.0 else 0.0
        val actualLength = if (durationSeconds > 0) minOf(target, durationSeconds) else target
        val centre = (anchorStart + anchorEnd) / 2.0
        var from = (centre - actualLength / 2.0).coerceAtLeast(0.0)
        if (durationSeconds > 0) from = minOf(from, (durationSeconds - actualLength).coerceAtLeast(0.0))
        val to = from + actualLength
        startEdit.setText(fmtNumber(from))
        endEdit.setText(fmtNumber(to))
    }

    private fun importTranscriptFile(uri: android.net.Uri) {
        try {
            val raw = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: throw IOException("Could not read the selected transcript file.")
            if (raw.isBlank()) throw IOException("The transcript file is empty.")
            val parsed = parseSubtitleText(raw)
            val timedLines = timestampedTranscriptLineCount(parsed)
            if (timedLines == 0) throw IOException("No timestamped transcript lines were found. Import SRT/VTT or TXT lines such as [00:00.00 - 00:04.20] spoken words.")
            transcript = parsed
            transcriptEdit.setText(parsed)
            setStatus("Transcript imported (" + timedLines + " timestamped lines). Automatically mapping all topics…")
            analyzeTranscript(true)
        } catch (e: Exception) {
            setStatus("Transcript import failed: " + (e.message ?: "unknown file error"))
        }
    }

        private fun parseSubtitleText(raw: String): String {
        val normalized = raw.replace("\r", "")
        val blocks = normalized.split(Regex("\n\\s*\n"))
        val output = StringBuilder()
        val fullTime = Regex("(\\d{1,2}):(\\d{2}):(\\d{2})[,.](\\d{1,3})")
        val shortTime = Regex("(\\d{1,3}):(\\d{2})[,.](\\d{1,3})")
        fun toSeconds(value: String): Double {
            val m = fullTime.find(value)
            if (m != null) {
                val fraction = m.groupValues[4].padEnd(3, '0').take(3).toInt()
                return m.groupValues[1].toInt() * 3600.0 + m.groupValues[2].toInt() * 60.0 + m.groupValues[3].toInt() + fraction / 1000.0
            }
            val s = shortTime.find(value) ?: return Double.NaN
            val fraction = s.groupValues[3].padEnd(3, '0').take(3).toInt()
            return s.groupValues[1].toInt() * 60.0 + s.groupValues[2].toInt() + fraction / 1000.0
        }
        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
            val timeLine = lines.firstOrNull { it.contains("-->") }
            if (timeLine == null) continue
            val sides = timeLine.split("-->")
            if (sides.size != 2) continue
            val start = toSeconds(sides[0])
            val end = toSeconds(sides[1])
            if (!start.isFinite() || !end.isFinite() || end <= start) continue
            val words = lines.dropWhile { !it.contains("-->") }.drop(1)
                .joinToString(" ").replace(Regex("<[^>]+>"), "")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").trim()
            if (words.isNotBlank()) {
                output.append("[").append(clock(start)).append(" - ").append(clock(end)).append("] ")
                    .append(words).append('\n')
            }
        }
        return if (output.isNotEmpty()) output.toString().trim() else normalized.trim()
    }

    private fun offlineModelsAvailable(): Boolean {
        return try {
            qwenModelFile().let { it.exists() && it.length() >= 420_000_000L } &&
                whisperModelFile().let { it.exists() && it.length() >= 100_000_000L }
        } catch (_: Exception) {
            false
        }
    }

    private fun isConnectivityFailure(error: Throwable): Boolean {
        var cause: Throwable? = error
        while (cause != null) {
            if (cause is java.net.UnknownHostException ||
                cause is java.net.ConnectException ||
                cause is java.net.NoRouteToHostException ||
                cause is java.net.SocketTimeoutException ||
                cause is java.net.UnknownServiceException) return true
            val message = cause.message?.lowercase(Locale.US) ?: ""
            if (message.contains("unable to resolve host") ||
                message.contains("failed to connect") ||
                message.contains("network is unreachable") ||
                message.contains("connection timed out") ||
                message.contains("no internet")) return true
            cause = cause.cause
        }
        return false
    }

    private fun transcribeAndAnalyze() {
        val uri = selectedVideo
        if (uri == null) { setStatus("Choose a video first."); return }
        if (provider == 3) {
            transcribeLocally(uri)
            return
        }
        if (provider == 2) { setStatus("Select Groq or OpenAI for automatic transcription, or paste a timestamped transcript."); return }
        val key = readKey(provider)
        if (key.isBlank()) { setStatus("Add an API key in Settings."); showSettings(); return }
        if (busy) return
        busy = true
        refreshFooter()
        setStatus("Decoding source audio to a speech-ready 16 kHz mono WAV…")
        executor.execute {
            var audio: File? = null
            try {
                // Decode to PCM/WAV instead of copying the source codec into an MP4 container.
                // MediaMuxer rejects some audio MIME formats (for example, Opus in MP4).
                audio = extractWav16kMono(uri)
                if (audio.length() > 24L * 1024L * 1024L) {
                    throw IOException("Decoded WAV exceeds the 24 MB transcription upload limit. Use a shorter source video or import a timestamped transcript.")
                }
                runOnUiThread { setStatus("Uploading WAV audio and transcribing speech with timestamps…") }
                val result = transcribe(audio, key, provider)
                if (result.isBlank()) throw IOException("No speech was detected. You can paste a transcript manually.")
                runOnUiThread {
                    transcript = result
                    transcriptEdit.setText(result)
                    setStatus("Transcript ready. Mapping every topic across the full video…")
                    analyzeTranscript(true)
                }
            } catch (e: Exception) {
                val useLocal = provider in 0..1 && isConnectivityFailure(e) && offlineModelsAvailable()
                runOnUiThread {
                    busy = false
                    refreshFooter()
                    if (useLocal) {
                        provider = 3
                        prefs.edit().putInt("provider", 3).apply()
                        setStatus("Cloud connection unavailable. Switching to on-device Whisper + Qwen3…")
                        transcribeLocally(uri)
                    } else {
                        setStatus("Transcription failed: " + (e.message ?: "unknown error"))
                    }
                }
            } finally {
                try { audio?.delete() } catch (_: Exception) { }
            }
        }
    }

    private fun modelsDirectory(): File {
        val directory = File(filesDir, "offline-models")
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Could not create the offline model folder.")
        return directory
    }

    private fun qwenModelFile(): File = File(modelsDirectory(), qwenModelName)
    private fun whisperModelFile(): File = File(modelsDirectory(), whisperModelName)

    private fun localModelStatus(): String {
        return try {
            val qwen = qwenModelFile()
            val whisper = whisperModelFile()
            val qwenReady = qwen.exists() && qwen.length() >= 420_000_000L
            val whisperReady = whisper.exists() && whisper.length() >= 100_000_000L
            "Qwen3 0.6B: " + (if (qwenReady) "Downloaded" else "Not downloaded") +
                "\nWhisper Base: " + (if (whisperReady) "Downloaded" else "Not downloaded")
        } catch (e: Exception) {
            "Model storage unavailable: " + (e.message ?: "unknown error")
        }
    }

    private fun downloadOfflineModels(statusView: TextView) {
        if (busy) return
        busy = true
        refreshFooter()
        statusView.text = "Preparing model storage…"
        executor.execute {
            try {
                val dir = modelsDirectory()
                val available = android.os.StatFs(dir.absolutePath).availableBytes
                if (available < 700L * 1024L * 1024L) {
                    throw IOException("At least 700 MB free storage is recommended before downloading both offline models.")
                }
                downloadModelFile(
                    "https://huggingface.co/ggml-org/Qwen3-0.6B-GGUF/resolve/main/Qwen3-0.6B-Q4_0.gguf?download=true",
                    qwenModelFile(),
                    420_000_000L,
                    "Qwen3 0.6B"
                ) { message -> runOnUiThread { statusView.text = message; setStatus(message) } }
                downloadModelFile(
                    "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin?download=true",
                    whisperModelFile(),
                    100_000_000L,
                    "Whisper Base"
                ) { message -> runOnUiThread { statusView.text = message; setStatus(message) } }
                runOnUiThread {
                    statusView.text = localModelStatus() + "\nOffline AI is ready. Select Offline local AI in the provider list."
                    setStatus("Both offline models are ready. Local transcription and AI highlight analysis can now run without internet.")
                    Toast.makeText(this, "Offline AI models downloaded", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusView.text = localModelStatus() + "\nDownload error: " + (e.message ?: "unknown error")
                    setStatus("Offline model download failed: " + (e.message ?: "unknown error"))
                }
            } finally {
                runOnUiThread {
                    busy = false
                    refreshFooter()
                }
            }
        }
    }

    private fun downloadModelFile(
        url: String,
        destination: File,
        minimumBytes: Long,
        displayName: String,
        onProgress: (String) -> Unit
    ) {
        if (destination.exists() && destination.length() >= minimumBytes) {
            onProgress(displayName + " already downloaded.")
            return
        }
        if (destination.exists()) destination.delete()
        val partial = File(destination.parentFile, destination.name + ".part")
        if (partial.exists()) partial.delete()
        onProgress("Downloading " + displayName + "…")
        val request = Request.Builder().url(url).header("User-Agent", "ShortsForge-Mobile/1.3").get().build()
        modelHttp.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException(displayName + " download returned HTTP " + response.code)
            val body = response.body ?: throw IOException(displayName + " download returned an empty response.")
            val length = body.contentLength()
            if (length in 1 until minimumBytes) throw IOException(displayName + " file is smaller than expected; the model download may be incomplete.")
            body.byteStream().use { input ->
                partial.outputStream().buffered(1024 * 1024).use { output ->
                    val buffer = ByteArray(256 * 1024)
                    var total = 0L
                    var lastUpdate = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > 2_000_000_000L) throw IOException(displayName + " file exceeded the safety size limit.")
                        output.write(buffer, 0, count)
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 800) {
                            lastUpdate = now
                            val percent = if (length > 0) (total * 100L / length).toInt().coerceIn(0, 99) else -1
                            val received = total / (1024 * 1024)
                            val totalMb = if (length > 0) " / " + (length / (1024 * 1024)) + " MB" else ""
                            onProgress(if (percent >= 0) "Downloading " + displayName + "… " + percent + "%" else "Downloading " + displayName + "… " + received + totalMb)
                        }
                    }
                }
            }
        }
        if (!partial.exists() || partial.length() < minimumBytes) {
            partial.delete()
            throw IOException(displayName + " download was incomplete. Please retry on a stable connection.")
        }
        if (!partial.renameTo(destination)) {
            destination.delete()
            if (!partial.renameTo(destination)) throw IOException("Could not save " + displayName + " to app storage.")
        }
        onProgress(displayName + " download complete.")
    }

    private fun transcribeLocally(uri: android.net.Uri) {
        val whisperFile = try { whisperModelFile() } catch (e: Exception) {
            setStatus("Offline models are not ready. Download them in Settings first.")
            showSettings()
            return
        }
        if (!whisperFile.exists() || whisperFile.length() < 100_000_000L) {
            setStatus("Whisper Base is not downloaded. Open Settings and download the offline AI models first.")
            showSettings()
            return
        }
        if (busy) return
        busy = true
        refreshFooter()
        setStatus("Offline mode: decoding video audio to 16 kHz WAV on this device…")
        executor.execute {
            var wav: File? = null
            try {
                wav = extractWav16kMono(uri)
                runOnUiThread { setStatus("Offline Whisper is transcribing the video. No audio is being sent to a server…") }
                val transcriptText = runBlocking {
                    val model = Whisper.loadModel(this@MainActivity, whisperFile.absolutePath)
                    try {
                        val result = Whisper.transcribe(
                            model,
                            wav!!.absolutePath,
                            WhisperConfig(language = "auto", threads = 4, printTimestamps = true)
                        )
                        result.segments.joinToString("\n") { segment ->
                            "[" + clock(segment.startMs / 1000.0) + " - " + clock(segment.endMs / 1000.0) + "] " + segment.text.trim()
                        }.ifBlank { result.text.trim() }
                    } finally {
                        Whisper.releaseModel(model)
                    }
                }
                if (transcriptText.isBlank()) throw IOException("Whisper did not detect speech in this video.")
                runOnUiThread {
                    transcript = transcriptText
                    transcriptEdit.setText(transcriptText)
                    setStatus("Offline transcription complete. Qwen3 is now finding important moments…")
                    analyzeTranscript(true)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    busy = false
                    refreshFooter()
                    setStatus("Offline transcription failed: " + (e.message ?: "unknown error"))
                }
            } finally {
                try { wav?.delete() } catch (_: Exception) { }
            }
        }
    }

    private fun analyzeWithLocalModel(data: String, auto: Boolean) {
        val modelFile = try { qwenModelFile() } catch (_: Exception) { null }
        if (modelFile == null || !modelFile.exists() || modelFile.length() < 420_000_000L) {
            setStatus("Qwen3 local model is not downloaded. Open Settings and download the offline AI models first.")
            showSettings()
            return
        }
        if (busy && !auto) return
        busy = true
        refreshFooter()
        setStatus("Loading Qwen3 on this device…")
        executor.execute {
            var model: dev.ffmpegkit.llama.LlamaModel? = null
            try {
                val timestampPattern = Regex("""\[\d{2}:\d{2}\.\d{2}\s*-\s*\d{2}:\d{2}\.\d{2}\]""")
                if (!timestampPattern.containsMatchIn(data)) {
                    throw IOException("The transcript needs timestamps. Use Offline auto-transcribe or import an SRT/VTT subtitle file.")
                }
                model = runBlocking {
                    Llama.loadModel(
                        modelPath = modelFile.absolutePath,
                        config = LlamaConfig(contextSize = 4096, threads = 4, temperature = 0.15f, topP = 0.85f, topK = 30)
                    )
                }
                val lines = data.lines().filter { it.isNotBlank() }
                // Overlapping transcript windows preserve topic boundaries and Q&A context.
                val chunks = lines.windowed(size = 36, step = 28, partialWindows = true)
                val chunkTotal = chunks.size
                val candidates = mutableListOf<Highlight>()
                for ((index, chunk) in chunks.withIndex()) {
                    val chunkText = chunk.joinToString("\n")
                    val prompt = """
Build a chronological VIDEO TOPIC MAP for this transcript segment. This is NOT just a top-highlights list.
Identify every meaningful distinct topic, subtopic, question-and-answer exchange, story, announcement, explanation, example, debate, prediction, practical lesson and conclusion actually discussed in this segment.
Return ONLY a JSON array with up to 10 distinct topic segments. Include every meaningful topic supported by this segment.
[{"start_seconds":120.5,"end_seconds":164.2,"hook":"Clear specific topic title","reason":"What is discussed and why the viewer may care","category":"Question and answer","importance_score":9}]
Rules:
- Put topics in chronological order. Do not rank/reorder them by importance.
- Use exact start and end boundaries supported by the transcript timestamps. Convert mm:ss.cc to seconds from the beginning of the original video.
- Each topic must describe a coherent continuous section, not one isolated sentence.
- Prefer natural subject boundaries, not fixed-duration windows. A coherent topic may exceed 180 seconds; keep the complete topic in the map instead of dropping it or cutting it arbitrarily. The creator can choose a shorter export range later. Split only when a genuinely different subject or subtopic begins.
- Include the question with its answer when needed for context. Preserve complete thoughts.
- Do not invent timestamps, events, names, facts or topics. Do not duplicate a topic from another segment.
- Give each title a clear subject, not generic text such as "Important point".
- importance_score is 1–10. It indicates usefulness, not list order.
- No markdown and no explanation outside the JSON array.
Transcript segment $index of $chunkTotal:
$chunkText
""".trimIndent()
                    val answer = runBlocking {
                        Llama.complete(
                            model,
                            prompt = prompt,
                            systemPrompt = "You are a factual long-form video topic mapper. Cover the segment comprehensively, preserve chronology and timestamps, and return strict JSON only.",
                            maxTokens = 1400
                        )
                    }
                    candidates.addAll(alignTopicTimesToChunk(parseHighlights(answer.text), chunkText))
                    val progress = ((index + 1) * 100 / chunkTotal).coerceIn(1, 100)
                    setStatus("Offline Qwen3 is mapping video topics… chunk " + (index + 1) + "/" + chunkTotal + " · " + progress + "%")
                }
                val best = normalizeTopicMap(candidates)
                runOnUiThread {
                    showHighlights(best)
                    setStatus(if (best.isEmpty()) {
                        "Qwen3 did not return reliable timestamped topics. Check transcript timestamps and try a longer transcript."
                    } else {
                        "Video Topic Map ready: " + best.size + " topics found in chronological order. Tap Play or select any topic."
                    })
                }
            } catch (e: Exception) {
                runOnUiThread { setStatus("Offline Qwen3 analysis failed: " + (e.message ?: "unknown error")) }
            } finally {
                try { if (model != null) Llama.releaseModel(model) } catch (_: Exception) { }
                runOnUiThread {
                    busy = false
                    refreshFooter()
                }
            }
        }
    }

    /**
     * Decode the selected video's audio track using Android MediaCodec, downmix to mono,
     * linearly resample to 16 kHz, and write the PCM WAV required by local Whisper.
     */
    private fun extractWav16kMono(uri: android.net.Uri): File {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null
        var decoderStarted = false
        val rawPcm = File(cacheDir, "sf-local-" + System.currentTimeMillis() + ".pcm")
        val wavFile = File(cacheDir, "sf-local-" + System.currentTimeMillis() + ".wav")
        try {
            extractor.setDataSource(this, uri, null)
            var audioTrack = -1
            var inputFormat: android.media.MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(android.media.MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrack = i
                    inputFormat = format
                    break
                }
            }
            if (audioTrack < 0 || inputFormat == null) throw IOException("The selected video has no audio track.")
            val mime = inputFormat.getString(android.media.MediaFormat.KEY_MIME)!!
            extractor.selectTrack(audioTrack)
            decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()
            decoderStarted = true

            val pcm = BufferedOutputStream(FileOutputStream(rawPcm), 1024 * 1024)
            var sourceRate = 0
            var channels = 0
            var pcmEncoding = AudioFormat.ENCODING_PCM_16BIT
            var previous = 0f
            var havePrevious = false
            var sourceIndex = 0L
            var nextOutputPosition = 0.0
            var outputSamples = 0L
            var inputDone = false
            var outputDone = false

            fun writeMonoSample(value: Float) {
                val safe = value.coerceIn(-1f, 1f)
                val shortValue = (safe * 32767f).toInt()
                pcm.write(shortValue and 0xff)
                pcm.write((shortValue shr 8) and 0xff)
                outputSamples++
            }

            fun consumeMonoSample(value: Float) {
                if (!havePrevious) {
                    writeMonoSample(value)
                    previous = value
                    havePrevious = true
                    sourceIndex = 0L
                    nextOutputPosition = sourceRate.toDouble() / 16000.0
                    return
                }
                val currentIndex = sourceIndex + 1L
                val ratio = sourceRate.toDouble() / 16000.0
                while (nextOutputPosition <= currentIndex.toDouble()) {
                    val fraction = (nextOutputPosition - sourceIndex.toDouble()).coerceIn(0.0, 1.0)
                    val interpolated = previous + (value - previous) * fraction.toFloat()
                    writeMonoSample(interpolated)
                    nextOutputPosition += ratio
                }
                previous = value
                sourceIndex = currentIndex
            }

            try {
                val info = MediaCodec.BufferInfo()
                while (!outputDone) {
                    if (!inputDone) {
                        val inputIndex = decoder.dequeueInputBuffer(10_000)
                        if (inputIndex >= 0) {
                            val inputBuffer = decoder.getInputBuffer(inputIndex)
                                ?: throw IOException("Audio decoder returned an empty input buffer.")
                            inputBuffer.clear()
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            } else {
                                val sampleTime = extractor.sampleTime
                                val sampleFlags = extractor.sampleFlags
                                decoder.queueInputBuffer(inputIndex, 0, sampleSize, sampleTime, sampleFlags)
                                extractor.advance()
                            }
                        }
                    }

                    val outputIndex = decoder.dequeueOutputBuffer(info, 10_000)
                    when {
                        outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val outputFormat = decoder.outputFormat
                            sourceRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            channels = outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            pcmEncoding = if (outputFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                                outputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
                            } else AudioFormat.ENCODING_PCM_16BIT
                            if (sourceRate <= 0 || channels <= 0) throw IOException("Audio decoder returned invalid sample-rate or channel information.")
                        }
                        outputIndex >= 0 -> {
                            if (info.size > 0) {
                                if (sourceRate <= 0 || channels <= 0) throw IOException("Audio format was not available from the decoder.")
                                val outputBuffer = decoder.getOutputBuffer(outputIndex)
                                if (outputBuffer != null) {
                                    val data = outputBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
                                    data.position(info.offset)
                                    data.limit(info.offset + info.size)
                                    val bytesPerSample = when (pcmEncoding) {
                                        AudioFormat.ENCODING_PCM_FLOAT -> 4
                                        AudioFormat.ENCODING_PCM_16BIT -> 2
                                        AudioFormat.ENCODING_PCM_8BIT -> 1
                                        else -> throw IOException("This device returned an unsupported decoded PCM audio format.")
                                    }
                                    val frameBytes = bytesPerSample * channels
                                    val frames = data.remaining() / frameBytes
                                    repeat(frames) {
                                        var sum = 0f
                                        repeat(channels) {
                                            val sample = when (pcmEncoding) {
                                                AudioFormat.ENCODING_PCM_FLOAT -> data.float
                                                AudioFormat.ENCODING_PCM_16BIT -> data.short.toFloat() / 32768f
                                                else -> ((data.get().toInt() and 0xff) - 128) / 128f
                                            }
                                            sum += sample
                                        }
                                        consumeMonoSample(sum / channels.toFloat())
                                    }
                                }
                            }
                            decoder.releaseOutputBuffer(outputIndex, false)
                            if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true
                        }
                    }
                }
            } finally {
                pcm.flush()
                pcm.close()
            }

            if (outputSamples < 1600L) throw IOException("The video did not contain enough decodable speech audio.")
            val dataBytes = rawPcm.length()
            FileOutputStream(wavFile).buffered(1024 * 1024).use { output ->
                fun writeIntLe(value: Int) {
                    output.write(value and 0xff)
                    output.write((value shr 8) and 0xff)
                    output.write((value shr 16) and 0xff)
                    output.write((value shr 24) and 0xff)
                }
                fun writeShortLe(value: Int) {
                    output.write(value and 0xff)
                    output.write((value shr 8) and 0xff)
                }
                output.write("RIFF".toByteArray(Charsets.US_ASCII))
                writeIntLe((36L + dataBytes).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                output.write("WAVE".toByteArray(Charsets.US_ASCII))
                output.write("fmt ".toByteArray(Charsets.US_ASCII))
                writeIntLe(16)
                writeShortLe(1)
                writeShortLe(1)
                writeIntLe(16000)
                writeIntLe(32000)
                writeShortLe(2)
                writeShortLe(16)
                output.write("data".toByteArray(Charsets.US_ASCII))
                writeIntLe(dataBytes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                rawPcm.inputStream().buffered(1024 * 1024).use { it.copyTo(output, 1024 * 1024) }
            }
            return wavFile
        } catch (e: Exception) {
            rawPcm.delete()
            wavFile.delete()
            throw IOException("Could not decode the selected video's audio to a 16 kHz mono WAV: " + (e.message ?: "unsupported audio codec"), e)
        } finally {
            try { if (decoderStarted) decoder?.stop() } catch (_: Exception) { }
            try { decoder?.release() } catch (_: Exception) { }
            try { extractor.release() } catch (_: Exception) { }
            rawPcm.delete()
        }
    }

    private fun transcribe(file: File, key: String, providerId: Int): String {
        val url = if (providerId == 0) "https://api.groq.com/openai/v1/audio/transcriptions" else "https://api.openai.com/v1/audio/transcriptions"
        val model = if (providerId == 0) "whisper-large-v3-turbo" else "whisper-1"
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("model", model)
            .addFormDataPart("response_format", "verbose_json")
            .addFormDataPart("timestamp_granularities[]", "segment")
            .addFormDataPart("file", file.name, file.asRequestBody("audio/wav".toMediaType()))
            .build()
        val req = Request.Builder().url(url).header("Authorization", "Bearer " + key).post(body).build()
        http.newCall(req).execute().use { response ->
            val raw = response.body?.string() ?: ""
            if (!response.isSuccessful) throw IOException("Transcription API HTTP " + response.code + ": " + apiError(raw))
            val json = JSONObject(raw)
            val segments = json.optJSONArray("segments") ?: return json.optString("text").trim()
            val out = StringBuilder()
            for (i in 0 until segments.length()) {
                val item = segments.getJSONObject(i)
                val a = item.optDouble("start", Double.NaN)
                val b = item.optDouble("end", Double.NaN)
                val words = item.optString("text").trim()
                if (words.isNotBlank() && a.isFinite() && b.isFinite()) {
                    out.append("[").append(clock(a)).append(" - ").append(clock(b)).append("] ").append(words).append('\n')
                }
            }
            return out.toString().trim()
        }
    }

    private fun analyzeTranscript(auto: Boolean = false) {
        val data = if (::transcriptEdit.isInitialized) transcriptEdit.text.toString().trim() else transcript.trim()
        if (data.isBlank()) { setStatus("Paste a timestamped transcript or auto-transcribe the video first."); return }
        val timestampedLines = timestampedTranscriptLineCount(data)
        if (timestampedLines == 0) {
            setStatus("No timestamped transcript lines found. Import SRT/VTT or lines like [00:00.00 - 00:04.20] spoken words before topic mapping.")
            return
        }
        if (provider == 3) {
            analyzeWithLocalModel(data, auto)
            return
        }
        val key = readKey(provider)
        if (key.isBlank()) { setStatus("Add an API key in Settings."); showSettings(); return }
        if (busy && !auto) return
        busy = true
        refreshFooter()
        setStatus("AI is mapping all topics across the transcript…")
        executor.execute {
            var handedOffToLocal = false
            try {
                // Split long recordings into overlapping sections so the model can map
                // the full transcript instead of squeezing a podcast into eight highlights.
                val transcriptLines = data.lines().filter { it.isNotBlank() }
                val chunks = transcriptLines.windowed(size = 80, step = 64, partialWindows = true)
                val chunkTotal = chunks.size
                val candidates = mutableListOf<Highlight>()
                for ((index, chunk) in chunks.withIndex()) {
                    val chunkText = chunk.joinToString("\n")
                    val prompt = """
You are a professional long-form video topic mapper and Shorts editor.
Create a CHRONOLOGICAL VIDEO TOPIC MAP for this transcript segment. Do not return only the most viral or important highlights. Identify every distinct meaningful subject actually discussed.
Look for:
- Introduction and background.
- Each different subject or subtopic.
- Questions and their complete answers.
- Personal stories, anecdotes, examples and explanations.
- Product launches, announcements, facts and claims.
- Opinions, debates, predictions and practical lessons.
- Topic changes, transitions and the conclusion.
Return ONLY a valid JSON array with up to 16 topic segments for this transcript segment. Identify all meaningful topics supported by this segment; do not stop early merely because several topics have already been found.
Exact shape:
[{"start_seconds":120.5,"end_seconds":164.2,"hook":"Specific topic title","reason":"What is discussed in this section and why it matters","category":"Question and answer","importance_score":9}]
Topic segmentation rules:
1. List topics in chronological order. Never sort the list by importance.
2. Use exact numeric start_seconds and end_seconds from transcript timing boundaries. They are seconds from the beginning of the ORIGINAL video.
3. A topic covers the continuous section where that subject is discussed, with its real beginning and ending. Do not use a generic 60/90-second window as the topic timestamp.
4. Prefer natural subject boundaries, not fixed-duration windows. A coherent topic may exceed 180 seconds; keep the complete topic in the map instead of dropping it or cutting it arbitrarily. The creator can choose a shorter export range later. Split only when a genuinely different subject or subtopic begins.
5. Include every meaningful distinct topic; do not limit results to only eight for the whole video. This response covers only one overlapping transcript chunk. Other chunks will be analysed and merged.
6. Include the complete question with its answer when needed for context. Do not start halfway through an answer or end mid-sentence.
7. Do not invent or guess timestamps, speakers, topics, statements or facts. Use only content supported by this transcript segment.
8. Do not create duplicate or substantially overlapping topics within this segment.
9. Make titles informative and specific. Categories should be one of: Introduction, Background, Question and answer, Important announcement, Key insight, Explanation, Story, Example, Opinion, Debate, Prediction, Practical advice, Conclusion, Other.
10. importance_score is 1–10 and indicates usefulness only; it does not determine chronological order.
11. Return numeric seconds. No markdown fences or text outside the JSON array.
Transcript chunk $index of $chunkTotal:
$chunkText
""".trimIndent()
                    val raw = chat(prompt, key, provider)
                    candidates.addAll(alignTopicTimesToChunk(parseHighlights(raw), chunkText))
                    val progress = ((index + 1) * 100 / chunkTotal).coerceIn(1, 100)
                    runOnUiThread {
                        setStatus("Mapping video topics… section " + (index + 1) + "/" + chunkTotal + " · " + progress + "%")
                    }
                }
                val topics = normalizeTopicMap(candidates)
                if (topics.isEmpty()) {
                    throw IOException("The AI response contained no usable timestamped topic cards across " + chunkTotal + " transcript section(s). Check the provider response, transcript timestamps, or try another AI model.")
                }
                runOnUiThread {
                    showHighlights(topics)
                    selectedHighlightIndex = 0
                    topics.firstOrNull()?.let { first ->
                        if (first.start != null && first.end != null) applyExactHighlightRange(first.start, first.end)
                    }
                    setStatus("Video Topic Map ready: " + topics.size + " topics found across " + chunkTotal +
                        " transcript section(s). Topics are in chronological order. Tap Play or select any topic.")
                }
            } catch (e: Exception) {
                val useLocal = isConnectivityFailure(e) && try { qwenModelFile().let { it.exists() && it.length() >= 420_000_000L } } catch (_: Exception) { false }
                if (useLocal) handedOffToLocal = true
                runOnUiThread {
                    if (useLocal) {
                        provider = 3
                        prefs.edit().putInt("provider", 3).apply()
                        setStatus("Cloud connection unavailable. Switching topic mapping to local Qwen3…")
                        analyzeWithLocalModel(data, true)
                    } else {
                        setStatus("Topic mapping failed: " + (e.message ?: "unknown AI error"))
                    }
                }
            } finally {
                if (!handedOffToLocal) runOnUiThread { busy = false; refreshFooter() }
            }
        }
    }

    private data class Highlight(
        val start: Double?,
        val end: Double?,
        val hook: String,
        val reason: String,
        val category: String,
        val importance: Int
    )

    private fun chat(prompt: String, key: String, p: Int): String {
        val req: Request
        if (p == 0 || p == 1) {
            val url = if (p == 0) "https://api.groq.com/openai/v1/chat/completions" else "https://api.openai.com/v1/chat/completions"
            val model = if (p == 0) "openai/gpt-oss-120b" else "gpt-4o-mini"
            val body = JSONObject().put("model", model).put("temperature", 0.2)
                .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            req = Request.Builder().url(url).header("Authorization", "Bearer " + key)
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        } else {
            val body = JSONObject().put("model", "claude-haiku-4-5-20251001").put("max_tokens", 5000)
                .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            req = Request.Builder().url("https://api.anthropic.com/v1/messages")
                .header("x-api-key", key).header("anthropic-version", "2023-06-01")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        }
        http.newCall(req).execute().use { response ->
            val raw = response.body?.string() ?: ""
            if (!response.isSuccessful) throw IOException("AI API HTTP " + response.code + ": " + apiError(raw))
            val json = JSONObject(raw)
            return if (p == 2) json.optJSONArray("content")?.optJSONObject(0)?.optString("text") ?: ""
            else json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content") ?: ""
        }
    }

    private fun timestampedTranscriptLineCount(data: String): Int {
        val pattern = Regex("""\[\s*\d{1,3}:\d{2}\.\d{1,3}\s*[-–]\s*\d{1,3}:\d{2}\.\d{1,3}\s*\]""")
        return data.lineSequence().count { pattern.containsMatchIn(it) }
    }

    private fun parseTimeValue(value: Any?): Double? {
        if (value == null || value == JSONObject.NULL) return null
        if (value is Number) return value.toDouble().takeIf { it.isFinite() && it >= 0.0 }
        val raw = value.toString().trim().trim('"', '\'').replace(',', '.')
        if (raw.isBlank()) return null
        raw.toDoubleOrNull()?.let { return it.takeIf { number -> number.isFinite() && number >= 0.0 } }
        val pieces = raw.split(":")
        val parsed = pieces.map { it.toDoubleOrNull() ?: return null }
        val seconds = when (parsed.size) {
            2 -> parsed[0] * 60.0 + parsed[1]
            3 -> parsed[0] * 3600.0 + parsed[1] * 60.0 + parsed[2]
            else -> return null
        }
        return seconds.takeIf { it.isFinite() && it >= 0.0 }
    }

    private fun firstJsonValue(item: JSONObject, keys: List<String>): Any? {
        for (key in keys) {
            if (item.has(key) && !item.isNull(key)) return item.opt(key)
        }
        return null
    }

    private fun firstJsonText(item: JSONObject, keys: List<String>, fallback: String): String {
        val value = firstJsonValue(item, keys) ?: return fallback
        return value.toString().trim().ifBlank { fallback }
    }

    private fun parseTopicArray(array: JSONArray): List<Highlight> {
        val out = mutableListOf<Highlight>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val start = parseTimeValue(firstJsonValue(item, listOf(
                "start_seconds", "start", "start_time", "start_timestamp", "from_seconds", "begin_seconds"
            )))
            val end = parseTimeValue(firstJsonValue(item, listOf(
                "end_seconds", "end", "end_time", "end_timestamp", "to_seconds", "finish_seconds"
            )))
            if (start == null || end == null || end <= start) continue
            if (durationMs > 0L && end * 1000.0 > durationMs + 5000.0) continue
            val hook = firstJsonText(item, listOf("hook", "title", "topic_title", "topic", "name"), "Suggested topic")
            val reason = firstJsonText(item, listOf("reason", "description", "explanation", "summary"), "")
            val category = firstJsonText(item, listOf("category", "topic_type", "type"), "Topic")
            val scoreValue = firstJsonValue(item, listOf("importance_score", "importance", "score"))
            val score = when (scoreValue) {
                is Number -> scoreValue.toInt()
                else -> scoreValue?.toString()?.toDoubleOrNull()?.toInt() ?: 0
            }.coerceIn(0, 10)
            out.add(Highlight(start, end, hook, reason, category, score))
        }
        return out
    }

    private fun parseHighlights(raw: String): List<Highlight> {
        // Model responses sometimes wrap JSON in prose/Markdown, or return { "topics": [...] }.
        // Scan balanced JSON containers while respecting quoted strings, then accept the first
        // container that yields usable timestamped topic objects.
        for (startIndex in raw.indices) {
            val opening = raw[startIndex]
            if (opening != '[' && opening != '{') continue
            val stack = mutableListOf<Char>()
            var inString = false
            var escaped = false
            var endIndex = -1
            var malformed = false
            for (index in startIndex until raw.length) {
                val ch = raw[index]
                if (inString) {
                    if (escaped) escaped = false
                    else if (ch == '\\') escaped = true
                    else if (ch == '"') inString = false
                    continue
                }
                when (ch) {
                    '"' -> inString = true
                    '{', '[' -> stack.add(ch)
                    '}', ']' -> {
                        if (stack.isEmpty()) {
                            malformed = true
                            break
                        }
                        val top = stack.removeAt(stack.lastIndex)
                        if ((top == '{' && ch != '}') || (top == '[' && ch != ']')) {
                            malformed = true
                            break
                        }
                        if (stack.isEmpty()) {
                            endIndex = index
                            break
                        }
                    }
                }
            }
            if (malformed || endIndex <= startIndex) continue
            val payload = raw.substring(startIndex, endIndex + 1)
            try {
                val parsed = if (opening == '[') {
                    parseTopicArray(JSONArray(payload))
                } else {
                    val rootObject = JSONObject(payload)
                    var nested: JSONArray? = null
                    for (key in listOf("topics", "topic_map", "topic_segments", "segments", "highlights", "clips", "results", "data")) {
                        nested = rootObject.optJSONArray(key)
                        if (nested != null) break
                    }
                    if (nested != null) parseTopicArray(nested)
                    else if (firstJsonValue(rootObject, listOf("start_seconds", "start", "start_time")) != null) {
                        parseTopicArray(JSONArray().put(rootObject))
                    } else emptyList()
                }
                if (parsed.isNotEmpty()) return parsed
            } catch (_: Exception) {
                // Try the next balanced JSON container in the response.
            }
        }
        return emptyList()
    }

    private fun transcriptTimeRange(text: String): Pair<Double, Double>? {
        val pattern = Regex("""\[\s*(\d{1,3}:\d{2}\.\d{1,3})\s*[-–]\s*(\d{1,3}:\d{2}\.\d{1,3})\s*\]""")
        val matches = pattern.findAll(text).toList()
        if (matches.isEmpty()) return null
        val first = matches.first()
        val last = matches.last()
        val start = parseTimeValue(first.groupValues[1]) ?: return null
        val end = parseTimeValue(last.groupValues[2]) ?: return null
        return if (end > start) Pair(start, end) else null
    }

    private fun alignTopicTimesToChunk(topics: List<Highlight>, chunkText: String): List<Highlight> {
        if (topics.isEmpty()) return topics
        val range = transcriptTimeRange(chunkText) ?: return topics
        val chunkStart = range.first
        val chunkEnd = range.second
        if (chunkStart <= 2.0) return topics
        val minimumStart = topics.mapNotNull { it.start }.minOrNull() ?: return topics
        val maximumEnd = topics.mapNotNull { it.end }.maxOrNull() ?: return topics
        val chunkSpan = chunkEnd - chunkStart
        // Some models return seconds relative to the current chunk despite the prompt asking
        // for source-video absolute seconds. Detect that case and offset those values.
        val looksChunkRelative = minimumStart < chunkStart - 2.0 && maximumEnd <= chunkSpan + 20.0
        return if (looksChunkRelative) {
            topics.map { item ->
                item.copy(
                    start = item.start?.plus(chunkStart),
                    end = item.end?.plus(chunkStart)
                )
            }
        } else topics
    }

    private fun normalizeTopicMap(candidates: List<Highlight>): List<Highlight> {
        // Topic Map is the complete topic index, not a Shorts-only clip list. Do not discard a
        // coherent topic merely because it lasts longer than the 180-second single-export limit.
        val chronological = candidates.filter { item ->
            val a = item.start
            val b = item.end
            a != null && b != null && a >= 0.0 && b > a &&
                (durationMs <= 0L || b * 1000.0 <= durationMs + 5000.0)
        }.sortedBy { it.start }
        val merged = mutableListOf<Highlight>()
        for (candidate in chronological) {
            val candidateStart = candidate.start ?: continue
            val candidateEnd = candidate.end ?: continue
            val duplicateIndex = merged.indexOfFirst { existing ->
                val existingStart = existing.start ?: return@indexOfFirst false
                val existingEnd = existing.end ?: return@indexOfFirst false
                val overlap = (minOf(candidateEnd, existingEnd) - maxOf(candidateStart, existingStart)).coerceAtLeast(0.0)
                val shorter = minOf(candidateEnd - candidateStart, existingEnd - existingStart).coerceAtLeast(0.001)
                kotlin.math.abs(candidateStart - existingStart) <= 18.0 && overlap / shorter >= 0.65
            }
            if (duplicateIndex >= 0) {
                if (candidate.importance > merged[duplicateIndex].importance) merged[duplicateIndex] = candidate
            } else {
                merged.add(candidate)
            }
        }
        return merged.sortedBy { it.start }
    }

        private fun suggestionSelectedStatus(index: Int): String =
        "Topic " + (index + 1) + " selected. Exact timestamps loaded into Start / End."

    private fun showHighlights(items: List<Highlight>) {
        if (!::highlightsView.isInitialized) return
        if (items !== highlightItems) {
            queuedHighlightIndices.clear()
            queuedHighlightRanges.clear()
        }
        highlightItems = items
        queuedHighlightIndices.removeAll { it !in items.indices }
        queuedHighlightRanges.keys.removeAll { it !in items.indices }
        if (::topicMapSummaryView.isInitialized) {
            topicMapSummaryView.text = if (items.isEmpty()) "VIDEO TOPIC MAP  ·  No timestamped topics found" else
                "VIDEO TOPIC MAP  ·  " + items.size + " topics detected  ·  chronological order"
        }
        if (selectedHighlightIndex !in items.indices) selectedHighlightIndex = if (items.isNotEmpty()) 0 else -1
        items.getOrNull(selectedHighlightIndex)?.let { selected ->
            if (selected.start != null && selected.end != null && ::startEdit.isInitialized && ::endEdit.isInitialized) {
                applyExactHighlightRange(selected.start, selected.end)
            }
        }
        highlightsView.removeAllViews()
        if (items.isEmpty()) {
            highlightsView.addView(text("No usable topic cards were returned. Check that the transcript has timestamps, try another AI provider, or run Analyze all topics again.", 12f, muted).apply { setPadding(0, dp(8), 0, 0) })
            refreshQueueControls()
            return
        }
        items.forEachIndexed { index, item ->
            val selected = index == selectedHighlightIndex
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(15), dp(14), dp(15), dp(14))
                background = shape(if (selected) Color.rgb(245, 244, 255) else Color.rgb(248, 249, 255), 14,
                    if (selected) accent else border)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    if (item.start != null && item.end != null) {
                        selectedHighlightIndex = index
                        applyExactHighlightRange(item.start, item.end)
                        showHighlights(highlightItems)
                        setStatus(suggestionSelectedStatus(index))
                    } else {
                        setStatus("This suggestion has no exact timestamps. Use a timestamped transcript.")
                    }
                }
            }
            val headingRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            headingRow.addView(text((index + 1).toString() + ". " + item.hook, 14f, ink, true),
                LinearLayout.LayoutParams(0, -2, 1f))
            if (selected) {
                headingRow.addView(text("SELECTED", 9f, accent, true))
            }
            row.addView(headingRow)
            if (item.start != null && item.end != null) {
                row.addView(text(clock(item.start) + "  →  " + clock(item.end), 12f, accent, true)
                    .apply { setPadding(0, dp(5), 0, 0) })
                val topicDuration = item.end - item.start
                val durationText = "Duration  " + clock(topicDuration) +
                    if (topicDuration > 180.0) "  ·  Long topic — trim to 3 minutes or less for Shorts export" else ""
                row.addView(text(durationText, 11f, muted, true)
                    .apply { setPadding(0, dp(3), 0, 0) })
            } else {
                row.addView(text("Timestamp unavailable", 12f, accent, true)
                    .apply { setPadding(0, dp(5), 0, 0) })
            }
            if (item.category.isNotBlank() || item.importance > 0) {
                val meta = listOfNotNull(
                    item.category.takeIf { it.isNotBlank() },
                    if (item.importance > 0) "Importance " + item.importance + "/10" else null
                ).joinToString("  ·  ")
                row.addView(text(meta, 10f, muted, true).apply { setPadding(0, dp(5), 0, 0) })
            }
            if (item.reason.isNotBlank()) {
                row.addView(text(item.reason, 12f, muted)
                    .apply { setPadding(0, dp(6), 0, 0) })
            }
            val queueCheck = CheckBox(this).apply {
                text = if (index in queuedHighlightIndices) "Added to Shorts queue" else "Add this topic to Shorts queue"
                textSize = 12f
                setTextColor(if (index in queuedHighlightIndices) accent else muted)
                buttonTintList = android.content.res.ColorStateList.valueOf(accent)
                isChecked = index in queuedHighlightIndices
                setOnCheckedChangeListener { _, checked ->
                    val accepted = setHighlightQueued(index, checked)
                    if (!accepted) isChecked = false
                    text = if (index in queuedHighlightIndices) "Added to Shorts queue" else "Add this topic to Shorts queue"
                    setTextColor(if (index in queuedHighlightIndices) accent else muted)
                }
            }
            row.addView(queueCheck.apply { setPadding(0, dp(5), 0, 0) })
            if (item.start != null && item.end != null) {
                row.addView(button("▶  Play this topic", false) { playTopicRange(item) }, full(dp(5)))
            }
            highlightsView.addView(row, full(dp(8)))
        }
        refreshQueueControls()
    }

    private fun setHighlightQueued(index: Int, add: Boolean): Boolean {
        val item = highlightItems.getOrNull(index) ?: return false
        val topicStart = item.start ?: return false
        val topicEnd = item.end ?: return false
        if (topicEnd <= topicStart) return false
        if (!add) {
            queuedHighlightIndices.remove(index)
            queuedHighlightRanges.remove(index)
            refreshQueueControls()
            return true
        }
        if (index in queuedHighlightIndices) return true

        var exportStart = topicStart
        var exportEnd = topicEnd
        if (index == selectedHighlightIndex && ::startEdit.isInitialized && ::endEdit.isInitialized) {
            val editedStart = startEdit.text.toString().toDoubleOrNull()
            val editedEnd = endEdit.text.toString().toDoubleOrNull()
            if (editedStart != null && editedEnd != null &&
                editedStart.isFinite() && editedEnd.isFinite() &&
                editedStart >= topicStart && editedEnd <= topicEnd && editedEnd > editedStart) {
                exportStart = editedStart
                exportEnd = editedEnd
            }
        }
        if (exportEnd - exportStart > 180.0) {
            setStatus("This complete topic is longer than 180 seconds. Select the card, shorten Start/End to a range of 180 seconds or less, then add it to the Shorts queue.")
            return false
        }
        if (durationMs > 0L && exportEnd * 1000.0 > durationMs + 250.0) return false

        queuedHighlightIndices.add(index)
        queuedHighlightRanges[index] = Pair(exportStart, exportEnd)
        refreshQueueControls()
        return true
    }

    private fun refreshQueueControls() {
        if (::queueSummaryView.isInitialized) {
            val totalMs = queuedHighlightIndices.sumOf { index ->
                val range = queuedHighlightRanges[index]
                if (range != null) ((range.second - range.first) * 1000.0).toLong()
                else {
                    val item = highlightItems.getOrNull(index)
                    if (item?.start != null && item.end != null) ((item.end - item.start) * 1000.0).toLong() else 0L
                }
            }
            queueSummaryView.text = if (queuedHighlightIndices.isEmpty()) {
                "No topics queued. The Video Topic Map is chronological. Add any number of detected topics to export separate Shorts."
            } else {
                queuedHighlightIndices.size.toString() + " separate Short(s) queued  ·  " + clock(totalMs / 1000.0) + " source footage selected"
            }
        }
        if (::studioQueueButton.isInitialized) {
            studioQueueButton.text = if (queuedHighlightIndices.isEmpty()) {
                "Open Studio Editor · current highlight"
            } else {
                "Open Studio Editor · " + queuedHighlightIndices.size + " topic(s) as one timeline"
            }
        }
        if (::batchExportButton.isInitialized) {
            batchExportButton.isEnabled = queuedHighlightIndices.isNotEmpty() && !busy
            batchExportButton.alpha = if (batchExportButton.isEnabled) 1f else 0.45f
            batchExportButton.text = if (queuedHighlightIndices.isEmpty()) {
                "Add topics above to enable separate Shorts export"
            } else if (batchExportActive) {
                "Exporting topic " + (batchExportIndex + 1) + " of " + batchExportItems.size + "…"
            } else {
                "Export " + queuedHighlightIndices.size + " queued topics as separate Shorts"
            }
        }
    }

    private fun applyPendingMarkersOrDefault() {
        val pendingStart = pendingYoutubeStartSeconds
        val pendingEnd = pendingYoutubeEndSeconds
        val durationSeconds = if (durationMs > 0) durationMs / 1000.0 else 0.0
        if (pendingStart != null && pendingEnd != null && pendingStart >= 0 && pendingEnd > pendingStart &&
            (durationSeconds <= 0 || pendingEnd <= durationSeconds)) {
            startText = fmtNumber(pendingStart)
            endText = fmtNumber(pendingEnd)
        } else {
            startText = "0"
            endText = if (durationMs > 0) fmtNumber(minOf(30_000L, durationMs) / 1000.0) else "30"
        }
    }

    private fun isYouTubeHost(host: String): Boolean {
        val h = host.lowercase(Locale.US).removePrefix("www.")
        return h == "youtu.be" || h.endsWith(".youtu.be") || h == "youtube.com" ||
            h.endsWith(".youtube.com") || h == "youtube-nocookie.com" ||
            h.endsWith(".youtube-nocookie.com") || h == "googlevideo.com" || h.endsWith(".googlevideo.com")
    }

    private fun extractYouTubeVideoId(input: String): String? {
        val raw = input.trim()
        if (raw.isBlank() || raw.contains(Regex("\\s"))) return null
        val normalized = if (raw.startsWith("https://", true) || raw.startsWith("http://", true)) raw else "https://$raw"
        val parsed = android.net.Uri.parse(normalized)
        val host = parsed.host?.lowercase(Locale.US)?.removePrefix("www.") ?: return null
        if (!(host == "youtu.be" || host == "youtube.com" || host.endsWith(".youtube.com"))) return null
        val path = parsed.pathSegments
        val id = when {
            host == "youtu.be" -> path.firstOrNull()
            parsed.path.equals("/watch", true) -> parsed.getQueryParameter("v")
            path.size >= 2 && listOf("shorts", "embed", "live", "v").contains(path[0]) -> path[1]
            else -> null
        }
        return id?.takeIf { Regex("^[A-Za-z0-9_-]{11}$").matches(it) }
    }

    private fun openYouTubePreview() {
        val id = extractYouTubeVideoId(youtubeUrlEdit.text.toString())
        if (id == null) {
            setStatus("Enter a valid YouTube watch, Shorts, embed, or youtu.be URL.")
            return
        }
        val intent = Intent(this, YouTubePreviewActivity::class.java)
            .putExtra(YouTubePreviewActivity.EXTRA_VIDEO_ID, id)
        youtubePreviewLauncher.launch(intent)
    }

    private fun sendYouTubeLinkToYtdlnis() {
        val raw = youtubeUrlEdit.text.toString().trim()
        val url = raw.toHttpUrlOrNull()
        if (url == null || url.scheme !in setOf("http", "https") || !isYouTubeHost(url.host)) {
            setStatus("Paste a valid YouTube watch, Shorts, or youtu.be URL first.")
            return
        }

        // YTDLnis documents this third-party hand-off: ACTION_SEND + text/*,
        // EXTRA_TEXT=<URL>, TYPE=video, BACKGROUND=false.
        // The user reviews and starts the download inside the separately installed app.
        val handoff = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, raw)
            putExtra("TYPE", "video")
            putExtra("BACKGROUND", "false")
            setPackage("com.deniscerri.ytdl")
        }
        try {
            startActivity(handoff)
            setStatus("Link sent to YTDLnis. Complete the download there, then share the saved video to ShortsForge or choose it from Downloads.")
        } catch (_: android.content.ActivityNotFoundException) {
            android.app.AlertDialog.Builder(this)
                .setTitle("YTDLnis is not installed")
                .setMessage("Install YTDLnis separately from its official GitHub releases. Return to ShortsForge and tap this button again. After downloading, share the video file back to ShortsForge.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Open official releases") { _, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/deniscerri/ytdlnis/releases")))
                    } catch (_: Exception) {
                        setStatus("Open github.com/deniscerri/ytdlnis/releases in your browser to install YTDLnis.")
                    }
                }
                .show()
        }
    }

    private fun importAuthorizedVideoUrl() {
        val raw = directVideoUrlEdit.text.toString().trim()
        if (raw.isBlank()) {
            setStatus("Paste a creator-authorized direct video file URL first.")
            return
        }
        val url = raw.toHttpUrlOrNull()
        if (url == null || url.scheme != "https") {
            setStatus("Use a valid HTTPS direct video file link.")
            return
        }
        if (isYouTubeHost(url.host)) {
            setStatus("YouTube watch URLs can be previewed and marked, but cannot be imported as editable MP4 files here.")
            return
        }
        val confirmation = CheckBox(this).apply {
            text = "I own this video or have permission from the rights holder to download and edit this direct video file."
            setTextColor(ink)
            textSize = 13f
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Confirm video permission")
            .setMessage("This imports a direct HTTPS video file into ShortsForge. It does not extract video streams from YouTube pages.")
            .setView(confirmation)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ ->
                if (!confirmation.isChecked) {
                    setStatus("Confirm that you have permission to use this source video before importing it.")
                } else {
                    downloadAuthorizedVideo(url.toString())
                }
            }
            .show()
    }

    private fun downloadAuthorizedVideo(url: String) {
        if (busy) return
        busy = true
        refreshFooter()
        setStatus("Connecting to the authorized video file…")
        executor.execute {
            var partial: File? = null
            var destination: File? = null
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "ShortsForge-Mobile/1.7")
                    .header("Accept", "video/*,application/octet-stream;q=0.9,*/*;q=0.5")
                    .get()
                    .build()
                sourceDownloadHttp.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Video URL returned HTTP " + response.code)
                    val finalHost = response.request.url.host
                    if (isYouTubeHost(finalHost)) {
                        throw IOException("The link redirected to YouTube. Use a creator-provided direct video file URL instead.")
                    }
                    val body = response.body ?: throw IOException("The video URL returned an empty response.")
                    val contentType = body.contentType()?.let { it.toString().substringBefore(";").lowercase(Locale.US) }.orEmpty()
                    val pathExtension = response.request.url.encodedPath.substringAfterLast('.', "").lowercase(Locale.US)
                    val extension = when {
                        videoExtensions.contains(pathExtension) -> pathExtension
                        contentType == "video/mp4" -> "mp4"
                        contentType == "video/quicktime" -> "mov"
                        contentType == "video/webm" -> "webm"
                        contentType == "video/x-matroska" -> "mkv"
                        contentType == "video/3gpp" -> "3gp"
                        contentType == "video/x-m4v" -> "m4v"
                        else -> ""
                    }
                    if (extension.isBlank() || !(contentType.startsWith("video/") || videoExtensions.contains(pathExtension))) {
                        throw IOException("The server did not return a supported video file. Use a direct MP4/MOV/WebM/MKV link.")
                    }
                    val maxBytes = 1024L * 1024L * 1024L
                    val advertisedLength = body.contentLength()
                    if (advertisedLength > maxBytes) throw IOException("The video file exceeds the 1 GB import limit.")
                    val directory = File(filesDir, "imported-videos")
                    if (!directory.exists() && !directory.mkdirs()) throw IOException("Could not create the imported video folder.")
                    val safeName = "ShortsForge_Source_" + System.currentTimeMillis() + "." + extension
                    destination = File(directory, safeName)
                    partial = File(directory, safeName + ".part")
                    body.byteStream().use { input ->
                        FileOutputStream(partial!!).use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var total = 0L
                            var nextReport = 4L * 1024L * 1024L
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count
                                if (total > maxBytes) throw IOException("The video file exceeded the 1 GB import limit.")
                                output.write(buffer, 0, count)
                                if (total >= nextReport) {
                                    val progress = if (advertisedLength > 0) (total * 100 / advertisedLength).coerceIn(0, 100).toInt() else -1
                                    runOnUiThread {
                                        setStatus(if (progress >= 0) "Importing video file… " + progress + "%" else "Importing video file… " + (total / (1024 * 1024)) + " MB")
                                    }
                                    nextReport = total + 4L * 1024L * 1024L
                                }
                            }
                            output.flush()
                            if (total < 64L * 1024L) throw IOException("The downloaded file is too small to be a valid video.")
                        }
                    }
                }
                if (partial == null || destination == null || !partial!!.renameTo(destination!!)) {
                    throw IOException("The downloaded video could not be saved in app storage.")
                }
                val importedUri = android.net.Uri.fromFile(destination!!)
                val importedDuration = readDuration(importedUri)
                if (importedDuration <= 0L) throw IOException("Android could not read the video's duration. The file may be unsupported or damaged.")
                runOnUiThread {
                    selectedVideo = importedUri
                    videoName = destination!!.name
                    durationMs = importedDuration
                    applyPendingMarkersOrDefault()
                    if (::videoNameView.isInitialized) {
                        videoNameView.text = videoName
                        videoDurationView.text = "Duration  " + clock(durationMs / 1000.0) + " · Imported direct video file"
                        playerView.visibility = View.VISIBLE
                        player?.setMediaItem(MediaItem.fromUri(importedUri))
                        player?.prepare()
                        player?.playWhenReady = false
                        startEdit.setText(startText)
                        endEdit.setText(endText)
                    }
                    busy = false
                    refreshFooter()
                    val markersMessage = if (pendingYoutubeStartSeconds != null && pendingYoutubeEndSeconds != null) {
                        " The saved YouTube preview markers were applied where they fit this source file."
                    } else ""
                    setStatus("Authorized video file imported. Starting full-video topic discovery…" + markersMessage)
                    autoStartAnalysisForSelectedSource()
                }
                partial?.delete()
            } catch (e: Exception) {
                partial?.delete()
                destination?.delete()
                runOnUiThread {
                    busy = false
                    refreshFooter()
                    setStatus("Direct video import failed: " + (e.message ?: "unknown download error"))
                }
            }
        }
    }

    private fun openStudioEditor() {
        val uri = selectedVideo ?: run { setStatus("Choose a video first."); return }
        val chosen = queuedHighlightIndices.mapNotNull { index ->
            val item = highlightItems.getOrNull(index) ?: return@mapNotNull null
            val range = queuedHighlightRanges[index]
            val from = range?.first ?: item.start ?: return@mapNotNull null
            val to = range?.second ?: item.end ?: return@mapNotNull null
            if (to <= from) null else item.copy(start = from, end = to)
        }
        val ranges = if (chosen.isNotEmpty()) {
            chosen.flatMap { item -> listOf((item.start!! * 1000).toLong(), (item.end!! * 1000).toLong()) }.toLongArray()
        } else {
            val start = startEdit.text.toString().toDoubleOrNull()
            val end = endEdit.text.toString().toDoubleOrNull()
            if (start == null || end == null || !start.isFinite() || !end.isFinite() || start < 0 || end <= start) {
                setStatus("Choose a valid clip range before opening Studio Editor.")
                return
            }
            longArrayOf((start * 1000).toLong(), (end * 1000).toLong())
        }
        val queuedTotal = ranges.toList().chunked(2).sumOf { it[1] - it[0] }
        if (queuedTotal > 180_000L) {
            setStatus("Your combined topics exceed the 180-second Studio export limit. Shorten or remove a topic.")
            return
        }
        val intent = Intent(this, StudioEditorActivity::class.java)
            .putExtra(StudioEditorActivity.EXTRA_SOURCE_URI, uri.toString())
            .putExtra(StudioEditorActivity.EXTRA_SOURCE_DURATION, durationMs)
            .putExtra(StudioEditorActivity.EXTRA_CLIP_RANGES_MS, ranges)
            .putStringArrayListExtra(StudioEditorActivity.EXTRA_CLIP_TITLES, ArrayList(chosen.map { it.hook }))
            .putExtra(StudioEditorActivity.EXTRA_START_MS, ranges[0])
            .putExtra(StudioEditorActivity.EXTRA_END_MS, ranges[1])
        startActivity(intent)
    }

    private fun startQueuedHighlightsBatchExport() {
        if (busy || batchExportActive) return
        val uri = selectedVideo ?: run {
            setStatus("Choose a source video before exporting topics.")
            return
        }
        val topics = queuedHighlightIndices.mapNotNull { index ->
            val item = highlightItems.getOrNull(index) ?: return@mapNotNull null
            val range = queuedHighlightRanges[index]
            val a = range?.first ?: item.start ?: return@mapNotNull null
            val b = range?.second ?: item.end ?: return@mapNotNull null
            item.copy(start = a, end = b)
        }.filter { item ->
            val a = item.start
            val b = item.end
            a != null && b != null && a >= 0.0 && b > a &&
                (durationMs <= 0L || b * 1000.0 <= durationMs + 250.0) &&
                b - a <= 180.0
        }
        if (topics.isEmpty()) {
            setStatus("Add at least one valid timestamped topic to the Shorts queue.")
            return
        }
        if (topics.size != queuedHighlightIndices.size) {
            setStatus("One or more queued topics have invalid timestamps or exceed the 180-second per-Short limit. Remove or correct them first.")
            return
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Export separate Shorts")
            .setMessage(
                "Export " + topics.size + " selected topic(s) as " + topics.size +
                    " separate MP4 files? Each file keeps that topic's exact start and end timestamps. " +
                    "Your current aspect ratio and text overlay will be applied to each export."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Export " + topics.size + " Shorts") { _, _ ->
                batchExportItems = topics
                batchExportIndex = 0
                batchExportUris.clear()
                batchExportActive = true
                busy = true
                refreshQueueControls()
                refreshFooter()
                startNextBatchHighlightExport()
            }
            .show()
    }

    private fun topicOutputName(item: Highlight, index: Int): String {
        val slug = item.hook.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(42)
            .ifBlank { "topic" }
        return String.format(Locale.US, "ShortsForge_%02d_%s.mp4", index + 1, slug)
    }

    private fun startNextBatchHighlightExport() {
        if (!batchExportActive) return
        if (batchExportIndex >= batchExportItems.size) {
            val completed = batchExportUris.size
            batchExportActive = false
            transformer = null
            outputFile?.delete()
            outputFile = null
            busy = false
            refreshQueueControls()
            refreshFooter()
            setStatus("Topic batch complete. " + completed + " separate Shorts saved in Movies / ShortsForge.")
            showBatchExportResultDialog(completed, batchExportItems.size)
            return
        }
        val uri = selectedVideo ?: run {
            stopBatchHighlightExport("Source video is no longer available.")
            return
        }
        val item = batchExportItems[batchExportIndex]
        val from = item.start ?: run {
            stopBatchHighlightExport("Topic timestamp is missing.")
            return
        }
        val to = item.end ?: run {
            stopBatchHighlightExport("Topic timestamp is missing.")
            return
        }
        if (to <= from || to - from > 180.0) {
            stopBatchHighlightExport("Topic " + (batchExportIndex + 1) + " has an invalid time range.")
            return
        }
        val file = File(cacheDir, "ShortsForge_batch_" + batchExportIndex + "_" + System.currentTimeMillis() + ".mp4")
        outputFile = file
        refreshQueueControls()
        setStatus(
            "Exporting topic " + (batchExportIndex + 1) + "/" + batchExportItems.size +
                ": " + item.hook
        )
        try {
            val mediaItem = MediaItem.Builder().setUri(uri).setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs((from * 1000.0).toLong())
                    .setEndPositionMs((to * 1000.0).toLong())
                    .build()
            ).build()
            val size = when {
                ::ratioSpinner.isInitialized && ratioSpinner.selectedItemPosition == 1 -> Pair(720, 720)
                ::ratioSpinner.isInitialized && ratioSpinner.selectedItemPosition == 2 -> Pair(1280, 720)
                else -> Pair(720, 1280)
            }
            val videoEffects = mutableListOf<androidx.media3.common.Effect>()
            videoEffects.add(
                Presentation.createForWidthAndHeight(
                    size.first, size.second, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
                )
            )
            val caption = if (::overlayEdit.isInitialized) overlayEdit.text.toString().trim().take(140) else ""
            if (caption.isNotBlank()) {
                val span = SpannableString(caption)
                span.setSpan(ForegroundColorSpan(Color.WHITE), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(BackgroundColorSpan(Color.argb(190, 8, 10, 18)), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(StyleSpan(Typeface.BOLD), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val settings = OverlaySettings.Builder()
                    .setOverlayFrameAnchor(0f, -0.78f)
                    .setBackgroundFrameAnchor(0f, -0.78f)
                    .build()
                videoEffects.add(OverlayEffect(listOf(TextOverlay.createStaticTextOverlay(span, settings))))
            }
            val edited = EditedMediaItem.Builder(mediaItem)
                .setEffects(Effects(emptyList(), videoEffects))
                .build()
            transformer = Transformer.Builder(this)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        runOnUiThread { onBatchHighlightExportCompleted(file, item) }
                    }
                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        runOnUiThread {
                            stopBatchHighlightExport(
                                "Export failed for topic " + (batchExportIndex + 1) +
                                    ": " + (exportException.message ?: "Media encoder error")
                            )
                        }
                    }
                }).build()
            transformer?.start(edited, file.absolutePath)
            handler.postDelayed(progressTask, 600)
            refreshFooter()
        } catch (e: Exception) {
            stopBatchHighlightExport("Could not export topic " + (batchExportIndex + 1) + ": " + (e.message ?: "unsupported video"))
        }
    }

    private fun onBatchHighlightExportCompleted(file: File, item: Highlight) {
        handler.removeCallbacks(progressTask)
        try {
            val displayName = topicOutputName(item, batchExportIndex)
            val uri = publishVideo(file, displayName)
            batchExportUris.add(uri)
            file.delete()
            transformer = null
            outputFile = null
            batchExportIndex += 1
            refreshQueueControls()
            setStatus(
                "Saved " + batchExportUris.size + "/" + batchExportItems.size +
                    " Shorts. Next: " + (batchExportItems.getOrNull(batchExportIndex)?.hook ?: "finishing")
            )
            handler.postDelayed({ startNextBatchHighlightExport() }, 250)
        } catch (e: Exception) {
            stopBatchHighlightExport("Gallery save failed after " + batchExportUris.size +
                " export(s): " + (e.message ?: "storage error"))
        }
    }

    private fun stopBatchHighlightExport(reason: String) {
        handler.removeCallbacks(progressTask)
        try { transformer?.cancel() } catch (_: Exception) { }
        outputFile?.delete()
        outputFile = null
        transformer = null
        batchExportActive = false
        busy = false
        refreshQueueControls()
        refreshFooter()
        val completed = batchExportUris.size
        setStatus(reason + if (completed > 0) " Saved " + completed + " Short(s) before the failure." else "")
        if (completed > 0) showBatchExportResultDialog(completed, batchExportItems.size)
    }

    private fun showBatchExportResultDialog(completed: Int, total: Int) {
        val message = if (completed == total) {
            completed.toString() + " separate MP4 files were saved to Movies / ShortsForge."
        } else {
            completed.toString() + " of " + total + " Shorts were saved before the batch stopped."
        }
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(if (completed == total) "All Shorts exported" else "Partial batch saved")
            .setMessage(message)
            .setNegativeButton("Done", null)
        if (batchExportUris.isNotEmpty()) {
            dialog.setPositiveButton("Share saved Shorts") { _, _ -> shareBatchExportedShorts() }
        }
        dialog.show()
    }

    private fun shareBatchExportedShorts() {
        if (batchExportUris.isEmpty()) return
        val streams = ArrayList(batchExportUris)
        val share = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "video/mp4"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, streams)
            clipData = android.content.ClipData.newUri(contentResolver, "ShortsForge Shorts", streams.first()).apply {
                streams.drop(1).forEach { addItem(android.content.ClipData.Item(it)) }
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Share exported Shorts"))
    }

    private fun exportClip() {
        val uri = selectedVideo ?: run { setStatus("Choose a video first."); return }
        if (busy) return
        val from = startEdit.text.toString().toDoubleOrNull()
        val to = endEdit.text.toString().toDoubleOrNull()
        if (from == null || to == null || !from.isFinite() || !to.isFinite() || from < 0 || to <= from) {
            setStatus("Enter a valid start and end time in seconds."); return
        }
        if (durationMs > 0 && to * 1000 > durationMs + 250) { setStatus("End time exceeds the source video duration."); return }
        if (to - from > 180) { setStatus("Choose a clip of 3 minutes or less for this mobile export."); return }
        val file = File(cacheDir, "ShortsForge_" + System.currentTimeMillis() + ".mp4")
        outputFile = file
        busy = true
        refreshFooter()
        setStatus("Preparing video export. Keep the app open…")
        try {
            val mediaItem = MediaItem.Builder().setUri(uri).setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs((from * 1000).toLong())
                    .setEndPositionMs((to * 1000).toLong()).build()
            ).build()
            val size = when (ratioSpinner.selectedItemPosition) {
                1 -> Pair(720, 720)
                2 -> Pair(1280, 720)
                else -> Pair(720, 1280)
            }
            val videoEffects = mutableListOf<androidx.media3.common.Effect>()
            videoEffects.add(Presentation.createForWidthAndHeight(size.first, size.second, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
            val caption = overlayEdit.text.toString().trim().take(140)
            if (caption.isNotBlank()) {
                val span = SpannableString(caption)
                span.setSpan(ForegroundColorSpan(Color.WHITE), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(BackgroundColorSpan(Color.argb(190, 8, 10, 18)), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(StyleSpan(Typeface.BOLD), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val settings = OverlaySettings.Builder().setOverlayFrameAnchor(0f, -0.78f).setBackgroundFrameAnchor(0f, -0.78f).build()
                videoEffects.add(OverlayEffect(listOf(TextOverlay.createStaticTextOverlay(span, settings))))
            }
            val edited = EditedMediaItem.Builder(mediaItem).setEffects(Effects(emptyList(), videoEffects)).build()
            transformer = Transformer.Builder(this)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        runOnUiThread { onExportCompleted(file) }
                    }
                    override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                        runOnUiThread {
                            finishExport()
                            setStatus("Export failed: " + (exportException.message ?: "Media encoder error"))
                        }
                    }
                }).build()
            transformer?.start(edited, file.absolutePath)
            setStatus("Exporting your Short…")
            handler.postDelayed(progressTask, 600)
        } catch (e: Exception) {
            finishExport()
            setStatus("Could not start export: " + (e.message ?: "unsupported video"))
        }
    }

    private val progressTask = object : Runnable {
        override fun run() {
            val job = transformer ?: return
            try {
                if (job.getProgress(progressHolder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                    setStatus(
                        if (batchExportActive) "Exporting topic " + (batchExportIndex + 1) + "/" + batchExportItems.size +
                            "… " + progressHolder.progress + "%"
                        else "Exporting your Short… " + progressHolder.progress + "%"
                    )
                }
            } catch (_: Exception) { }
            if (busy && transformer != null) handler.postDelayed(this, 600)
        }
    }

    private fun onExportCompleted(file: File) {
        handler.removeCallbacks(progressTask)
        try {
            val uri = publishVideo(file)
            file.delete()
            finishExport()
            setStatus("Export complete. Saved in Movies / ShortsForge.")
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(contentResolver, "ShortsForge video", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val exportedDuration = readDuration(uri).coerceAtLeast(1_000L)
            android.app.AlertDialog.Builder(this)
                .setTitle("Your Short is ready")
                .setMessage("The MP4 was saved to Movies / ShortsForge. You can continue editing it in ShortsForge Studio.")
                .setPositiveButton("Edit in Studio") { _, _ ->
                    startActivity(Intent(this, StudioEditorActivity::class.java)
                        .putExtra(StudioEditorActivity.EXTRA_SOURCE_URI, uri.toString())
                        .putExtra(StudioEditorActivity.EXTRA_SOURCE_DURATION, exportedDuration)
                        .putExtra(StudioEditorActivity.EXTRA_START_MS, 0L)
                        .putExtra(StudioEditorActivity.EXTRA_END_MS, exportedDuration))
                }
                .setNeutralButton("Share") { _, _ ->
                    startActivity(Intent.createChooser(send, "Share or upload your Short"))
                }
                .setNegativeButton("Done", null)
                .show()
        } catch (e: Exception) {
            finishExport()
            setStatus("Video exported, but Gallery save failed: " + (e.message ?: "storage error"))
        }
    }

    private fun publishVideo(file: File, displayName: String = file.name): android.net.Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/ShortsForge")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Could not create a Gallery entry.")
            try {
                contentResolver.openOutputStream(uri, "w").use { stream ->
                    if (stream == null) throw IOException("Could not open Gallery output.")
                    file.inputStream().use { input -> input.copyTo(stream) }
                }
                contentResolver.update(uri, ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }, null, null)
                return uri
            } catch (e: Exception) {
                contentResolver.delete(uri, null, null)
                throw e
            }
        }
        val legacyDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        legacyDir.mkdirs()
        val shareFile = File(legacyDir, displayName)
        file.copyTo(shareFile, overwrite = true)
        return FileProvider.getUriForFile(this, packageName + ".fileprovider", shareFile)
    }

    private fun finishExport() {
        handler.removeCallbacks(progressTask)
        transformer = null
        outputFile?.delete()
        outputFile = null
        busy = false
        runOnUiThread { refreshFooter() }
    }

    private fun cancelExport() {
        val wasBatch = batchExportActive
        val completed = batchExportUris.size
        val total = batchExportItems.size
        if (wasBatch) batchExportActive = false
        try { transformer?.cancel() } catch (_: Exception) { }
        finishExport()
        if (wasBatch) {
            refreshQueueControls()
            val message = if (completed > 0) {
                "Batch export cancelled. " + completed + " of " + total + " separate Shorts were saved."
            } else {
                "Batch export cancelled. No Shorts were saved."
            }
            setStatus(message)
            if (completed > 0) showBatchExportResultDialog(completed, total)
        } else {
            setStatus("Export cancelled.")
        }
    }

    private fun clock(seconds: Double): String {
        val ms = (seconds.coerceAtLeast(0.0) * 1000).toLong()
        return String.format(Locale.US, "%02d:%02d.%02d", ms / 60000, (ms % 60000) / 1000, (ms % 1000) / 10)
    }

    private fun fmtNumber(n: Double) = if (n % 1.0 == 0.0) n.toLong().toString() else String.format(Locale.US, "%.2f", n)

    private fun apiError(raw: String): String = try {
        JSONObject(raw).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() } ?: raw.take(220)
    } catch (_: Exception) { raw.take(220) }

    private fun readKey(id: Int): String {
        val value = prefs.getString("key_" + id, null) ?: return ""
        return try {
            val bytes = Base64.decode(value, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (_: Exception) { "" }
    }

    private fun saveKey(id: Int, key: String) {
        if (key.isBlank()) { prefs.edit().remove("key_" + id).apply(); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey())
        val bytes = cipher.iv + cipher.doFinal(key.toByteArray(Charsets.UTF_8))
        prefs.edit().putString("key_" + id, Base64.encodeToString(bytes, Base64.NO_WRAP)).apply()
    }

    private fun keystoreKey(): SecretKey {
        val store = java.security.KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        val alias = "ShortsForgeApiKeyEncryption"
        val current = store.getKey(alias, null)
        if (current is SecretKey) return current
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return generator.generateKey()
    }
}
