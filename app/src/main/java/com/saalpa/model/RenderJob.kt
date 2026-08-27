package com.saalpa.model

import android.graphics.Bitmap
import java.io.File

sealed class RenderState {
    object Idle : RenderState()
    data class Initializing(val totalFrames: Int) : RenderState()
    data class Rendering(
        val currentFrame: Int,
        val totalFrames: Int,
        val progress: Float, // 0.0f to 1.0f
        val fpsSpeed: Float,
        val previewBitmap: Bitmap? = null,
        val elapsedTimeMs: Long = 0L
    ) : RenderState()
    data class Finalizing(val totalFrames: Int) : RenderState()
    data class Completed(
        val videoFile: File,
        val durationSec: Float,
        val width: Int,
        val height: Int,
        val fps: Int,
        val fileSizeFormatted: String
    ) : RenderState()
    data class Error(val message: String) : RenderState()
}

data class RenderConfiguration(
    val templateId: String,
    val durationSec: Float = 3.0f,
    val fps: Int = 30,
    val resolution: RenderResolution = RenderResolution.HD_720P,
    val aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
    val customHtml: String? = null,
    val customCss: String? = null,
    val customJs: String? = null,
    val paramsMap: Map<String, String> = emptyMap()
) {
    val totalFrames: Int get() = (durationSec * fps).toInt().coerceAtLeast(1)

    fun getEffectiveDimensions(): Pair<Int, Int> {
        val baseDim = when (resolution) {
            RenderResolution.SD_540P -> 540
            RenderResolution.HD_720P -> 720
            RenderResolution.FHD_1080P -> 1080
        }
        return when (aspectRatio) {
            AspectRatioType.PORTRAIT_9_16 -> {
                val w = (baseDim / 2) * 2 // Ensure even number for H.264
                val h = (((baseDim * 16) / 9) / 2) * 2
                Pair(w, h)
            }
            AspectRatioType.SQUARE_1_1 -> {
                val s = (baseDim / 2) * 2
                Pair(s, s)
            }
            AspectRatioType.LANDSCAPE_16_9 -> {
                val h = (baseDim / 2) * 2
                val w = (((baseDim * 16) / 9) / 2) * 2
                Pair(w, h)
            }
            AspectRatioType.PORTRAIT_4_5 -> {
                val w = (baseDim / 2) * 2
                val h = (((baseDim * 5) / 4) / 2) * 2
                Pair(w, h)
            }
        }
    }
}
