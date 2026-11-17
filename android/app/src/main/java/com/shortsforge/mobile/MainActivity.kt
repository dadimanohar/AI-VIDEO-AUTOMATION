package com.shortsforge.mobile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Effects
import androidx.media3.effect.Presentation
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var videoUri: Uri? = null
    private lateinit var status: TextView
    private lateinit var videoLabel: TextView
    private lateinit var transcript: EditText
    private lateinit var apiKey: EditText
    private lateinit var provider: Spinner
    private lateinit var startSec: EditText
    private lateinit var endSec: EditText
    private lateinit var caption: EditText
    private lateinit var result: TextView
    private val client = OkHttpClient.Builder().callTimeout(120, TimeUnit.SECONDS).build()

    private val pickVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            videoUri = uri
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            videoLabel.text = "Selected: " + displayName(uri)
            status.text = "Video ready. Enter clip timestamps or analyze a transcript."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("shortsforge", MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
            setBackgroundColor(0xFFF7F8FA.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        setContentView(scroll)

        content.addView(TextView(this).apply {
            text = "ShortsForge"
            textSize = 30f; setTextColor(0xFF101828.toInt())
        })
        content.addView(TextView(this).apply {
            text = "MOBILE VIDEO → YOUTUBE SHORTS"
            textSize = 12f; setTextColor(0xFF667085.toInt())
        })
        status = TextView(this).apply { text = "Import a video to begin."; textSize = 14f; setPadding(0, 18, 0, 18) }
        content.addView(status)
        videoLabel = TextView(this).apply { text = "No video selected"; textSize = 14f }
        content.addView(videoLabel)
        content.addView(button("Choose video") { pickVideo.launch(arrayOf("video/*")) })

        content.addView(section("AI highlight analysis"))
        provider = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Groq", "OpenAI", "Anthropic Claude", "Offline/manual"))
            setSelection(prefs.getInt("provider", 0))
        }
        content.addView(provider)
        apiKey = edit("API key (stored only on this device)", prefs.getString("api_key", "") ?: "")
        apiKey.inputType = 129
        content.addView(apiKey)
        content.addView(button("Save API settings") {
            prefs.edit().putString("api_key", apiKey.text.toString()).putInt("provider", provider.selectedItemPosition).apply()
            status.text = "Settings saved on this device."
        })
        content.addView(TextView(this).apply { text = "Paste a transcript to ask the selected AI to find engaging moments. Cloud providers require your own API key."; textSize = 13f; setPadding(0,12,0,6) })
        transcript = edit("Paste transcript here…")
        transcript.minLines = 4
        content.addView(transcript)
        content.addView(button("Analyze transcript") { analyzeTranscript() })
        result = TextView(this).apply { textSize = 13f; setTextColor(0xFF344054.toInt()); setPadding(0,10,0,16) }
        content.addView(result)

        content.addView(section("Create a vertical Short"))
        startSec = edit("Start time in seconds", "0")
        endSec = edit("End time in seconds", "30")
        content.addView(startSec); content.addView(endSec)
        caption = edit("Caption overlay (optional)")
        content.addView(caption)
        content.addView(TextView(this).apply { text = "Export uses 720 × 1280 (9:16) output. Choose a short segment; longer exports take more time and storage."; textSize = 12f; setPadding(0,8,0,8) })
        content.addView(button("Export 9:16 MP4") { exportClip() })
        content.addView(TextView(this).apply {
            text = "Local mode: manual timestamp selection is available without an API key. Local Whisper/GGUF inference is not bundled in this first Android build."
            textSize = 12f; setTextColor(0xFF667085.toInt()); setPadding(0,20,0,20)
        })
    }

    private fun section(value: String) = TextView(this).apply {
        text = value; textSize = 19f; setTextColor(0xFF101828.toInt()); setPadding(0,24,0,8)
    }

    private fun edit(hintText: String, value: String = "") = EditText(this).apply {
        hint = hintText; setText(value); textSize = 14f
        setPadding(12, 10, 12, 10)
        setSingleLine(false)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; setOnClickListener { action() }
    }

    private fun displayName(uri: Uri): String {
        var name = "video"
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) name = it.getString(0) ?: name
        }
        return name
    }

    private fun analyzeTranscript() {
        val text = transcript.text.toString().trim()
        if (text.isEmpty()) { status.text = "Paste a transcript first."; return }
        val key = apiKey.text.toString().trim()
        val mode = provider.selectedItemPosition
        if (mode == 3) {
            result.text = "Offline/manual mode selected. Use the transcript and timestamps to choose a moment. Semantic local-model support is not bundled yet."
            return
        }
        if (key.isEmpty()) { status.text = "Enter and save your provider API key first."; return }
        status.text = "Analyzing transcript…"
        Thread {
            try {
                val prompt = "Find up to 8 engaging short-video moments in this transcript. Return JSON array only. Each item must have start_seconds, end_seconds (estimate only if timestamps exist), hook, reason. Never invent exact timestamps if the transcript has no timestamps. Transcript:\n" + text
                val output = when (mode) {
                    0 -> chatCompletion("https://api.groq.com/openai/v1/chat/completions", key, "llama-3.3-70b-versatile", prompt, false)
                    1 -> chatCompletion("https://api.openai.com/v1/chat/completions", key, "gpt-4o-mini", prompt, false)
                    else -> chatCompletion("https://api.anthropic.com/v1/messages", key, "claude-3-5-haiku-latest", prompt, true)
                }
                runOnUiThread { result.text = output; status.text = "Analysis complete. Verify suggested timestamps against the video before exporting." }
            } catch (e: Exception) {
                runOnUiThread { status.text = "AI request failed: " + (e.message ?: "unknown error") }
            }
        }.start()
    }

    private fun chatCompletion(url: String, key: String, model: String, prompt: String, anthropic: Boolean): String {
        val body = if (anthropic) JSONObject()
            .put("model", model).put("max_tokens", 1800)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        else JSONObject().put("model", model).put("temperature", 0.2)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        val builder = Request.Builder().url(url).post(body.toString().toRequestBody("application/json".toMediaType()))
        if (anthropic) builder.addHeader("x-api-key", key).addHeader("anthropic-version", "2023-06-01")
        else builder.addHeader("Authorization", "Bearer $key")
        val response = client.newCall(builder.build()).execute()
        val raw = response.body?.string() ?: ""
        if (!response.isSuccessful) throw IllegalStateException("HTTP " + response.code + ": " + raw.take(500))
        val json = JSONObject(raw)
        return if (anthropic) json.getJSONArray("content").getJSONObject(0).optString("text")
        else json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content")
    }

    private fun exportClip() {
        val uri = videoUri
        if (uri == null) { status.text = "Choose a video first."; return }
        val start = startSec.text.toString().toLongOrNull()
        val end = endSec.text.toString().toLongOrNull()
        if (start == null || end == null || start < 0 || end <= start) { status.text = "Enter valid start/end seconds."; return }
        val outputDir = getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES) ?: filesDir
        val output = File(outputDir, "ShortsForge_" + System.currentTimeMillis() + ".mp4")
        status.text = "Exporting clip… Keep the app open until export completes."
        try {
            val item = MediaItem.Builder().setUri(uri).setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(start * 1000)
                    .setEndPositionMs(end * 1000)
                    .build()
            ).build()
            val presentation = Presentation.createForWidthAndHeight(720, 1280, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP)
            val edited = EditedMediaItem.Builder(item)
                .setEffects(Effects(emptyList(), listOf(presentation)))
                .build()
            val transformer = Transformer.Builder(this)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                        runOnUiThread {
                            status.text = "Export complete: " + output.name
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"; putExtra(Intent.EXTRA_STREAM, Uri.fromFile(output))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(Intent.createChooser(share, "Save or share your Short"))
                        }
                    }
                    override fun onError(composition: androidx.media3.transformer.Composition, exportResult: ExportResult, exportException: ExportException) {
                        runOnUiThread { status.text = "Export failed: " + exportException.message }
                    }
                }).build()
            transformer.start(edited, output.absolutePath)
        } catch (e: Exception) { status.text = "Could not start export: " + e.message }
    }
}
