package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.model.AspectRatioType
import com.example.model.RenderConfiguration
import com.example.model.RenderState
import com.example.model.SavedVideo
import com.example.model.VideoTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HyperFramesEngine(private val context: Context) {
    private val TAG = "HyperFramesEngine"

    fun renderVideo(
        template: VideoTemplate,
        config: RenderConfiguration
    ): Flow<RenderState> = flow {
        val startTimeMs = System.currentTimeMillis()
        val (width, height) = config.getEffectiveDimensions()
        val totalFrames = config.totalFrames
        val fps = config.fps
        val durationSec = config.durationSec

        emit(RenderState.Initializing(totalFrames))

        // Create destination file
        val outputDir = File(context.filesDir, "rendered_videos").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HyperFrame_${template.name.replace(" ", "_")}_$timeStamp.mp4"
        val outputFile = File(outputDir, fileName)

        val capturer = HtmlFrameCapturer(context)
        var encoder: HtmlVideoEncoder? = null
        var frameBitmap: Bitmap? = null

        try {
            // Compile HTML with custom or template code
            val compiledHtml = if (config.customHtml != null && config.customCss != null && config.customJs != null) {
                // User custom code mode
                val customTemplate = template.copy(
                    htmlBody = config.customHtml,
                    cssStyle = config.customCss,
                    jsScript = config.customJs
                )
                customTemplate.compileFullHtml(
                    paramsMap = config.paramsMap,
                    currentTimeSec = 0f,
                    durationSec = durationSec,
                    isLivePlaying = false
                )
            } else {
                template.compileFullHtml(
                    paramsMap = config.paramsMap,
                    currentTimeSec = 0f,
                    durationSec = durationSec,
                    isLivePlaying = false
                )
            }

            // Setup Headless Frame Capturer
            capturer.setup(compiledHtml, width, height)

            // Setup Hardware MediaCodec Video Encoder
            val bitRate = when {
                width >= 1080 -> 10_000_000
                width >= 720 -> 6_000_000
                else -> 3_000_000
            }
            encoder = HtmlVideoEncoder(outputFile, width, height, fps, bitRate)
            encoder.start()

            var renderedFrames = 0
            val renderStartTime = System.currentTimeMillis()

            frameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (frame in 0 until totalFrames) {
                val currentTimeSec = frame / fps.toFloat()
                val progress = if (totalFrames > 1) frame / (totalFrames - 1).toFloat() else 0f

                // 1. Capture HTML/DOM frame into Bitmap
                val captured = capturer.captureFrame(
                    timeSec = currentTimeSec,
                    progress = progress,
                    width = width,
                    height = height,
                    reusableBitmap = frameBitmap
                )

                // 2. Hardware encode frame into MP4
                encoder.encodeFrame(captured, frame)
                renderedFrames++

                // 3. Emit progress
                val elapsedMs = System.currentTimeMillis() - renderStartTime
                val currentFps = if (elapsedMs > 0) (renderedFrames * 1000f) / elapsedMs else 0f

                // Downscale preview bitmap for UI display performance
                val previewBitmap = Bitmap.createScaledBitmap(
                    captured,
                    (width / 3).coerceAtLeast(180),
                    (height / 3).coerceAtLeast(180),
                    true
                )

                emit(
                    RenderState.Rendering(
                        currentFrame = frame + 1,
                        totalFrames = totalFrames,
                        progress = (frame + 1).toFloat() / totalFrames.toFloat(),
                        fpsSpeed = currentFps,
                        previewBitmap = previewBitmap,
                        elapsedTimeMs = elapsedMs
                    )
                )
            }

            emit(RenderState.Finalizing(totalFrames))

            // Finish encoding & muxing
            encoder.finish()
            encoder = null

            val totalTimeMs = System.currentTimeMillis() - startTimeMs
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
            frameBitmap?.recycle()
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
