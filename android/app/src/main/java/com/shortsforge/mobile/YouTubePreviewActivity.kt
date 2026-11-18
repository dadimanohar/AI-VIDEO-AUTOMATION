package com.shortsforge.mobile

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.Locale

class YouTubePreviewActivity : ComponentActivity() {
    private val backgroundColor = Color.rgb(15, 17, 25)
    private val panelColor = Color.rgb(27, 30, 42)
    private val accent = Color.rgb(112, 94, 255)
    private val muted = Color.rgb(169, 177, 195)

    private var currentSeconds = 0.0
    private var durationSeconds = 0.0
    private var title = "YouTube video preview"

    private lateinit var webView: WebView
    private lateinit var titleView: TextView
    private lateinit var durationView: TextView
    private lateinit var statusView: TextView
    private lateinit var startField: EditText
    private lateinit var endField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
        if (videoId.isNullOrBlank() || !VIDEO_ID.matches(videoId)) {
            Toast.makeText(this, "Invalid YouTube video URL.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        buildScreen(videoId)
    }

    @Suppress("SetJavaScriptEnabled")
    private fun buildScreen(videoId: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }
        setContentView(root)

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(12))
            setBackgroundColor(Color.rgb(22, 24, 34))
        }
        toolbar.addView(text("SHORTSFORGE  ·  YOUTUBE PREVIEW", Color.WHITE, 14f, true))
        toolbar.addView(text("Preview the video and mark clip timestamps", muted, 11f))
        root.addView(toolbar)

        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(16))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        titleView = text(title, Color.WHITE, 15f, true)
        body.addView(titleView)
        durationView = text("Loading YouTube player…", muted, 12f)
        body.addView(durationView.apply { setPadding(0, dp(5), 0, dp(10)) })

        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptCanOpenWindowsAutomatically = false
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            addJavascriptInterface(PlayerBridge(), "ShortsForge")
        }
        body.addView(webView, LinearLayout.LayoutParams(-1, dp(230)).apply {
            bottomMargin = dp(12)
        })
        webView.loadDataWithBaseURL(
            "https://shortsforge.mobile",
            playerHtml(videoId),
            "text/html",
            "UTF-8",
            null
        )

        val markerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(13))
            background = rounded(panelColor, 15)
        }
        markerPanel.addView(text("CLIP MARKERS", Color.WHITE, 12f, true))
        markerPanel.addView(text("Play or seek in the embedded YouTube player, then set the In and Out markers.", muted, 11f)
            .apply { setPadding(0, dp(4), 0, dp(10)) })

        val markerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        startField = secondsField("Start seconds", "0")
        endField = secondsField("End seconds", "60")
        markerRow.addView(startField, LinearLayout.LayoutParams(0, dp(48), 1f).apply { rightMargin = dp(6) })
        markerRow.addView(endField, LinearLayout.LayoutParams(0, dp(48), 1f).apply { leftMargin = dp(6) })
        markerPanel.addView(markerRow)

        val inOut = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        inOut.addView(actionButton("Set IN at playhead", false) {
            startField.setText(format(currentSeconds))
            val end = endField.text.toString().toDoubleOrNull() ?: 0.0
            if (end <= currentSeconds) endField.setText(format(minOf(currentSeconds + 60.0, durationSeconds.takeIf { it > 0 } ?: currentSeconds + 60.0)))
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { rightMargin = dp(5); topMargin = dp(8) })
        inOut.addView(actionButton("Set OUT at playhead", false) {
            endField.setText(format(currentSeconds))
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { leftMargin = dp(5); topMargin = dp(8) })
        markerPanel.addView(inOut)
        body.addView(markerPanel)

        statusView = text(
            "Preview and timestamp markers only. The YouTube watch URL does not provide an editable MP4 file.",
            muted, 12f
        ).apply { setPadding(dp(4), dp(12), dp(4), dp(10)) }
        body.addView(statusView)

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(12))
            setBackgroundColor(Color.rgb(22, 24, 34))
        }
        footer.addView(actionButton("Use these clip markers", true) { returnMarkers() }, full())
        footer.addView(actionButton("Back to ShortsForge", false) { finish() }, full(dp(7)))
        root.addView(footer)
    }

    private fun playerHtml(videoId: String): String = """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
          <style>
            html,body,#player { margin:0; padding:0; width:100%; height:100%; background:#000; overflow:hidden; }
            iframe { width:100%; height:100%; border:0; }
          </style>
        </head>
        <body>
          <div id="player"></div>
          <script>
            var player;
            function onYouTubeIframeAPIReady() {
              player = new YT.Player('player', {
                width:'100%', height:'100%', videoId:'$videoId',
                playerVars:{ playsinline:1, controls:1, rel:0, fs:1, origin:window.location.origin },
                events:{
                  onReady:function(e) {
                    try {
                      ShortsForge.onPlayerInfo(e.target.getVideoData().title || 'YouTube video', e.target.getDuration());
                    } catch(err) {}
                    window.setInterval(function() {
                      try {
                        if (player && player.getCurrentTime) {
                          ShortsForge.onPlayerTime(player.getCurrentTime(), player.getDuration());
                        }
                      } catch(err) {}
                    }, 300);
                  },
                  onStateChange:function(e) {
                    try { ShortsForge.onPlayerTime(player.getCurrentTime(), player.getDuration()); } catch(err) {}
                  },
                  onError:function(e) {
                    try { ShortsForge.onPlayerError(e.data); } catch(err) {}
                  }
                }
              });
            }
          </script>
          <script src="https://www.youtube.com/iframe_api"></script>
        </body>
        </html>
    """.trimIndent()

    private inner class PlayerBridge {
        @JavascriptInterface
        fun onPlayerInfo(videoTitle: String, duration: Double) {
            runOnUiThread {
                title = videoTitle.take(180)
                durationSeconds = duration.coerceAtLeast(0.0)
                titleView.text = title
                durationView.text = if (durationSeconds > 0) "Duration  " + clock(durationSeconds) else "Video information loaded"
                val defaultEnd = minOf(60.0, durationSeconds.takeIf { it > 0 } ?: 60.0)
                if ((endField.text.toString().toDoubleOrNull() ?: 0.0) <= 0.0) endField.setText(format(defaultEnd))
            }
        }

        @JavascriptInterface
        fun onPlayerTime(current: Double, duration: Double) {
            runOnUiThread {
                currentSeconds = current.coerceAtLeast(0.0)
                if (duration > 0.0) durationSeconds = duration
                statusView.text = "Playhead  " + clock(currentSeconds) +
                    if (durationSeconds > 0) "  /  " + clock(durationSeconds) else ""
            }
        }

        @JavascriptInterface
        fun onPlayerError(code: Int) {
            runOnUiThread {
                statusView.text = "YouTube player reported error $code. The video may be private, age-restricted, unavailable, or disallow embedding."
            }
        }
    }

    private fun returnMarkers() {
        val start = startField.text.toString().toDoubleOrNull()
        val end = endField.text.toString().toDoubleOrNull()
        if (start == null || end == null || !start.isFinite() || !end.isFinite() ||
            start < 0 || end <= start || (durationSeconds > 0 && end > durationSeconds)) {
            Toast.makeText(this, "Enter a valid start and end time within the video duration.", Toast.LENGTH_LONG).show()
            return
        }
        setResult(Activity.RESULT_OK, intent.apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_START_SECONDS, start)
            putExtra(EXTRA_END_SECONDS, end)
            putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
        })
        finish()
    }

    private fun secondsField(hintText: String, value: String) = EditText(this).apply {
        hint = hintText
        setText(value)
        setSingleLine(true)
        textSize = 13f
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        setTextColor(Color.WHITE)
        setHintTextColor(muted)
        setPadding(dp(10), dp(8), dp(10), dp(8))
        background = rounded(Color.rgb(40, 44, 59), 9)
    }

    private fun actionButton(label: String, primary: Boolean, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        background = rounded(if (primary) accent else Color.rgb(43, 47, 63), 11)
        minHeight = dp(43)
        setPadding(dp(8), dp(5), dp(8), dp(5))
        setOnClickListener { action() }
    }

    private fun text(value: String, color: Int, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun rounded(color: Int, radius: Int) = android.graphics.drawable.GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun full(top: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { if (top > 0) topMargin = top }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun format(value: Double) = String.format(Locale.US, "%.2f", value)
    private fun clock(value: Double): String {
        val safe = value.coerceAtLeast(0.0).toLong()
        return String.format(Locale.US, "%02d:%02d", safe / 60, safe % 60)
    }

    companion object {
        const val EXTRA_VIDEO_ID = "shortsforge.youtube.videoId"
        const val EXTRA_TITLE = "shortsforge.youtube.title"
        const val EXTRA_START_SECONDS = "shortsforge.youtube.startSeconds"
        const val EXTRA_END_SECONDS = "shortsforge.youtube.endSeconds"
        const val EXTRA_DURATION_SECONDS = "shortsforge.youtube.durationSeconds"
        private val VIDEO_ID = Regex("^[A-Za-z0-9_-]{11}$")
    }
}
