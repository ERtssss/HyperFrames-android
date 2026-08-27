package com.example.ui.components

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
import com.example.model.AspectRatioType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.ViewportBorder
import com.example.ui.theme.ViewportDarkBg
import com.example.ui.theme.WindowDotGreen
import com.example.ui.theme.WindowDotRed
import com.example.ui.theme.WindowDotYellow

@Composable
fun WebViewPreview(
    compiledHtml: String,
    currentTimeSec: Float,
    progress: Float,
    isPlaying: Boolean,
    aspectRatio: AspectRatioType,
    modifier: Modifier = Modifier
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
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_NO_CACHE
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
            if (window.HyperFrames && window.HyperFrames.seek) {
                window.HyperFrames.seek($currentTimeSec, $progress);
            } else {
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
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .aspectRatio(ratioFloat, matchHeightConstraintsFirst = true)
                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = ElectricCyan.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(18.dp))
                .background(ViewportDarkBg)
                .border(1.5.dp, ViewportBorder, RoundedCornerShape(18.dp))
                .testTag("preview_viewport")
        ) {
            // Sleek Preview Window Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color(0xFF1E1B24))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Window Control Dots
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(WindowDotRed))
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(WindowDotYellow))
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(WindowDotGreen))
                }

                // Aspect Ratio Label
                Text(
                    text = "CANVAS · ${aspectRatio.title.split(" ").first()}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp,
                    color = androidx.compose.ui.graphics.Color(0xFFCAC4D0)
                )

                // Live Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) WindowDotGreen else WindowDotYellow)
                    )
                    Text(
                        text = if (isPlaying) "PLAYING" else "PAUSED",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPlaying) WindowDotGreen else androidx.compose.ui.graphics.Color(0xFF938F99)
                    )
                }
            }

            // Webview Display Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

