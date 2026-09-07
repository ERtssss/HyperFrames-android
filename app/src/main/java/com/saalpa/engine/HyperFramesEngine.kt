package com.saalpa.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.saalpa.model.AspectRatioType
import com.saalpa.model.RenderConfiguration
import com.saalpa.model.RenderState
import com.saalpa.model.SavedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostic Profiling Data Class for Render Pipeline Optimization
 */
data class RenderProfile(
    var htmlGenTimeMs: Long = 0L,
    var webViewPrepTimeMs: Long = 0L,
    var totalCaptureTimeMs: Long = 0L,
    var totalEncodingTimeMs: Long = 0L,
    var totalUiUpdateTimeMs: Long = 0L,
    var audioPrepTimeMs: Long = 0L,
    var slowestFrameMs: Long = 0L,
    var totalFrames: Int = 0,
    var totalTimeMs: Long = 0L
) {
    fun logSummary(tag: String) {
        val avgCaptureMs = if (totalFrames > 0) totalCaptureTimeMs / totalFrames.toFloat() else 0f
        val avgEncodeMs = if (totalFrames > 0) totalEncodingTimeMs / totalFrames.toFloat() else 0f
        val avgTotalFrameMs = if (totalFrames > 0) (totalCaptureTimeMs + totalEncodingTimeMs) / totalFrames.toFloat() else 0f
        val effectiveFps = if (totalTimeMs > 0) (totalFrames * 1000f) / totalTimeMs else 0f

        Log.i(tag, """
            ╔══════════════════════════════════════════════════════════
            ║ HYPERFRAMES ENGINE PROFILING REPORT
            ╠══════════════════════════════════════════════════════════
            ║ HTML generation:      ${htmlGenTimeMs.toString().padStart(6)} ms
            ║ WebView preparation:  ${webViewPrepTimeMs.toString().padStart(6)} ms
            ║ Frame capture:        ${totalCaptureTimeMs.toString().padStart(6)} ms (avg ${String.format(Locale.US, "%.1f", avgCaptureMs)} ms/frame)
            ║ Bitmap processing:    ${totalUiUpdateTimeMs.toString().padStart(6)} ms (throttled preview)
            ║ Encoding:             ${totalEncodingTimeMs.toString().padStart(6)} ms (avg ${String.format(Locale.US, "%.1f", avgEncodeMs)} ms/frame)
            ║ Audio processing:     ${audioPrepTimeMs.toString().padStart(6)} ms
            ║ Total render time:    ${totalTimeMs.toString().padStart(6)} ms
            ╠──────────────────────────────────────────────────────────
            ║ Frames rendered:      $totalFrames
            ║ Average frame time:   ${String.format(Locale.US, "%.1f", avgTotalFrameMs)} ms
            ║ Slowest frame:        $slowestFrameMs ms
            ║ Effective export FPS: ${String.format(Locale.US, "%.1f", effectiveFps)}
            ╚══════════════════════════════════════════════════════════
        """.trimIndent())
    }
}

class HyperFramesEngine(private val context: Context) {
    private val TAG = "HyperFramesEngine"

