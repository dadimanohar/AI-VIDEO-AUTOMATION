package com.shortsforge.mobile

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputType
import android.util.Base64
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.SpeedProvider
import androidx.media3.common.audio.SpeedChangingAudioProcessor
import androidx.media3.common.audio.ChannelMixingAudioProcessor
import androidx.media3.common.audio.ChannelMixingMatrix
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.Presentation
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

@UnstableApi
class StudioEditorActivity : ComponentActivity() {
    private val accent = Color.rgb(103, 88, 255)
    private val backgroundColor = Color.rgb(13, 15, 22)
    private val panelColor = Color.rgb(26, 29, 40)
    private val muted = Color.rgb(158, 166, 184)
    private val border = Color.rgb(55, 60, 76)
    private val handler = Handler(Looper.getMainLooper())
    private val progressHolder = ProgressHolder()
    private val studioHttp = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .callTimeout(110, TimeUnit.SECONDS)
        .build()

    private data class ClipSegment(val id: Int, var startMs: Long, var endMs: Long, val title: String = "")

    private lateinit var sourceUri: Uri
    private var sourceDurationMs = 0L
    private var segments = mutableListOf<ClipSegment>()
    private var selectedSegmentIndex = 0
    private var nextSegmentId = 2
    private val undoStack = mutableListOf<List<ClipSegment>>()

    private var player: ExoPlayer? = null
    private var transformer: Transformer? = null
    private var outputFile: File? = null
    private var busy = false
    private var playheadTouched = false
    private var speed = 1f
    private var musicUri: Uri? = null
    private var musicName = ""
    private var muteOriginalAudio = false
    private var overlayText = ""
    private var overlayPosition = 0
    private var overlayStyle = 0
    private var overlayColor = 0
    private var filterPreset = 0
    private var brightnessValue = 0f
    private var contrastValue = 0f
    private var rotationDegrees = 0f
    private var cropToFill = true
    private var originalVolume = 1f
    private var musicVolume = 0.25f
    private var transcriptContext = ""
    private var topicContext = ""
    private var aiProvider = 0
    private var aiEditBusy = false

    private lateinit var root: LinearLayout
    private lateinit var playerView: PlayerView
    private lateinit var timelineTrack: LinearLayout
    private lateinit var timelineInfo: TextView
    private lateinit var currentTimeView: TextView
    private lateinit var statusView: TextView
    private lateinit var progressView: TextView
    private lateinit var playhead: SeekBar
    private lateinit var startField: EditText
    private lateinit var endField: EditText
    private lateinit var speedSpinner: Spinner
    private lateinit var ratioSpinner: Spinner
    private lateinit var framingSpinner: Spinner
    private lateinit var rotationSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var brightnessSeek: SeekBar
    private lateinit var contrastSeek: SeekBar
    private lateinit var brightnessValueView: TextView
    private lateinit var contrastValueView: TextView
    private lateinit var originalVolumeSeek: SeekBar
    private lateinit var musicVolumeSeek: SeekBar
    private lateinit var originalVolumeView: TextView
    private lateinit var musicVolumeValueView: TextView
    private lateinit var overlayField: EditText
    private lateinit var positionSpinner: Spinner
    private lateinit var styleSpinner: Spinner
    private lateinit var colorSpinner: Spinner
    private lateinit var muteCheck: CheckBox
    private lateinit var musicView: TextView
    private lateinit var exportButton: Button
    private lateinit var undoButton: Button
    private lateinit var deleteButton: Button
    private var playheadTicker: Runnable? = null

