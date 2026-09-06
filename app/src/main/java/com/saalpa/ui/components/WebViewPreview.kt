package com.saalpa.ui.components

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.saalpa.engine.HyperFramesJsBridge
import com.saalpa.model.AspectRatioType
import com.saalpa.ui.theme.ElectricCyan
import com.saalpa.ui.theme.StudioCardBorder
import com.saalpa.ui.theme.ViewportBorder
import com.saalpa.ui.theme.ViewportDarkBg
import com.saalpa.ui.theme.WindowDotGreen
import com.saalpa.ui.theme.WindowDotRed
import com.saalpa.ui.theme.WindowDotYellow

@Composable
fun WebViewPreview(
    compiledHtml: String,
    currentTimeSec: Float,
    progress: Float,
    isPlaying: Boolean,
    aspectRatio: AspectRatioType,
    modifier: Modifier = Modifier,
    jsBridge: HyperFramesJsBridge? = null
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            if (jsBridge != null) {
                addJavascriptInterface(jsBridge, "AndroidHyperFrames")
                addJavascriptInterface(jsBridge, "AndroidBridge")
            }
            webViewClient = WebViewClient()
        }
    }

    // Load HTML when template/custom code updates
    LaunchedEffect(compiledHtml) {
        webView.loadDataWithBaseURL("https://hyperframes.local", compiledHtml, "text/html", "UTF-8", null)
    }

    // Sync timeline position smoothly with JavaScript
    LaunchedEffect(currentTimeSec, progress, isPlaying) {
        val jsCode = """
            if (window.__hfSeek) {
                window.__hfSeek($currentTimeSec, $progress);
            } else {
                if (window.__timelines) {
                    for (var k in window.__timelines) {
                        if (window.__timelines[k] && typeof window.__timelines[k].seek === 'function') {
                            window.__timelines[k].seek($currentTimeSec, false);
                        }
                    }
                }
                if (typeof window.seekTo === 'function') window.seekTo($currentTimeSec);
                if (window.gsap && window.gsap.globalTimeline) window.gsap.globalTimeline.seek($currentTimeSec, false);
                if (window.HyperFrames && window.HyperFrames.seek) window.HyperFrames.seek($currentTimeSec, $progress);
                document.documentElement.style.setProperty('--time', '${currentTimeSec}s');
                document.documentElement.style.setProperty('--progress', '$progress');
            }
        """.trimIndent()
        webView.evaluateJavascript(jsCode, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    val ratioFloat = when (aspectRatio) {
        AspectRatioType.PORTRAIT_9_16 -> 9f / 16f
        AspectRatioType.SQUARE_1_1 -> 1f / 1f
        AspectRatioType.LANDSCAPE_16_9 -> 16f / 9f
        AspectRatioType.PORTRAIT_4_5 -> 4f / 5f
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxSize()
                .testTag("preview_viewport")
        )
    }
}

