package com.saalpa.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface

/**
 * JavaScriptInterface bridge allowing bidirectional communication between
 * Web-based GSAP timelines (HyperFrames engine) and the Android Kotlin ViewModel.
 */
class HyperFramesJsBridge(
    private val onTimelineReadyListener: ((durationSec: Float, totalScenes: Int, sceneDataJson: String) -> Unit)? = null,
    private val onTickListener: ((currentTimeSec: Float, progress: Float, isPlaying: Boolean) -> Unit)? = null,
    private val onSceneChangeListener: ((sceneIndex: Int, sceneId: String, sceneTitle: String) -> Unit)? = null,
    private val onMediaStatusListener: ((mediaId: String, status: String, currentSec: Float) -> Unit)? = null,
    private val onLogListener: ((level: String, message: String) -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val TAG = "HyperFramesJsBridge"

    @JavascriptInterface
    fun onTimelineReady(durationSec: Float, totalScenes: Int, sceneDataJson: String) {
        Log.d(TAG, "onTimelineReady: duration=${durationSec}s, scenes=$totalScenes")
        mainHandler.post {
            onTimelineReadyListener?.invoke(durationSec, totalScenes, sceneDataJson)
        }
    }

    @JavascriptInterface
    fun onTimelineTick(currentTimeSec: Float, progress: Float, isPlaying: Boolean) {
        mainHandler.post {
            onTickListener?.invoke(currentTimeSec, progress, isPlaying)
        }
    }

    @JavascriptInterface
    fun onSceneChange(sceneIndex: Int, sceneId: String, sceneTitle: String) {
        Log.d(TAG, "onSceneChange: index=$sceneIndex, id=$sceneId, title=$sceneTitle")
        mainHandler.post {
            onSceneChangeListener?.invoke(sceneIndex, sceneId, sceneTitle)
        }
    }

    @JavascriptInterface
    fun onMediaStatus(mediaId: String, status: String, currentSec: Float) {
        mainHandler.post {
            onMediaStatusListener?.invoke(mediaId, status, currentSec)
        }
    }

    @JavascriptInterface
    fun log(level: String, message: String) {
        Log.d(TAG, "JS-LOG [$level]: $message")
        mainHandler.post {
            onLogListener?.invoke(level, message)
        }
    }

    @JavascriptInterface
    fun postMessage(type: String, payload: String) {
        Log.d(TAG, "postMessage: $type -> $payload")
        mainHandler.post {
            onLogListener?.invoke(type, payload)
        }
    }
}
