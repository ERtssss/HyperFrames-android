package com.saalpa.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class VoiceoverAudioService(private val context: Context) {
    private val TAG = "VoiceoverAudioService"
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var recordTimerJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0f)
    val recordingDurationSec: StateFlow<Float> = _recordingDurationSec.asStateFlow()

    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    fun startRecording(coroutineScope: CoroutineScope): File? {
        try {
            stopRecording()
            stopPreview()

            val dir = File(context.cacheDir, "voiceovers").apply { mkdirs() }
            val file = File(dir, "voiceover_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _recordingDurationSec.value = 0f

            recordTimerJob = coroutineScope.launch(Dispatchers.Main) {
                val start = System.currentTimeMillis()
                while (isActive && _isRecording.value) {
                    val elapsed = (System.currentTimeMillis() - start) / 1000f
                    _recordingDurationSec.value = elapsed
                    delay(100)
                }
            }

            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording: ${e.message}", e)
            _isRecording.value = false
            return null
        }
    }

    fun stopRecording(): File? {
        recordTimerJob?.cancel()
        recordTimerJob = null
        _isRecording.value = false

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaRecorder: ${e.message}")
        }
        mediaRecorder = null

        val file = currentRecordingFile
        if (file != null && file.exists() && file.length() > 0) {
            return file
        }
        return null
    }

    fun cancelRecording() {
        recordTimerJob?.cancel()
        recordTimerJob = null
        _isRecording.value = false

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling MediaRecorder: ${e.message}")
        }
        mediaRecorder = null

        currentRecordingFile?.delete()
        currentRecordingFile = null
    }

    fun playPreview(audioUri: Uri, onComplete: () -> Unit = {}) {
        stopPreview()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, audioUri)
                setOnCompletionListener {
                    _isPreviewPlaying.value = false
                    onComplete()
                }
                prepare()
                start()
            }
            _isPreviewPlaying.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preview audio: ${e.message}", e)
            _isPreviewPlaying.value = false
        }
    }

    fun stopPreview() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaPlayer: ${e.message}")
        }
        mediaPlayer = null
        _isPreviewPlaying.value = false
    }
}
