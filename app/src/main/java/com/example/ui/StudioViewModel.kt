package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TemplateRepository
import com.example.data.VideoStorageManager
import com.example.engine.HyperFramesEngine
import com.example.model.AspectRatioType
import com.example.model.RenderConfiguration
import com.example.model.RenderResolution
import com.example.model.RenderState
import com.example.model.SavedVideo
import com.example.model.VideoTemplate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class StudioTab(val label: String) {
    TEMPLATES("Presets"),
    CUSTOMIZE("Quick Edit"),
    CODE("Code"),
    SETTINGS("Format"),
    GALLERY("Gallery")
}

data class StudioUiState(
    val activeTab: StudioTab = StudioTab.TEMPLATES,
    val selectedTemplate: VideoTemplate = TemplateRepository.templates.first(),
    val paramsMap: Map<String, String> = emptyMap(),
    val customHtml: String = "",
    val customCss: String = "",
    val customJs: String = "",
    val isCustomCodeActive: Boolean = false,
    
    // Timeline & Playback
    val currentTimeSec: Float = 0f,
    val durationSec: Float = 3.5f,
    val isPlaying: Boolean = false,
    val fps: Int = 30,
    val aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
    val resolution: RenderResolution = RenderResolution.HD_720P,

    // Rendering State
    val renderState: RenderState = RenderState.Idle,
    val showRenderDialog: Boolean = false,

    // Gallery
    val savedVideos: List<SavedVideo> = emptyList(),
    val selectedGalleryVideo: SavedVideo? = null,

    // Info Modal
    val showExplainerDialog: Boolean = false
) {
    val totalFrames: Int get() = (durationSec * fps).toInt().coerceAtLeast(1)
    val currentFrameIndex: Int get() = ((currentTimeSec / durationSec) * totalFrames).toInt().coerceIn(0, totalFrames - 1)
    val progress: Float get() = if (durationSec > 0f) (currentTimeSec / durationSec).coerceIn(0f, 1f) else 0f
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val engine = HyperFramesEngine(application.applicationContext)
    private val storageManager = VideoStorageManager(application.applicationContext)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var renderJob: Job? = null

    init {
        loadTemplate(TemplateRepository.templates.first())
        refreshGallery()
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update { it.copy(activeTab = tab) }
        if (tab == StudioTab.GALLERY) {
            refreshGallery()
        }
    }

    fun loadTemplate(template: VideoTemplate) {
        val initialParams = template.params.associate { it.key to it.defaultValue }
        _uiState.update {
            it.copy(
                selectedTemplate = template,
                paramsMap = initialParams,
                customHtml = template.htmlBody,
                customCss = template.cssStyle,
                customJs = template.jsScript,
                isCustomCodeActive = false,
                durationSec = template.defaultDurationSec,
                fps = template.defaultFps,
                aspectRatio = template.defaultAspectRatio,
                currentTimeSec = 0f,
                isPlaying = false
            )
        }
        stopPlayback()
    }

    fun updateParam(key: String, value: String) {
        _uiState.update {
            val newMap = it.paramsMap.toMutableMap().apply { put(key, value) }
            it.copy(paramsMap = newMap)
        }
    }

    fun updateCustomCode(html: String, css: String, js: String) {
        _uiState.update {
            it.copy(
                customHtml = html,
                customCss = css,
                customJs = js,
                isCustomCodeActive = true
            )
        }
    }

    fun resetToTemplateCode() {
        val current = _uiState.value.selectedTemplate
        _uiState.update {
            it.copy(
                customHtml = current.htmlBody,
                customCss = current.cssStyle,
                customJs = current.jsScript,
                isCustomCodeActive = false
            )
        }
    }

    fun setAspectRatio(ratio: AspectRatioType) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun setResolution(resolution: RenderResolution) {
        _uiState.update { it.copy(resolution = resolution) }
    }

    fun setFps(fps: Int) {
        _uiState.update { it.copy(fps = fps) }
    }

    fun setDuration(durationSec: Float) {
        _uiState.update {
            val clampedTime = it.currentTimeSec.coerceAtMost(durationSec)
            it.copy(durationSec = durationSec, currentTimeSec = clampedTime)
        }
    }

    // --- Timeline Playback Control ---

    fun togglePlay() {
        if (_uiState.value.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun seekTo(timeSec: Float) {
        val duration = _uiState.value.durationSec
        val clamped = timeSec.coerceIn(0f, duration)
        _uiState.update { it.copy(currentTimeSec = clamped) }
    }

    fun stepFrame(deltaFrames: Int) {
        val state = _uiState.value
        val frameDuration = 1f / state.fps.toFloat()
        val nextTime = (state.currentTimeSec + deltaFrames * frameDuration).coerceIn(0f, state.durationSec)
        _uiState.update { it.copy(currentTimeSec = nextTime) }
    }

    private fun startPlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        
        playbackJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var initialTime = _uiState.value.currentTimeSec
            if (initialTime >= _uiState.value.durationSec - 0.05f) {
                initialTime = 0f
            }

            val startEpoch = System.currentTimeMillis() - (initialTime * 1000L).toLong()

            while (isActive && _uiState.value.isPlaying) {
                val elapsedSec = (System.currentTimeMillis() - startEpoch) / 1000f
                val duration = _uiState.value.durationSec

                if (elapsedSec >= duration) {
                    // Loop animation
                    _uiState.update { it.copy(currentTimeSec = 0f) }
                    startPlayback()
                    break
                } else {
                    _uiState.update { it.copy(currentTimeSec = elapsedSec) }
                }
                delay(30) // ~30 fps preview refresh
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    // --- Video Rendering ---

    fun startRender() {
        stopPlayback()
        val state = _uiState.value

        val config = RenderConfiguration(
            templateId = state.selectedTemplate.id,
            durationSec = state.durationSec,
            fps = state.fps,
            resolution = state.resolution,
            aspectRatio = state.aspectRatio,
            customHtml = if (state.isCustomCodeActive) state.customHtml else null,
            customCss = if (state.isCustomCodeActive) state.customCss else null,
            customJs = if (state.isCustomCodeActive) state.customJs else null,
            paramsMap = state.paramsMap
        )

        _uiState.update { it.copy(showRenderDialog = true, renderState = RenderState.Idle) }

        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            engine.renderVideo(state.selectedTemplate, config).collectLatest { status ->
                _uiState.update { it.copy(renderState = status) }
                if (status is RenderState.Completed) {
                    refreshGallery()
                }
            }
        }
    }

    fun cancelRender() {
        renderJob?.cancel()
        renderJob = null
        _uiState.update { it.copy(showRenderDialog = false, renderState = RenderState.Idle) }
    }

    fun dismissRenderDialog() {
        _uiState.update { it.copy(showRenderDialog = false, renderState = RenderState.Idle) }
    }

    // --- Gallery & Sharing ---

    fun refreshGallery() {
        viewModelScope.launch {
            val list = storageManager.getAllSavedVideos()
            _uiState.update { it.copy(savedVideos = list) }
        }
    }

    fun selectGalleryVideo(video: SavedVideo?) {
        _uiState.update { it.copy(selectedGalleryVideo = video) }
    }

    fun shareVideo(video: SavedVideo) {
        storageManager.shareVideo(video)
    }

    fun deleteVideo(video: SavedVideo) {
        storageManager.deleteVideo(video)
        refreshGallery()
        if (_uiState.value.selectedGalleryVideo?.id == video.id) {
            _uiState.update { it.copy(selectedGalleryVideo = null) }
        }
    }

    // --- Explainer Dialog ---

    fun setShowExplainer(show: Boolean) {
        _uiState.update { it.copy(showExplainerDialog = show) }
    }

    fun getCompiledHtmlForPreview(): String {
        val state = _uiState.value
        val template = if (state.isCustomCodeActive) {
            state.selectedTemplate.copy(
                htmlBody = state.customHtml,
                cssStyle = state.customCss,
                jsScript = state.customJs
            )
        } else {
            state.selectedTemplate
        }

        return template.compileFullHtml(
            paramsMap = state.paramsMap,
            currentTimeSec = state.currentTimeSec,
            durationSec = state.durationSec,
            isLivePlaying = state.isPlaying
        )
    }
}
