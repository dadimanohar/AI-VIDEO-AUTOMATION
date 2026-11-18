package com.shortsforge.mobile

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
import android.media.MediaMuxer
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
    private val qwenModelName = "Qwen3-0.6B-Q4_0.gguf"
    private val whisperModelName = "ggml-base.bin"
    private val handler = Handler(Looper.getMainLooper())
    private val progressHolder = ProgressHolder()

    private var selectedVideo: android.net.Uri? = null
    private var videoName = ""
    private var durationMs = 0L
    private var transcript = ""
    private var highlightItems: List<Highlight> = emptyList()
    private var selectedHighlightIndex = -1
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
    private lateinit var statusView: TextView
    private lateinit var footerStatus: TextView
    private lateinit var exportButton: Button
    private lateinit var cancelButton: Button
    private lateinit var highlightsView: LinearLayout

    private val pickTranscript = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importTranscriptFile(uri)
    }

    private val pickVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedVideo = uri
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            videoName = displayName(uri)
            durationMs = readDuration(uri)
            startText = "0"
            endText = if (durationMs > 0) fmtNumber(minOf(30_000L, durationMs) / 1000.0) else "30"
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
            setStatus("Video ready. Review the preview or find highlights.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider = prefs.getInt("provider", 0).coerceIn(0, 3)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(page) }
        setContentView(root)
        showEditor()
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

    private fun showEditor() {
        root.removeAllViews()
        root.addView(toolbar("ShortsForge", "MOBILE SHORTS STUDIO", "Settings") { showSettings() })
        val scroll = ScrollView(this).apply { clipToPadding = false }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(8), dp(18), dp(24)) }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        body.addView(text("Turn footage into a Short.", 25f, ink, true).apply { setPadding(0, dp(12), 0, dp(4)) })
        body.addView(text("Choose a source, find a moment, then edit and export.", 14f, muted))

        val source = card(body, "01  •  SOURCE VIDEO", "Choose a video file from your device to start editing.")
        source.addView(button("Choose video", true) { pickVideo.launch(arrayOf("video/*")) }, full())
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

        val ai = card(body, "02  •  FIND THE BEST MOMENT", "Get timestamped highlights from the video's speech.")
        ai.addView(button("Auto-transcribe + find clips", true) { transcribeAndAnalyze() }, full())
        ai.addView(button("Analyze pasted transcript", false) { analyzeTranscript() }, full(dp(8)))
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
        highlightsView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        ai.addView(highlightsView)

        val edit = card(body, "03  •  EDIT YOUR SHORT", "Select the highlight length, preview the exact clip, then export.")
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
        edit.addView(text("AI looks for complete question-and-answer exchanges, key claims, explanations and surprising facts. Choosing a suggestion builds a 60s or 90s window around that moment.", 12f, muted).apply { setPadding(0, dp(7), 0, dp(2)) })
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
            retriever.setDataSource(this, uri)
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
            transcript = parsed
            transcriptEdit.setText(parsed)
            setStatus("Transcript imported. Choose 60s or 90s, then analyse the important moments.")
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
        setStatus("Preparing audio from the selected video…")
        executor.execute {
            var audio: File? = null
            try {
                audio = extractAudio(uri)
                if (audio.length() > 24L * 1024L * 1024L) throw IOException("Audio exceeds the provider upload limit. Use a shorter source or paste a transcript.")
                runOnUiThread { setStatus("Transcribing speech and creating timestamps…") }
                val result = transcribe(audio, key, provider)
                if (result.isBlank()) throw IOException("No speech was detected. You can paste a transcript manually.")
                runOnUiThread {
                    transcript = result
                    transcriptEdit.setText(result)
                    setStatus("Transcript ready. Analysing the best moments…")
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
                // Eight-line overlap keeps a question at one chunk's end connected to its answer in the next.
                val chunks = lines.windowed(size = 36, step = 28, partialWindows = true)
                val chunkTotal = chunks.size
                val candidates = mutableListOf<Highlight>()
                for ((index, chunk) in chunks.withIndex()) {
                    val chunkText = chunk.joinToString("\n")
                    val prompt = """
Find the most important short-form video moments in this timestamped interview transcript.
Target exported video length: $clipDurationSeconds seconds.
Return ONLY a JSON array with at most 3 anchor moments:
[{"start_seconds":120.5,"end_seconds":164.2,"hook":"Short accurate title","reason":"Why this matters","category":"Question and answer","importance_score":9}]
Find complete question-and-answer exchanges, important announcements, key insights, explanations, surprising facts, predictions and practical advice.
Include the question when it is needed to understand the answer. Never cut away the context that makes an answer meaningful.
The anchor start/end must come from the transcript timestamps. Convert mm:ss.cc to seconds from the beginning of the original video. Do not invent or guess times. Do not include partial thoughts.
Rank importance_score from 1 to 10. No markdown or explanatory text outside the JSON array.
Transcript segment $index of $chunkTotal:
$chunkText
""".trimIndent()
                    val answer = runBlocking {
                        Llama.complete(
                            model,
                            prompt = prompt,
                            systemPrompt = "You are a careful factual interview editor. Preserve context. Return strict JSON only.",
                            maxTokens = 420
                        )
                    }
                    candidates.addAll(parseHighlights(answer.text))
                    val progress = ((index + 1) * 100 / chunkTotal).coerceIn(1, 100)
                    setStatus("Offline Qwen3 is analysing transcript chunks… " + progress + "%")
                }
                val best = candidates
                    .distinctBy { (it.start?.toInt() ?: -1).toString() + "-" + (it.end?.toInt() ?: -1).toString() }
                    .sortedByDescending { it.importance }
                    .take(8)
                runOnUiThread {
                    showHighlights(best)
                    setStatus(if (best.isEmpty()) {
                        "Qwen3 did not return reliable timestamped highlights. Try a longer transcript or Cloud AI for a stronger analysis."
                    } else {
                        "Offline Qwen3 found " + best.size + " important moments. Tap one to create a " + clipDurationSeconds + "s clip."
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
            throw IOException("Could not prepare local Whisper audio: " + (e.message ?: "audio decoder error"), e)
        } finally {
            try { if (decoderStarted) decoder?.stop() } catch (_: Exception) { }
            try { decoder?.release() } catch (_: Exception) { }
            try { extractor.release() } catch (_: Exception) { }
            rawPcm.delete()
        }
    }

    private fun extractAudio(uri: android.net.Uri): File {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var started = false
        val file = File(cacheDir, "sf-audio-" + System.currentTimeMillis() + ".m4a")
        try {
            extractor.setDataSource(this, uri, null)
            var track = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                if ((f.getString(MediaFormat.KEY_MIME) ?: "").startsWith("audio/")) { track = i; format = f; break }
            }
            if (track < 0 || format == null) throw IOException("The selected video has no audio track.")
            extractor.selectTrack(track)
            muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val dest = muxer.addTrack(format)
            muxer.start()
            started = true
            val declaredBufferSize = try { format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) } catch (_: Exception) { 0 }
            val bufferSize = maxOf(1_048_576, declaredBufferSize)
            val buffer = java.nio.ByteBuffer.allocateDirect(bufferSize)
            val info = MediaCodec.BufferInfo()
            while (true) {
                buffer.clear()
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                info.offset = 0
                info.size = size
                info.presentationTimeUs = extractor.sampleTime
                info.flags = extractor.sampleFlags
                muxer.writeSampleData(dest, buffer, info)
                if (!extractor.advance()) break
            }
        } catch (e: Exception) {
            file.delete()
            throw IOException("Could not read the video's audio: " + (e.message ?: "unsupported format"), e)
        } finally {
            try { if (started) muxer?.stop() } catch (_: Exception) { }
            try { muxer?.release() } catch (_: Exception) { }
            try { extractor.release() } catch (_: Exception) { }
        }
        if (!file.exists() || file.length() == 0L) throw IOException("No audio samples were extracted.")
        return file
    }

    private fun transcribe(file: File, key: String, providerId: Int): String {
        val url = if (providerId == 0) "https://api.groq.com/openai/v1/audio/transcriptions" else "https://api.openai.com/v1/audio/transcriptions"
        val model = if (providerId == 0) "whisper-large-v3-turbo" else "whisper-1"
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("model", model)
            .addFormDataPart("response_format", "verbose_json")
            .addFormDataPart("timestamp_granularities[]", "segment")
            .addFormDataPart("file", file.name, file.asRequestBody("audio/mp4".toMediaType()))
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
        if (provider == 3) {
            analyzeWithLocalModel(data, auto)
            return
        }
        val key = readKey(provider)
        if (key.isBlank()) { setStatus("Add an API key in Settings."); showSettings(); return }
        if (busy && !auto) return
        busy = true
        refreshFooter()
        setStatus("AI is searching for clear hooks and complete clip moments…")
        executor.execute {
            var handedOffToLocal = false
            try {
                val prompt = """
You are a professional interview, podcast and long-form video editor. Find the most valuable moments in this transcript.
The user's target output clip length is """ + clipDurationSeconds + """ seconds.
Return ONLY a valid JSON array containing up to 8 strongest anchor moments, ranked by importance.
Exact shape:
[{"start_seconds":120.5,"end_seconds":164.2,"hook":"A clear short title","reason":"Why this matters and what the viewer learns","category":"Question and answer","importance_score":9}]
Ranking priorities:
1. Complete question + answer exchanges. Include the question when it gives the answer necessary context; never start halfway through an answer.
2. Important new facts, concrete claims, announcements, explanations, predictions, practical advice and useful examples.
3. Surprising, controversial or highly informative statements that make sense without misleading editing.
4. A clear beginning and ending. Prefer a complete thought over a dramatic but incomplete sentence.
Timestamp rules:
- Use only timestamp boundaries supported by the transcript. They are seconds from the beginning of the source video.
- Do not invent timestamps. Return [] if no reliable timestamps exist.
- start_seconds and end_seconds mark the full core exchange / key point, not the final export window.
- The app will expand around this anchor to a $clipDurationSeconds-second export window, bounded by the source duration.
- Do not repeat the same event and do not overlap anchor moments.
- importance_score must be an integer from 1 to 10.
- category should be one of: "Question and answer", "Important announcement", "Key insight", "Explanation", "Prediction", "Surprising fact", "Practical advice".
- Return numeric seconds, not time strings. No markdown fences or text outside the JSON.
Transcript:
$data
""".trimIndent()
                val raw = chat(prompt, key, provider)
                val items = parseHighlights(raw)
                runOnUiThread {
                    showHighlights(items)
                    setStatus(if (items.isEmpty()) "No reliable timestamped clips were returned. Check the transcript timestamps." else "Found " + items.size + " clip suggestions. Tap one to set its range.")
                }
            } catch (e: Exception) {
                val useLocal = isConnectivityFailure(e) && try { qwenModelFile().let { it.exists() && it.length() >= 420_000_000L } } catch (_: Exception) { false }
                if (useLocal) handedOffToLocal = true
                runOnUiThread {
                    if (useLocal) {
                        provider = 3
                        prefs.edit().putInt("provider", 3).apply()
                        setStatus("Cloud connection unavailable. Switching highlight analysis to local Qwen3…")
                        analyzeWithLocalModel(data, true)
                    } else {
                        setStatus("AI analysis failed: " + (e.message ?: "unknown error"))
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
            val body = JSONObject().put("model", "claude-haiku-4-5-20251001").put("max_tokens", 2200)
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

    private fun parseHighlights(raw: String): List<Highlight> {
        val first = raw.indexOf('[')
        val last = raw.lastIndexOf(']')
        if (first < 0 || last <= first) return emptyList()
        val arr = JSONArray(raw.substring(first, last + 1))
        val out = mutableListOf<Highlight>()
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val a = item.optDouble("start_seconds", Double.NaN)
            val b = item.optDouble("end_seconds", Double.NaN)
            val start = a.takeIf { it.isFinite() && it >= 0 }
            val end = b.takeIf { it.isFinite() && it > (start ?: -1.0) }
            if (start != null && end != null && durationMs > 0 && end > durationMs / 1000.0 + 1.0) continue
            out.add(
                Highlight(
                    start, end,
                    item.optString("hook", "Suggested moment"),
                    item.optString("reason", ""),
                    item.optString("category", "Key insight"),
                    item.optInt("importance_score", 0).coerceIn(0, 10)
                )
            )
        }
        return out.take(8)
    }

    private fun showHighlights(items: List<Highlight>) {
        if (!::highlightsView.isInitialized) return
        highlightItems = items
        if (selectedHighlightIndex !in items.indices) selectedHighlightIndex = if (items.isNotEmpty()) 0 else -1
        highlightsView.removeAllViews()
        if (items.isEmpty()) {
            highlightsView.addView(text("No timestamped suggestions to show.", 12f, muted).apply { setPadding(0, dp(8), 0, 0) })
            return
        }
        items.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(11), dp(12), dp(11))
                background = shape(if (index == selectedHighlightIndex) Color.rgb(238, 236, 255) else Color.rgb(248, 249, 255), 12, if (index == selectedHighlightIndex) accent else border)
                setOnClickListener {
                    if (item.start != null && item.end != null) {
                        selectedHighlightIndex = index
                        applyHighlightWindow(item.start, item.end)
                        showHighlights(highlightItems)
                        setStatus("Suggestion " + (index + 1) + " selected. Preview the clip or export.")
                    } else setStatus("This suggestion has no exact timestamps. Use a timestamped transcript.")
                }
            }
            row.addView(text((index + 1).toString() + (if (index == selectedHighlightIndex) "  ✓ SELECTED" else "") + ". " + item.hook, 14f, ink, true))
            val meta = item.category + if (item.importance > 0) "  ·  Importance " + item.importance + "/10" else ""
            row.addView(text(meta, 11f, muted, true).apply { setPadding(0, dp(3), 0, 0) })
            row.addView(text(if (item.start != null && item.end != null) "Key moment: " + clock(item.start) + "  →  " + clock(item.end) + "  ·  Tap for " + clipDurationSeconds + "s clip" else "Timestamp unavailable", 12f, accent, true).apply { setPadding(0, dp(4), 0, 0) })
            if (item.reason.isNotBlank()) row.addView(text(item.reason, 12f, muted).apply { setPadding(0, dp(4), 0, 0) })
            highlightsView.addView(row, full(dp(8)))
        }
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
                    setStatus("Exporting your Short… " + progressHolder.progress + "%")
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
            startActivity(Intent.createChooser(send, "Share or upload your Short"))
        } catch (e: Exception) {
            finishExport()
            setStatus("Video exported, but Gallery save failed: " + (e.message ?: "storage error"))
        }
    }

    private fun publishVideo(file: File): android.net.Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
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
        val shareFile = File(legacyDir, file.name)
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
        try { transformer?.cancel() } catch (_: Exception) { }
        finishExport()
        setStatus("Export cancelled.")
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