    fun renderVideo(
        config: RenderConfiguration
    ): Flow<RenderState> = flow {
        val overallStartTimeMs = System.currentTimeMillis()
        val profile = RenderProfile()

        val (width, height) = config.getEffectiveDimensions()
        val totalFrames = config.totalFrames
        val fps = config.fps
        val durationSec = config.durationSec
        profile.totalFrames = totalFrames

        emit(RenderState.Initializing(totalFrames))

        // Create destination file
        val outputDir = File(context.filesDir, "rendered_videos").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HyperFrame_${config.projectName.replace(" ", "_")}_$timeStamp.mp4"
        val outputFile = File(outputDir, fileName)

        val capturer = HtmlFrameCapturer(context)
        var encoder: HtmlVideoEncoder? = null

        // Pre-allocate ping-pong double buffers to avoid any frame-by-frame heap allocations
        var buffer0: Bitmap? = null
        var buffer1: Bitmap? = null
        var canvas0: Canvas? = null
        var canvas1: Canvas? = null

        try {
            // 1. Measure HTML Generation
            val htmlStartMs = System.currentTimeMillis()
            val compiledHtml = config.customHtml ?: ""
            profile.htmlGenTimeMs = System.currentTimeMillis() - htmlStartMs

            // 2. Measure Headless WebView Preparation
            val prepStartMs = System.currentTimeMillis()
            capturer.setup(compiledHtml, width, height)
            profile.webViewPrepTimeMs = System.currentTimeMillis() - prepStartMs

            // 3. Setup Hardware Video Encoder
            val bitRate = when {
                width >= 1080 -> 10_000_000
                width >= 720 -> 6_000_000
                else -> 3_000_000
            }
            encoder = HtmlVideoEncoder(outputFile, width, height, fps, bitRate)
            encoder.start()

            // Allocate 2 reusable Bitmaps & Canvases once
            buffer0 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            buffer1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas0 = Canvas(buffer0)
            canvas1 = Canvas(buffer1)

            val buffers = arrayOf(buffer0, buffer1)
            val canvases = arrayOf(canvas0, canvas1)

            var renderedFrames = 0
            val renderLoopStartTime = System.currentTimeMillis()
            var lastUiUpdateMs = 0L

            val previewW = (width / 3).coerceAtLeast(180)
            val previewH = (height / 3).coerceAtLeast(180)

            for (frame in 0 until totalFrames) {
                val frameStartMs = System.currentTimeMillis()
                val currentTimeSec = frame / fps.toFloat()
                val progress = if (totalFrames > 1) frame / (totalFrames - 1).toFloat() else 0f

                // Select ping-pong buffer
                val bufIdx = frame % 2
                val currentBitmap = buffers[bufIdx]
                val currentCanvas = canvases[bufIdx]

                // Frame Capture (direct offscreen WebView draw into targetCanvas)
                val captureStartMs = System.currentTimeMillis()
                capturer.captureFrame(
                    timeSec = currentTimeSec,
                    progress = progress,
                    targetCanvas = currentCanvas
                )
                val captureDurationMs = System.currentTimeMillis() - captureStartMs
                profile.totalCaptureTimeMs += captureDurationMs

                // Hardware Encode Frame into H.264
                val encodeStartMs = System.currentTimeMillis()
                encoder.encodeFrame(currentBitmap, frame)
                val encodeDurationMs = System.currentTimeMillis() - encodeStartMs
                profile.totalEncodingTimeMs += encodeDurationMs

                val frameTotalMs = captureDurationMs + encodeDurationMs
                if (frameTotalMs > profile.slowestFrameMs) {
                    profile.slowestFrameMs = frameTotalMs
                }

                renderedFrames++

                // Throttled UI Progress & Preview Thumbnail update (~5 times/sec)
                // Prevents UI thread congestion and eliminates 95% of thumbnail downscaling allocations
                val nowMs = System.currentTimeMillis()
                val isLastFrame = (frame == totalFrames - 1)
                val shouldUpdateUi = (frame == 0 || isLastFrame || (nowMs - lastUiUpdateMs >= 180))

                if (shouldUpdateUi) {
                    val uiStartMs = System.currentTimeMillis()
                    lastUiUpdateMs = nowMs

                    val elapsedMs = nowMs - renderLoopStartTime
                    val currentFps = if (elapsedMs > 0) (renderedFrames * 1000f) / elapsedMs else 0f
                    val remainingFrames = totalFrames - renderedFrames
                    val estimatedRemainingSec = if (currentFps > 0) (remainingFrames / currentFps).toInt() else 0

                    val previewBitmap = Bitmap.createScaledBitmap(currentBitmap, previewW, previewH, true)
                    profile.totalUiUpdateTimeMs += (System.currentTimeMillis() - uiStartMs)

                    emit(
                        RenderState.Rendering(
                            currentFrame = frame + 1,
                            totalFrames = totalFrames,
                            progress = (frame + 1).toFloat() / totalFrames.toFloat(),
                            fpsSpeed = currentFps,
                            previewBitmap = previewBitmap,
                            elapsedTimeMs = elapsedMs,
                            estimatedRemainingSec = estimatedRemainingSec
                        )
                    )
                }
            }

            emit(RenderState.Finalizing(totalFrames))

            // Finish encoding & muxing
            val finalizeStartMs = System.currentTimeMillis()
            encoder.finish()
            encoder = null
            profile.totalEncodingTimeMs += (System.currentTimeMillis() - finalizeStartMs)

            profile.totalTimeMs = System.currentTimeMillis() - overallStartTimeMs
            profile.logSummary(TAG)

            val fileSizeBytes = outputFile.length()
            val fileSizeFormatted = formatFileSize(fileSizeBytes)

            emit(
                RenderState.Completed(
                    videoFile = outputFile,
                    durationSec = durationSec,
                    width = width,
                    height = height,
                    fps = fps,
                    fileSizeFormatted = fileSizeFormatted
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Render failed", e)
            emit(RenderState.Error(e.message ?: "Video rendering failed"))
        } finally {
            try {
                encoder?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing encoder in finally", e)
            }
            capturer.destroy()
            buffer0?.recycle()
            buffer1?.recycle()
        }
    }.flowOn(Dispatchers.Default)

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
            bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }
}
