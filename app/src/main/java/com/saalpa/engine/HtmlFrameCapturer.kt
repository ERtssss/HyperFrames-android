package com.saalpa.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Headless frame capture engine that loads HTML/CSS/JS in an offscreen WebView
 * and renders exact deterministic frames at specified timestamp/progress.
 */
class HtmlFrameCapturer(private val context: Context) {
    private val TAG = "HtmlFrameCapturer"
    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    suspend fun setup(htmlContent: String, width: Int, height: Int) = withContext(Dispatchers.Main) {
        val loadDeferred = CompletableDeferred<Boolean>()

        val wv = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(width, height)
            setBackgroundColor(Color.BLACK)
            setInitialScale(100)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                loadWithOverviewMode = false
                useWideViewPort = true
                textZoom = 100
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (!loadDeferred.isCompleted) {
                        loadDeferred.complete(true)
                    }
                }
            }
        }

        // Measure and layout offscreen
        wv.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        wv.layout(0, 0, width, height)

        wv.loadDataWithBaseURL("https://hyperframes.local", htmlContent, "text/html", "UTF-8", null)
        webView = wv

        // Wait for page ready
        loadDeferred.await()
        // Stabilization delay for CDN scripts (GSAP), fonts, and DOM layout initialization
        delay(350)
    }

    suspend fun captureFrame(
        timeSec: Float,
        progress: Float,
        width: Int,
        height: Int,
        reusableBitmap: Bitmap? = null
    ): Bitmap = withContext(Dispatchers.Main) {
        val wv = webView ?: throw IllegalStateException("WebView not initialized")
        val jsDeferred = CompletableDeferred<Unit>()

        val jsCode = """
            if (window.__hfSeek) {
                window.__hfSeek($timeSec, $progress);
            } else {
                if (window.__timelines) {
                    for (var k in window.__timelines) {
                        if (window.__timelines[k] && typeof window.__timelines[k].seek === 'function') {
                            window.__timelines[k].seek($timeSec, false);
                        }
                    }
                }
                if (typeof window.seekTo === 'function') window.seekTo($timeSec);
                if (window.gsap && window.gsap.globalTimeline) window.gsap.globalTimeline.seek($timeSec, false);
                if (window.HyperFrames && window.HyperFrames.seek) window.HyperFrames.seek($timeSec, $progress);
                document.documentElement.style.setProperty('--time', '${timeSec}s');
                document.documentElement.style.setProperty('--progress', '$progress');
            }
        """.trimIndent()

        wv.evaluateJavascript(jsCode) {
            jsDeferred.complete(Unit)
        }
        jsDeferred.await()

        // Wait for DOM & GSAP repaint
        delay(12)

        val bitmap = reusableBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)
        wv.draw(canvas)

        bitmap
    }

    suspend fun destroy() = withContext(Dispatchers.Main) {
        try {
            webView?.stopLoading()
            webView?.clearCache(true)
            webView?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying WebView", e)
        }
        webView = null
    }
}