    private val pickMusic = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            musicUri = uri
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            musicName = displayName(uri)
            if (::musicView.isInitialized) musicView.text = "Music track: " + musicName + " (mixed under original audio)"
            setStatus("Background music added. It will loop under the original sound; adjust both volume sliders below.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val raw = intent.getStringExtra(EXTRA_SOURCE_URI)
        if (raw.isNullOrBlank()) { finish(); return }
        sourceUri = Uri.parse(raw)
        sourceDurationMs = intent.getLongExtra(EXTRA_SOURCE_DURATION, 0L)
        transcriptContext = intent.getStringExtra(EXTRA_TRANSCRIPT).orEmpty().take(24000)
        topicContext = intent.getStringExtra(EXTRA_TOPIC_CONTEXT).orEmpty().take(2000)
        aiProvider = intent.getIntExtra(EXTRA_AI_PROVIDER, 0).coerceIn(0, 3)
        val rawRanges = intent.getLongArrayExtra(EXTRA_CLIP_RANGES_MS)
        val clipTitles = intent.getStringArrayListExtra(EXTRA_CLIP_TITLES) ?: arrayListOf()
        if (rawRanges != null && rawRanges.size >= 2 && rawRanges.size % 2 == 0) {
            var id = 1
            for (i in rawRanges.indices step 2) {
                val from = rawRanges[i].coerceAtLeast(0L)
                val requestedEnd = rawRanges[i + 1]
                val to = if (sourceDurationMs > 0) requestedEnd.coerceAtMost(sourceDurationMs) else requestedEnd
                if (to - from >= 500L) {
                    segments.add(ClipSegment(id++, from, to, clipTitles.getOrNull(i / 2).orEmpty()))
                }
            }
        }
        if (segments.isEmpty()) {
            val from = intent.getLongExtra(EXTRA_START_MS, 0L).coerceAtLeast(0L)
            val suggestedEnd = intent.getLongExtra(EXTRA_END_MS, 60_000L)
            val to = if (sourceDurationMs > 0) suggestedEnd.coerceIn(from + 500L, sourceDurationMs) else maxOf(from + 500L, suggestedEnd)
            segments.add(ClipSegment(1, from, to, "Selected highlight"))
        }
        nextSegmentId = (segments.maxOfOrNull { it.id } ?: 0) + 1
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }
        setContentView(root)
        showStudio()
    }

    override fun onPause() {
        player?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        playheadTicker?.let { handler.removeCallbacks(it) }
        handler.removeCallbacks(progressTask)
        try { transformer?.cancel() } catch (_: Exception) { }
        player?.release()
        super.onDestroy()
    }

    private fun showStudio() {
        root.removeAllViews()
        root.addView(buildToolbar())
        val scroll = ScrollView(this).apply { clipToPadding = false }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(18))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        body.addView(sectionLabel("PREVIEW"))
        player = ExoPlayer.Builder(this).build()
        playerView = PlayerView(this).apply {
            player = this@StudioEditorActivity.player
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setBackgroundColor(Color.BLACK)
        }
        body.addView(playerView, LinearLayout.LayoutParams(-1, dp(300)).apply {
            bottomMargin = dp(8)
        })
        player?.setMediaItem(MediaItem.fromUri(sourceUri))
        player?.prepare()
        player?.seekTo(segments.first().startMs)
        body.addView(buildPlaybackControls(), full(dp(4)))

        body.addView(sectionLabel("TIMELINE  ·  CLIPS"))
        timelineInfo = labelText("")
        body.addView(timelineInfo)
        val timelineScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.rgb(19, 22, 31))
            setPadding(dp(7), dp(8), dp(7), dp(8))
        }
        timelineTrack = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        timelineScroll.addView(timelineTrack)
        body.addView(timelineScroll, full(dp(5)))
        currentTimeView = labelText("")
        body.addView(currentTimeView)
        playhead = SeekBar(this).apply {
            max = 1000
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val seg = selectedSegment() ?: return
                    if (fromUser) {
                        val position = seg.startMs + ((seg.endMs - seg.startMs) * progress / 1000L)
                        player?.seekTo(position)
                        currentTimeView.text = clock(position) + " / " + clock(seg.endMs)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) { playheadTouched = true }
                override fun onStopTrackingTouch(seekBar: SeekBar?) { playheadTouched = false }
            })
        }
        body.addView(playhead, full())
        body.addView(buildClipActions(), full(dp(7)))

        body.addView(sectionLabel("EDIT TOOLS"))
        body.addView(buildTrimCard(), full(dp(7)))
        body.addView(buildSpeedCanvasCard(), full(dp(8)))
        body.addView(buildVideoLookCard(), full(dp(8)))
        body.addView(buildTextCard(), full(dp(8)))
        body.addView(buildAudioCard(), full(dp(8)))

        statusView = labelText("Ready to edit. Changes are applied when you export.")
        body.addView(statusView.apply { setPadding(dp(4), dp(12), dp(4), dp(8)) })
        renderTimeline()
        refreshFields()
        startPlayheadTicker()

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(8), dp(14), dp(10))
            setBackgroundColor(Color.rgb(22, 24, 33))
            elevation = dp(12).toFloat()
        }
        progressView = labelText("Edits are previewed here and rendered when you export.")
        footer.addView(progressView, full())
        exportButton = actionButton("Export edited video", true) { exportEditedTimeline() }
        footer.addView(exportButton, full(dp(5)))
        root.addView(footer)
    }

    private fun buildToolbar(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(9), dp(12), dp(9))
            setBackgroundColor(Color.rgb(21, 23, 32))
        }
        bar.addView(actionButton("‹ Back", false) { finish() }, LinearLayout.LayoutParams(dp(82), dp(44)))
        val title = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        title.addView(labelText("SHORTSFORGE STUDIO", Color.WHITE, 14f, true))
        title.addView(labelText("TIMELINE EDITOR", muted, 10f, true))
        bar.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(actionButton("Done", true) { exportEditedTimeline() }, LinearLayout.LayoutParams(dp(86), dp(44)))
        return bar
    }

    private fun buildPlaybackControls(): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val play = actionButton("▶  Play", true) {
            val p = player ?: return@actionButton
            val seg = selectedSegment() ?: return@actionButton
            if (p.isPlaying) {
                p.pause()
            } else {
                if (p.currentPosition < seg.startMs || p.currentPosition >= seg.endMs) p.seekTo(seg.startMs)
                p.play()
            }
        }
        row.addView(play, LinearLayout.LayoutParams(0, dp(44), 1f))
        row.addView(actionButton("↺ Start", false) {
            selectedSegment()?.let { player?.seekTo(it.startMs) }
        }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { leftMargin = dp(7) })
        row.addView(actionButton("End ↦", false) {
            selectedSegment()?.let { player?.seekTo(it.endMs) }
        }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { leftMargin = dp(7) })
        return row
    }

    private fun buildClipActions(): View {
        val panel = panel()
        panel.addView(labelText("Arrange your clips. Split at the playhead, remove a segment, undo, or add another range."))
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(actionButton("✂ Split", true) { splitAtPlayhead() }, weight())
        row1.addView(actionButton("＋ Add clip", false) { addClipDialog() }, weight(dp(5)))
        row1.addView(actionButton("Duplicate", false) { duplicateSelectedSegment() }, weight(dp(5)))
        row1.addView(actionButton("Delete", false) { deleteSelectedSegment() }, weight(dp(5)))
        panel.addView(row1, full(dp(7)))
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        undoButton = actionButton("↶ Undo", false) { undo() }
        deleteButton = actionButton("← Move", false) { moveSelected(-1) }
        row2.addView(undoButton, weight())
        row2.addView(deleteButton, weight(dp(6)))
        row2.addView(actionButton("Move →", false) { moveSelected(1) }, weight(dp(6)))
        panel.addView(row2, full(dp(6)))
        return panel
    }

    private fun buildTrimCard(): View {
        val panel = panel()
        panel.addView(labelText("TRIM SELECTED CLIP"))
        val fields = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        startField = editField("Start (seconds)")
        endField = editField("End (seconds)")
        fields.addView(startField, LinearLayout.LayoutParams(0, dp(48), 1f).apply { rightMargin = dp(5) })
        fields.addView(endField, LinearLayout.LayoutParams(0, dp(48), 1f).apply { leftMargin = dp(5) })
        panel.addView(fields, full(dp(6)))
        panel.addView(actionButton("Apply trim to selected clip", false) { applyTrim() }, full(dp(6)))
        return panel
    }

    private fun buildSpeedCanvasCard(): View {
        val panel = panel()
        panel.addView(labelText("PLAYBACK SPEED"))
        speedSpinner = darkSpinner(listOf("0.5× Slow", "0.75×", "1.0× Normal", "1.25×", "1.5×", "2.0× Fast"))
        speedSpinner.setSelection(2)
        speedSpinner.onItemSelectedListener = simpleSelection { position ->
            speed = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)[position]
            player?.playbackParameters = PlaybackParameters(speed)
            setStatus("Preview speed set to " + speed + "×. Export will use this speed.")
        }
        panel.addView(speedSpinner, full(dp(5)))
        panel.addView(labelText("CANVAS / ASPECT RATIO").apply { setPadding(0, dp(12), 0, dp(5)) })
        ratioSpinner = darkSpinner(listOf("Vertical 9:16 · Shorts", "Square 1:1", "Landscape 16:9"))
        panel.addView(ratioSpinner, full())
        panel.addView(labelText("FRAME").apply { setPadding(0, dp(11), 0, dp(5)) })
        framingSpinner = darkSpinner(listOf("Fill frame · crop edges", "Fit whole video · keep edges"))
        framingSpinner.setSelection(0)
        framingSpinner.onItemSelectedListener = simpleSelection { cropToFill = it == 0 }
        panel.addView(framingSpinner, full())
        panel.addView(labelText("ROTATE VIDEO").apply { setPadding(0, dp(11), 0, dp(5)) })
        rotationSpinner = darkSpinner(listOf("0° · Original", "90° clockwise", "180°", "270° clockwise"))
        rotationSpinner.onItemSelectedListener = simpleSelection { rotationDegrees = listOf(0f, 90f, 180f, 270f)[it] }
        panel.addView(rotationSpinner, full())
        panel.addView(labelText("Changes are rendered when you export. Use Preview to check the source framing before export.").apply { setPadding(0, dp(7), 0, 0) })
        return panel
    }

    private fun buildVideoLookCard(): View {
        val panel = panel()
        panel.addView(labelText("VIDEO LOOK"))
        filterSpinner = darkSpinner(listOf("Natural · no filter", "Black & white", "Negative", "Bright", "Cinematic contrast"))
        filterSpinner.onItemSelectedListener = simpleSelection { filterPreset = it }
        panel.addView(filterSpinner, full(dp(5)))
        brightnessValueView = labelText("Brightness  ·  0")
        panel.addView(brightnessValueView, full(dp(7)))
        brightnessSeek = SeekBar(this).apply {
            max = 200
            progress = 100
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnSeekBarChangeListener(simpleSeek { value ->
                brightnessValue = ((value - 100) / 200f).coerceIn(-0.5f, 0.5f)
                brightnessValueView.text = "Brightness  ·  " + String.format(Locale.US, "%.2f", brightnessValue)
            })
        }
        panel.addView(brightnessSeek, full())
        contrastValueView = labelText("Contrast  ·  0")
        panel.addView(contrastValueView, full(dp(7)))
        contrastSeek = SeekBar(this).apply {
            max = 200
            progress = 100
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnSeekBarChangeListener(simpleSeek { value ->
                contrastValue = ((value - 100) / 200f).coerceIn(-0.5f, 0.5f)
                contrastValueView.text = "Contrast  ·  " + String.format(Locale.US, "%.2f", contrastValue)
            })
        }
        panel.addView(contrastSeek, full())
        panel.addView(actionButton("Reset video look", false) {
            filterPreset = 0
            brightnessValue = 0f
            contrastValue = 0f
            rotationDegrees = 0f
            cropToFill = true
            filterSpinner.setSelection(0)
            brightnessSeek.progress = 100
            contrastSeek.progress = 100
            rotationSpinner.setSelection(0)
            framingSpinner.setSelection(0)
            brightnessValueView.text = "Brightness  ·  0"
            contrastValueView.text = "Contrast  ·  0"
            setStatus("Video look reset to the original picture.")
        }, full(dp(8)))
        panel.addView(labelText("Filters and picture adjustments are applied to the exported video.").apply { setPadding(0, dp(6), 0, 0) })
        return panel
    }

    private fun buildTextCard(): View {
        val panel = panel()
        panel.addView(labelText("TEXT OVERLAY"))
        overlayField = editField("Type title, caption, or callout")
        overlayField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        overlayField.setSingleLine(false)
        overlayField.minLines = 2
        overlayField.addTextChangedListener(simpleWatcher { overlayText = it.take(140) })
        panel.addView(overlayField, full(dp(5)))
        panel.addView(actionButton("AI · Generate 3 hook / title ideas", true) { generateAiEditIdeas() }, full(dp(6)))
        panel.addView(actionButton("Clear text overlay", false) {
            overlayText = ""
            overlayField.setText("")
            setStatus("Text overlay cleared.")
        }, full(dp(5)))
        panel.addView(labelText("AI ideas use the selected topic transcript and your saved cloud AI key. Choose an idea to add it as an editable text overlay.").apply { setPadding(0, dp(5), 0, dp(4) })
        panel.addView(labelText("POSITION").apply { setPadding(0, dp(10), 0, dp(5)) })
        positionSpinner = darkSpinner(listOf("Top", "Centre", "Bottom"))
        positionSpinner.setSelection(2)
        positionSpinner.onItemSelectedListener = simpleSelection { overlayPosition = it }
        panel.addView(positionSpinner, full())
        panel.addView(labelText("TEXT STYLE").apply { setPadding(0, dp(10), 0, dp(5)) })
        styleSpinner = darkSpinner(listOf("Clean", "Bold with dark background", "Large headline"))
        styleSpinner.onItemSelectedListener = simpleSelection { overlayStyle = it }
        panel.addView(styleSpinner, full())
        panel.addView(labelText("TEXT COLOUR").apply { setPadding(0, dp(10), 0, dp(5)) })
        colorSpinner = darkSpinner(listOf("White", "Yellow", "Cyan"))
        colorSpinner.onItemSelectedListener = simpleSelection { overlayColor = it }
        panel.addView(colorSpinner, full())
        return panel
    }

    private fun buildAudioCard(): View {
        val panel = panel()
        panel.addView(labelText("AUDIO TRACK"))
        muteCheck = CheckBox(this).apply {
            text = "Mute original video audio"
            setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnCheckedChangeListener { _, checked ->
                muteOriginalAudio = checked
                setStatus(if (checked) "Original video audio will be removed on export." else "Original video audio will be kept unless a music track replaces it.")
            }
        }
        panel.addView(muteCheck, full())
        musicView = labelText("No background music selected.")
        panel.addView(musicView, full(dp(4)))
        originalVolumeView = labelText("Original video sound  ·  100%")
        panel.addView(originalVolumeView, full(dp(6)))
        originalVolumeSeek = SeekBar(this).apply {
            max = 100
            progress = 100
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnSeekBarChangeListener(simpleSeek { value ->
                originalVolume = value / 100f
                originalVolumeView.text = "Original video sound  ·  " + value + "%"
                if (value == 0) {
                    muteOriginalAudio = true
                    if (::muteCheck.isInitialized) muteCheck.isChecked = true
                } else if (muteOriginalAudio) {
                    muteOriginalAudio = false
                    if (::muteCheck.isInitialized) muteCheck.isChecked = false
                }
            })
        }
        panel.addView(originalVolumeSeek, full())
        musicVolumeValueView = labelText("Background music  ·  25%")
        panel.addView(musicVolumeValueView, full(dp(6)))
        musicVolumeSeek = SeekBar(this).apply {
            max = 100
            progress = 25
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnSeekBarChangeListener(simpleSeek { value ->
                musicVolume = value / 100f
                musicVolumeValueView.text = "Background music  ·  " + value + "%"
            })
        }
        panel.addView(musicVolumeSeek, full())
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(actionButton("＋ Add music", true) { pickMusic.launch(arrayOf("audio/*")) }, weight())
        row.addView(actionButton("Remove music", false) {
            musicUri = null
            musicName = ""
            musicVolume = 0.25f
            if (::musicVolumeSeek.isInitialized) musicVolumeSeek.progress = 25
            musicView.text = "No background music selected."
            setStatus("Background music removed.")
        }, weight(dp(7)))
        panel.addView(row, full(dp(7)))
        panel.addView(labelText("Background music is mixed underneath the original video sound. Lower music volume to keep speech clear, or mute the original sound to use music only."))
        return panel
    }

    private fun renderTimeline() {
        if (!::timelineTrack.isInitialized) return
        timelineTrack.removeAllViews()
        segments.forEachIndexed { index, segment ->
            val topicLabel = segment.title.take(22).ifBlank { "Selected range" }
            val chip = actionButton(
                "CLIP " + (index + 1) + "  ·  " + topicLabel + "\n" + clock(segment.startMs) + " — " + clock(segment.endMs),
                index == selectedSegmentIndex
            ) {
                selectedSegmentIndex = index
                player?.pause()
                player?.seekTo(segment.startMs)
                refreshFields()
                renderTimeline()
                setStatus("Clip " + (index + 1) + " selected.")
            }
            chip.textSize = 11f
            chip.minHeight = dp(58)
            timelineTrack.addView(chip, LinearLayout.LayoutParams(dp(174), dp(62)).apply {
                rightMargin = dp(6)
            })
        }
        timelineInfo.text = segments.size.toString() + " clip segment(s)  ·  " + clock(totalDurationMs()) + " total"
        if (::undoButton.isInitialized) undoButton.isEnabled = undoStack.isNotEmpty()
        if (::deleteButton.isInitialized) deleteButton.isEnabled = segments.size > 1
    }

    private fun refreshFields() {
        val seg = selectedSegment() ?: return
        if (::startField.isInitialized) {
            startField.setText(fmtSeconds(seg.startMs))
            endField.setText(fmtSeconds(seg.endMs))
        }
        if (::currentTimeView.isInitialized) currentTimeView.text = clock(seg.startMs) + " / " + clock(seg.endMs)
        if (::playhead.isInitialized) playhead.progress = 0
    }

    private fun selectedSegment(): ClipSegment? = segments.getOrNull(selectedSegmentIndex)

    private fun saveUndo() {
        undoStack.add(segments.map { it.copy() })
        if (undoStack.size > 30) undoStack.removeAt(0)
        if (::undoButton.isInitialized) undoButton.isEnabled = true
    }

    private fun splitAtPlayhead() {
        val seg = selectedSegment() ?: return
        val point = player?.currentPosition ?: seg.startMs
        if (point <= seg.startMs + 500L || point >= seg.endMs - 500L) {
            setStatus("Move the playhead inside the selected clip before splitting.")
            return
        }
        saveUndo()
        val left = ClipSegment(seg.id, seg.startMs, point, seg.title)
        val right = ClipSegment(nextSegmentId++, point, seg.endMs, if (seg.title.isBlank()) "" else seg.title + " · part 2")
        segments[selectedSegmentIndex] = left
        segments.add(selectedSegmentIndex + 1, right)
        selectedSegmentIndex += 1
        renderTimeline()
        refreshFields()
        setStatus("Clip split at " + clock(point) + ". The two pieces can be moved or deleted separately.")
    }

    private fun addClipDialog() {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(18), dp(8), dp(18), dp(4))
        }
        val from = editField("Start seconds")
        val to = editField("End seconds")
        val p = player?.currentPosition ?: 0L
        from.setText(fmtSeconds(p))
        to.setText(fmtSeconds(minOf(p + 15_000L, if (sourceDurationMs > 0) sourceDurationMs else p + 15_000L)))
        wrap.addView(from, LinearLayout.LayoutParams(0, dp(48), 1f).apply { rightMargin = dp(5) })
        wrap.addView(to, LinearLayout.LayoutParams(0, dp(48), 1f).apply { leftMargin = dp(5) })
        android.app.AlertDialog.Builder(this)
            .setTitle("Add a source-video segment")
            .setMessage("Enter the source video's start and end time in seconds.")
            .setView(wrap)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add clip") { _, _ ->
                val a = from.text.toString().toDoubleOrNull()
                val b = to.text.toString().toDoubleOrNull()
                if (a == null || b == null || !a.isFinite() || !b.isFinite() || a < 0 || b <= a ||
                    (sourceDurationMs > 0 && b * 1000 > sourceDurationMs)) {
                    setStatus("Invalid clip range. Check the source duration and try again.")
                } else {
                    saveUndo()
                    segments.add(ClipSegment(nextSegmentId++, (a * 1000).toLong(), (b * 1000).toLong(), "Added clip"))
                    selectedSegmentIndex = segments.lastIndex
                    player?.pause()
                    player?.seekTo(segments.last().startMs)
                    renderTimeline()
                    refreshFields()
                    setStatus("A new clip segment was added to the timeline.")
                }
            }.show()
    }

    private fun duplicateSelectedSegment() {
        val source = selectedSegment() ?: return
        saveUndo()
        val copy = ClipSegment(
            nextSegmentId++,
            source.startMs,
            source.endMs,
            (source.title.ifBlank { "Clip" }) + " · copy"
        )
        segments.add(selectedSegmentIndex + 1, copy)
        selectedSegmentIndex += 1
        player?.pause()
        player?.seekTo(copy.startMs)
        renderTimeline()
        refreshFields()
        setStatus("Clip duplicated. The copy can be trimmed, moved or deleted independently.")
    }

    private fun deleteSelectedSegment() {
        if (segments.size <= 1) {
            setStatus("The timeline must contain at least one clip.")
            return
        }
        saveUndo()
        val removed = segments.removeAt(selectedSegmentIndex)
        selectedSegmentIndex = selectedSegmentIndex.coerceAtMost(segments.lastIndex)
        player?.pause()
        player?.seekTo(selectedSegment()?.startMs ?: 0L)
        renderTimeline()
        refreshFields()
        setStatus("Removed clip segment " + removed.id + ". Use Undo to restore it.")
    }

    private fun moveSelected(direction: Int) {
        val target = selectedSegmentIndex + direction
        if (target !in segments.indices) return
        saveUndo()
        val item = segments.removeAt(selectedSegmentIndex)
        segments.add(target, item)
        selectedSegmentIndex = target
        renderTimeline()
        refreshFields()
        player?.pause()
        player?.seekTo(item.startMs)
        setStatus("Clip order changed. Export will follow the timeline order.")
    }

    private fun undo() {
        if (undoStack.isEmpty()) return
        segments = undoStack.removeAt(undoStack.lastIndex).map { it.copy() }.toMutableList()
        selectedSegmentIndex = selectedSegmentIndex.coerceIn(0, segments.lastIndex)
        renderTimeline()
        refreshFields()
        selectedSegment()?.let { player?.seekTo(it.startMs) }
        if (::undoButton.isInitialized) undoButton.isEnabled = undoStack.isNotEmpty()
        setStatus("Last timeline change undone.")
    }

    private fun applyTrim() {
        val seg = selectedSegment() ?: return
        val start = startField.text.toString().toDoubleOrNull()
        val end = endField.text.toString().toDoubleOrNull()
        if (start == null || end == null || !start.isFinite() || !end.isFinite() ||
            start < 0 || end * 1000 <= start * 1000 + 500 ||
            (sourceDurationMs > 0 && end * 1000 > sourceDurationMs)) {
            setStatus("Invalid trim range. End must be after Start and inside the source video.")
            return
        }
        saveUndo()
        seg.startMs = (start * 1000).toLong()
        seg.endMs = (end * 1000).toLong()
        player?.pause()
        player?.seekTo(seg.startMs)
        renderTimeline()
        refreshFields()
        setStatus("Trim applied to selected clip.")
    }

    private fun startPlayheadTicker() {
        val task = object : Runnable {
            override fun run() {
                val p = player
                val seg = selectedSegment()
                if (p != null && seg != null) {
                    if (p.isPlaying && p.currentPosition >= seg.endMs) {
                        p.pause()
                        p.seekTo(seg.endMs)
                    }
                    if (!playheadTouched && ::playhead.isInitialized) {
                        val fraction = ((p.currentPosition - seg.startMs).toFloat() /
                            (seg.endMs - seg.startMs).coerceAtLeast(1L)).coerceIn(0f, 1f)
                        playhead.progress = (fraction * 1000).toInt()
                        if (::currentTimeView.isInitialized) currentTimeView.text = clock(p.currentPosition) + " / " + clock(seg.endMs)
                    }
                }
                handler.postDelayed(this, 180)
            }
        }
        playheadTicker = task
        handler.post(task)
    }

    private fun previewSelection() {
        val seg = selectedSegment() ?: return
        player?.apply {
            if (currentPosition < seg.startMs || currentPosition >= seg.endMs) seekTo(seg.startMs)
            playWhenReady = true
            play()
        }
    }

    private fun exportEditedTimeline() {
        if (busy) return
        if (segments.isEmpty()) {
            setStatus("Add at least one clip segment.")
            return
        }
        val total = totalDurationMs()
        if (total <= 0 || total > 180_000L) {
            setStatus("Keep the combined timeline between 0 and 180 seconds for this mobile export.")
            return
        }
        val file = File(cacheDir, "ShortsForge_Studio_" + System.currentTimeMillis() + ".mp4")
        outputFile = file
        busy = true
        exportButton.isEnabled = false
        setStatus("Preparing the edited timeline…")
        try {
            val size = when (ratioSpinner.selectedItemPosition) {
                1 -> Pair(720, 720)
                2 -> Pair(1280, 720)
                else -> Pair(720, 1280)
            }
            val speedProvider = object : SpeedProvider {
                override fun getSpeed(timeUs: Long): Float = speed
                override fun getNextSpeedChangeTimeUs(timeUs: Long): Long = C.TIME_UNSET
            }
            val editedItems = segments.map { seg ->
                val clipped = MediaItem.Builder().setUri(sourceUri).setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(seg.startMs)
                        .setEndPositionMs(seg.endMs)
                        .build()
                ).build()
                val effects = mutableListOf<androidx.media3.common.Effect>()
                if (rotationDegrees != 0f) {
                    effects.add(ScaleAndRotateTransformation.Builder().setRotationDegrees(rotationDegrees).build())
                }
                when (filterPreset) {
                    1 -> effects.add(RgbFilter.createGrayscaleFilter())
                    2 -> effects.add(RgbFilter.createInvertedFilter())
                    3 -> effects.add(Brightness(0.12f))
                    4 -> effects.add(Contrast(0.22f))
                }
                if (brightnessValue != 0f) effects.add(Brightness(brightnessValue))
                if (contrastValue != 0f) effects.add(Contrast(contrastValue))
                effects.add(Presentation.createForWidthAndHeight(
                    size.first,
                    size.second,
                    if (cropToFill) Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP else Presentation.LAYOUT_SCALE_TO_FIT
                ))
                if (speed != 1f) effects.add(SpeedChangeEffect(speedProvider))
                buildOverlayEffect()?.let { overlay -> effects.add(overlay) }
                val audioProcessors = mutableListOf<androidx.media3.common.audio.AudioProcessor>()
                if (speed != 1f) audioProcessors.add(SpeedChangingAudioProcessor(speedProvider))
                if (!muteOriginalAudio && originalVolume < 0.999f) audioProcessors.add(createVolumeProcessor(originalVolume))
                EditedMediaItem.Builder(clipped)
                    .setEffects(Effects(audioProcessors, effects))
                    .setRemoveAudio(muteOriginalAudio || originalVolume <= 0.001f)
                    .setFrameRate(30)
                    .build()
            }
            val videoSequence = EditedMediaItemSequence.Builder(editedItems).build()
            val composition = if (musicUri != null) {
                val music = EditedMediaItem.Builder(MediaItem.fromUri(musicUri!!))
                    .setRemoveVideo(true)
                    .setEffects(Effects(listOf(createVolumeProcessor(musicVolume)), emptyList()))
                    .build()
                val musicSequence = EditedMediaItemSequence.Builder(listOf(music))
                    .setIsLooping(true)
                    .build()
                Composition.Builder(videoSequence, musicSequence).build()
            } else {
                Composition.Builder(videoSequence).build()
            }
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
                            setStatus("Export failed: " + (exportException.message ?: "Media3 could not render this edit."))
                        }
                    }
                }).build()
            transformer?.start(composition, file.absolutePath)
            setStatus("Rendering your clips, picture adjustments, editable text and mixed audio…")
            handler.postDelayed(progressTask, 500)
        } catch (e: Exception) {
            finishExport()
            setStatus("Could not start export: " + (e.message ?: "unknown editor error"))
        }
    }

    private fun createVolumeProcessor(gain: Float): ChannelMixingAudioProcessor {
        val processor = ChannelMixingAudioProcessor()
        for (channels in 1..6) {
            val outputChannels = if (channels == 1) 1 else 2
            val matrix = ChannelMixingMatrix.createForConstantPower(channels, outputChannels).scaleBy(gain.coerceIn(0f, 1f))
            processor.putChannelMixingMatrix(matrix)
        }
        return processor
    }

    private fun buildOverlayEffect(): OverlayEffect? {
        val caption = overlayText.trim().take(140)
        if (caption.isBlank()) return null
        val span = SpannableString(caption)
        val textColor = when (overlayColor) {
            1 -> Color.rgb(255, 224, 72)
            2 -> Color.rgb(76, 225, 255)
            else -> Color.WHITE
        }
        span.setSpan(ForegroundColorSpan(textColor), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (overlayStyle == 1) {
            span.setSpan(BackgroundColorSpan(Color.argb(205, 7, 9, 15)), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(StyleSpan(Typeface.BOLD), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else if (overlayStyle == 2) {
            span.setSpan(StyleSpan(Typeface.BOLD), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(RelativeSizeSpan(1.35f), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val y = when (overlayPosition) {
            0 -> 0.78f
            1 -> 0f
            else -> -0.78f
        }
        val settings = OverlaySettings.Builder()
            .setOverlayFrameAnchor(0f, y)
            .setBackgroundFrameAnchor(0f, y)
            .build()
        return OverlayEffect(listOf(TextOverlay.createStaticTextOverlay(span, settings)))
    }

    private val progressTask = object : Runnable {
        override fun run() {
            val job = transformer ?: return
            try {
                if (job.getProgress(progressHolder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                    progressView.text = "Exporting edited video… " + progressHolder.progress + "%"
                }
            } catch (_: Exception) { }
            if (busy && transformer != null) handler.postDelayed(this, 500)
        }
    }

    private fun onExportCompleted(file: File) {
        handler.removeCallbacks(progressTask)
        try {
            val uri = publishVideo(file)
            file.delete()
            finishExport()
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(contentResolver, "ShortsForge Studio video", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            android.app.AlertDialog.Builder(this)
                .setTitle("Your edit is ready")
                .setMessage("The MP4 was saved to Movies/ShortsForge.")
                .setPositiveButton("Edit this video again") { _, _ ->
                    startActivity(Intent(this, StudioEditorActivity::class.java)
                        .putExtra(EXTRA_SOURCE_URI, uri.toString())
                        .putExtra(EXTRA_SOURCE_DURATION, readDuration(uri))
                        .putExtra(EXTRA_START_MS, 0L)
                        .putExtra(EXTRA_END_MS, readDuration(uri).coerceAtLeast(1_000L)))
                    finish()
                }
                .setNeutralButton("Share") { _, _ ->
                    startActivity(Intent.createChooser(share, "Share your edited Short"))
                }
                .setNegativeButton("Done") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            finishExport()
            setStatus("Export completed but Gallery save failed: " + (e.message ?: "storage error"))
        }
    }

    private fun publishVideo(file: File): Uri {
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

    private fun finishExport() {
        handler.removeCallbacks(progressTask)
        transformer = null
        outputFile?.delete()
        outputFile = null
        busy = false
        if (::exportButton.isInitialized) exportButton.isEnabled = true
    }

    private fun totalDurationMs(): Long = segments.sumOf { (it.endMs - it.startMs).coerceAtLeast(0L) }

    private fun displayName(uri: Uri): String {
        var name = "Selected audio"
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) name = it.getString(0) ?: name
        }
        return name
    }

    private fun readDuration(uri: Uri): Long {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(this, uri)
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) { 0L } finally { try { retriever.release() } catch (_: Exception) { } }
    }

    private fun setStatus(message: String) {
        if (::statusView.isInitialized) statusView.text = message
        if (::progressView.isInitialized && !busy) progressView.text = message
    }

    private fun sectionLabel(value: String) = labelText(value, Color.rgb(177, 181, 205), 10f, true).apply {
        setPadding(dp(3), dp(13), dp(3), dp(6))
    }

    private fun labelText(value: String, color: Int = muted, size: Float = 12f, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(10), dp(12), dp(11))
        background = rounded(panelColor, 16, border)
    }

    private fun editField(hintText: String) = EditText(this).apply {
        hint = hintText
        setTextColor(Color.WHITE)
        setHintTextColor(Color.rgb(128, 136, 155))
        textSize = 14f
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        setPadding(dp(10), dp(7), dp(10), dp(7))
        background = rounded(Color.rgb(35, 39, 52), 10, border)
    }

    private fun darkSpinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@StudioEditorActivity, android.R.layout.simple_spinner_dropdown_item, items)
        setPadding(dp(8), dp(4), dp(8), dp(4))
        background = rounded(Color.rgb(35, 39, 52), 10, border)
    }

    private fun actionButton(title: String, primary: Boolean, click: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 12f
        setTextColor(if (primary) Color.WHITE else Color.rgb(225, 228, 240))
        background = rounded(if (primary) accent else Color.rgb(38, 42, 56), 11, if (primary) accent else border)
        minHeight = dp(42)
        setPadding(dp(8), dp(5), dp(8), dp(5))
        setOnClickListener { click() }
    }

    private fun rounded(fill: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun full(top: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { if (top > 0) topMargin = top }
    private fun weight(left: Int = 0) = LinearLayout.LayoutParams(0, dp(43), 1f).apply { if (left > 0) leftMargin = left }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun fmtSeconds(ms: Long) = String.format(Locale.US, "%.2f", ms / 1000.0)
    private fun clock(ms: Long): String {
        val seconds = (ms.coerceAtLeast(0L) / 1000L)
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
    }

    private fun simpleSeek(onChanged: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) onChanged(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun simpleSelection(onSelected: (Int) -> Unit) = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { onSelected(position) }
    }

    private fun simpleWatcher(onChanged: (String) -> Unit) = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onChanged(s?.toString() ?: "") }
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    companion object {
        const val EXTRA_SOURCE_URI = "shortsforge.sourceUri"
        const val EXTRA_SOURCE_DURATION = "shortsforge.sourceDurationMs"
        const val EXTRA_START_MS = "shortsforge.startMs"
        const val EXTRA_END_MS = "shortsforge.endMs"
        const val EXTRA_CLIP_RANGES_MS = "shortsforge.clipRangesMs"
        const val EXTRA_CLIP_TITLES = "shortsforge.clipTitles"
        const val EXTRA_TRANSCRIPT = "shortsforge.transcriptContext"
        const val EXTRA_TOPIC_CONTEXT = "shortsforge.topicContext"
        const val EXTRA_AI_PROVIDER = "shortsforge.aiProvider"
    }
}
