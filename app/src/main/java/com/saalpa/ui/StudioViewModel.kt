package com.saalpa.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saalpa.data.ImportedZipAsset
import com.saalpa.data.MediaHelper
import com.saalpa.data.TemplateRepository
import com.saalpa.data.VideoStorageManager
import com.saalpa.data.VoiceoverAudioService
import com.saalpa.data.ZipMediaManager
import com.saalpa.engine.HyperFramesEngine
import com.saalpa.engine.HyperFramesJsBridge
import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.MediaOverlayItem
import com.saalpa.model.MediaType
import com.saalpa.model.PresetMediaAssets
import com.saalpa.model.RenderConfiguration
import com.saalpa.model.RenderResolution
import com.saalpa.model.RenderState
import com.saalpa.model.SavedVideo
import com.saalpa.model.SceneMarker
import com.saalpa.model.VideoEffectType
import com.saalpa.model.VideoTemplate
import com.saalpa.model.VoiceoverState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Stack

enum class StudioTab(val label: String, val iconRes: String) {
    EDIT("Изменить", "ContentCut"),
    ELEMENTS("Элементы", "Layers"),
    AUDIO("Аудио", "MusicNote"),
    MEDIA("Медиа", "FolderZip"),
    CODE("Код", "Code"),
    GALLERY("Галерея", "VideoLibrary")
}

data class StudioUiState(
    val activeTab: StudioTab = StudioTab.EDIT,
    val selectedTemplate: VideoTemplate = TemplateRepository.templates.first(),
    val paramsMap: Map<String, String> = emptyMap(),
    val customHtml: String = "",
    val customCss: String = "",
    val customJs: String = "",
    val isCustomCodeActive: Boolean = false,
    
    // Timeline & Playback
    val currentTimeSec: Float = 0f,
    val durationSec: Float = 6.0f,
    val isPlaying: Boolean = false,
    val fps: Int = 30,
    val playbackSpeed: Float = 1.0f,
    val aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
    val resolution: RenderResolution = RenderResolution.HD_720P,
    val timelineZoom: Float = 1.0f, // 1.0 .. 3.0

    // Selected Timeline Element
    val selectedElementId: String? = null,

    // GSAP Scene Markers
    val sceneMarkers: List<SceneMarker> = emptyList(),
    val activeSceneIndex: Int = 0,
    val activeSceneId: String = "",
    val activeSceneTitle: String = "",

    // Multi-track Overlays (Videos, Photos, Audio, Text, FX)
    val mediaOverlays: List<MediaOverlayItem> = emptyList(),
    val voiceoverState: VoiceoverState = VoiceoverState(),

    // Media Hub & ZIP Assets
    val importedAssets: List<ImportedZipAsset> = emptyList(),
    val isImportingZip: Boolean = false,

    // Settings & Dialogs
    val showSettingsDialog: Boolean = false,
    val renderState: RenderState = RenderState.Idle,
    val showRenderDialog: Boolean = false,

    // Gallery
    val savedVideos: List<SavedVideo> = emptyList(),
    val selectedGalleryVideo: SavedVideo? = null,

    // Info Modal
    val showExplainerDialog: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    val totalFrames: Int get() = (durationSec * fps).toInt().coerceAtLeast(1)
    val currentFrameIndex: Int get() = ((currentTimeSec / durationSec) * totalFrames).toInt().coerceIn(0, totalFrames - 1)
    val progress: Float get() = if (durationSec > 0f) (currentTimeSec / durationSec).coerceIn(0f, 1f) else 0f
    val selectedItem: MediaOverlayItem? get() = mediaOverlays.find { it.id == selectedElementId }
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "StudioViewModel"
    private val context: Context = application.applicationContext
    private val engine = HyperFramesEngine(context)
    private val storageManager = VideoStorageManager(context)
    private val voiceoverService = VoiceoverAudioService(context)
    private val zipMediaManager = ZipMediaManager(context)

    private val undoStack = Stack<List<MediaOverlayItem>>()
    private val redoStack = Stack<List<MediaOverlayItem>>()

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var renderJob: Job? = null

    val jsBridge = HyperFramesJsBridge(
        onTimelineReadyListener = { durationSec, totalScenes, json ->
            onJsTimelineReady(durationSec, totalScenes, json)
        },
        onTickListener = { timeSec, prog, isPlay ->
            onJsTick(timeSec, prog, isPlay)
        },
        onSceneChangeListener = { index, id, title ->
            onJsSceneChange(index, id, title)
        },
        onLogListener = { lvl, msg ->
            Log.d(TAG, "[$lvl] $msg")
        }
    )

    init {
        // Initialize with standard mobile video starter project (CapCut default without presets)
        val defaultTemplate = TemplateRepository.templates.first()
        val initialParams = defaultTemplate.params.associate { it.key to it.defaultValue }
        
        val initialOverlays = listOf(
            MediaOverlayItem(
                id = "default_title_text",
                type = MediaType.TEXT,
                title = "Главный заголовок",
                textContent = "CAPCUT EDITOR",
                textColor = "#00F0FF",
                fontSizeSp = 32,
                startTimeSec = 0.5f,
                durationSec = 4.0f,
                xPercent = 50f,
                yPercent = 25f,
                animation = MediaAnimationType.ZOOM_IN
            ),
            MediaOverlayItem(
                id = "default_bg_glow",
                type = MediaType.EFFECT,
                title = "Neon Glow Effect",
                effectType = VideoEffectType.NEON_GLOW,
                startTimeSec = 0f,
                durationSec = 6.0f,
                opacity = 0.7f
            )
        )

        _uiState.update {
            it.copy(
                selectedTemplate = defaultTemplate,
                paramsMap = initialParams,
                customHtml = defaultTemplate.htmlBody,
                customCss = defaultTemplate.cssStyle,
                customJs = defaultTemplate.jsScript,
                durationSec = 6.0f,
                fps = 30,
                aspectRatio = AspectRatioType.PORTRAIT_9_16,
                mediaOverlays = initialOverlays
            )
        }

        refreshGallery()
        observeVoiceoverService()
        loadImportedAssets()
    }

    fun loadImportedAssets() {
        viewModelScope.launch {
            val list = zipMediaManager.loadAllImportedAssets()
            _uiState.update { it.copy(importedAssets = list) }
        }
    }

    fun importZipArchive(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingZip = true) }
            val newAssets = zipMediaManager.unpackZip(uri)
            val all = zipMediaManager.loadAllImportedAssets()
            _uiState.update { it.copy(importedAssets = all, isImportingZip = false) }
        }
    }

    fun importMediaFiles(uris: List<Uri>) {
        viewModelScope.launch {
            val newAssets = zipMediaManager.importFiles(uris)
            val all = zipMediaManager.loadAllImportedAssets()
            _uiState.update { it.copy(importedAssets = all) }
        }
    }

    fun deleteImportedAsset(asset: ImportedZipAsset) {
        viewModelScope.launch {
            zipMediaManager.deleteAsset(asset)
            val all = zipMediaManager.loadAllImportedAssets()
            _uiState.update { it.copy(importedAssets = all) }
        }
    }

    fun addImportedAssetToTimeline(asset: ImportedZipAsset) {
        pushUndo()
        val state = _uiState.value
        val source = asset.dataUrl ?: asset.file.toURI().toString()
        val newItem = when (asset.mediaType) {
            MediaType.BGM, MediaType.VOICEOVER -> {
                MediaOverlayItem(
                    type = asset.mediaType,
                    title = asset.name,
                    sourceUri = source,
                    startTimeSec = state.currentTimeSec,
                    durationSec = 10f,
                    volume = 1.0f
                )
            }
            MediaType.VIDEO -> {
                MediaOverlayItem(
                    type = MediaType.VIDEO,
                    title = asset.name,
                    sourceUri = source,
                    startTimeSec = state.currentTimeSec,
                    durationSec = 6f,
                    opacity = 0.9f,
                    animation = MediaAnimationType.STATIC
                )
            }
            else -> {
                MediaOverlayItem(
                    type = MediaType.PHOTO,
                    title = asset.name,
                    sourceUri = source,
                    startTimeSec = state.currentTimeSec,
                    durationSec = 4f,
                    animation = MediaAnimationType.ZOOM_IN
                )
            }
        }
        _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + newItem, selectedElementId = newItem.id) }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    private fun pushUndo() {
        undoStack.push(_uiState.value.mediaOverlays)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.pop()
            redoStack.push(_uiState.value.mediaOverlays)
            _uiState.update {
                it.copy(
                    mediaOverlays = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(_uiState.value.mediaOverlays)
            _uiState.update {
                it.copy(
                    mediaOverlays = next,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    private fun observeVoiceoverService() {
        viewModelScope.launch {
            voiceoverService.isRecording.collectLatest { isRec ->
                _uiState.update {
                    it.copy(voiceoverState = it.voiceoverState.copy(isRecording = isRec))
                }
            }
        }
        viewModelScope.launch {
            voiceoverService.recordingDurationSec.collectLatest { dur ->
                _uiState.update {
                    it.copy(voiceoverState = it.voiceoverState.copy(recordingDurationSec = dur))
                }
            }
        }
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update { it.copy(activeTab = tab) }
        if (tab == StudioTab.GALLERY) {
            refreshGallery()
        }
    }

    fun setSelectedElement(id: String?) {
        _uiState.update { it.copy(selectedElementId = id) }
    }

    fun updateParam(key: String, value: String) {
        _uiState.update {
            val newMap = it.paramsMap.toMutableMap().apply { put(key, value) }
            it.copy(paramsMap = newMap)
        }
    }

    fun updateCustomCode(html: String, css: String, js: String) {
        val detectedDuration = Regex("""data-duration=["']([0-9.]+)["']""").find(html)?.groupValues?.get(1)?.toFloatOrNull()
        _uiState.update {
            val newDuration = detectedDuration ?: it.durationSec
            val clampedTime = it.currentTimeSec.coerceAtMost(newDuration)
            it.copy(
                customHtml = html,
                customCss = css,
                customJs = js,
                isCustomCodeActive = true,
                durationSec = newDuration,
                currentTimeSec = clampedTime
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
            val clamped = durationSec.coerceAtLeast(0.5f)
            val clampedTime = it.currentTimeSec.coerceAtMost(clamped)
            it.copy(durationSec = clamped, currentTimeSec = clampedTime)
        }
    }

    fun setTimelineZoom(zoom: Float) {
        _uiState.update { it.copy(timelineZoom = zoom.coerceIn(0.5f, 3.0f)) }
    }

    // --- Timeline Playback ---

    fun togglePlay() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        
        playbackJob = viewModelScope.launch {
            val speed = _uiState.value.playbackSpeed
            var initialTime = _uiState.value.currentTimeSec
            if (initialTime >= _uiState.value.durationSec - 0.05f) {
                initialTime = 0f
            }

            val startEpoch = System.currentTimeMillis() - ((initialTime / speed) * 1000L).toLong()

            while (isActive && _uiState.value.isPlaying) {
                val currentSpeed = _uiState.value.playbackSpeed
                val elapsedSec = ((System.currentTimeMillis() - startEpoch) / 1000f) * currentSpeed
                val duration = _uiState.value.durationSec

                if (elapsedSec >= duration) {
                    _uiState.update { it.copy(currentTimeSec = 0f) }
                    play()
                    break
                } else {
                    _uiState.update { it.copy(currentTimeSec = elapsedSec) }
                }
                delay(25)
            }
        }
    }

    fun pause() {
        stopPlayback()
    }

    fun seekTo(timeSec: Float) {
        val duration = _uiState.value.durationSec
        val clamped = timeSec.coerceIn(0f, duration)
        _uiState.update { it.copy(currentTimeSec = clamped) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val validSpeed = speed.coerceIn(0.25f, 4.0f)
        _uiState.update { it.copy(playbackSpeed = validSpeed) }
        if (_uiState.value.isPlaying) {
            play()
        }
    }

    fun stepFrame(deltaFrames: Int) {
        val state = _uiState.value
        val frameDuration = 1f / state.fps.toFloat()
        val nextTime = (state.currentTimeSec + deltaFrames * frameDuration).coerceIn(0f, state.durationSec)
        seekTo(nextTime)
    }

    fun jumpToScene(marker: SceneMarker) {
        seekTo(marker.startSec)
        _uiState.update {
            it.copy(
                activeSceneIndex = marker.index,
                activeSceneId = marker.id,
                activeSceneTitle = marker.title
            )
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    // --- CapCut Clip & Track Editing Actions (Split, Trim, Delete, Duplicate) ---

    fun splitSelectedClipAtPlayhead() {
        val state = _uiState.value
        val targetId = state.selectedElementId ?: return
        val item = state.mediaOverlays.find { it.id == targetId } ?: return
        val currentPlayhead = state.currentTimeSec

        if (currentPlayhead > item.startTimeSec && currentPlayhead < item.endTimeSec) {
            pushUndo()
            val firstPartDuration = currentPlayhead - item.startTimeSec
            val secondPartDuration = item.durationSec - firstPartDuration

            val part1 = item.copy(durationSec = firstPartDuration)
            val part2 = item.copy(
                id = java.util.UUID.randomUUID().toString(),
                startTimeSec = currentPlayhead,
                durationSec = secondPartDuration
            )

            val updatedList = state.mediaOverlays.map { if (it.id == targetId) part1 else it } + part2
            _uiState.update { it.copy(mediaOverlays = updatedList, selectedElementId = part2.id) }
        }
    }

    fun deleteSelectedClip() {
        val targetId = _uiState.value.selectedElementId ?: return
        pushUndo()
        _uiState.update { state ->
            state.copy(
                mediaOverlays = state.mediaOverlays.filterNot { it.id == targetId },
                selectedElementId = null
            )
        }
    }

    fun duplicateSelectedClip() {
        val targetId = _uiState.value.selectedElementId ?: return
        val item = _uiState.value.mediaOverlays.find { it.id == targetId } ?: return
        pushUndo()
        val duplicated = item.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${item.title} (Копия)",
            startTimeSec = (item.startTimeSec + 0.5f).coerceAtMost(_uiState.value.durationSec - 1f)
        )
        _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + duplicated, selectedElementId = duplicated.id) }
    }

    // --- CapCut Overlays (Photos, Videos, Text, Effects, Voiceover) ---

    fun addTextOverlay(
        text: String = "Новый текст",
        colorHex: String = "#00F0FF",
        fontSizeSp: Int = 28,
        animation: MediaAnimationType = MediaAnimationType.ZOOM_IN
    ) {
        pushUndo()
        val state = _uiState.value
        val newItem = MediaOverlayItem(
            type = MediaType.TEXT,
            title = "Текст: $text",
            textContent = text,
            textColor = colorHex,
            fontSizeSp = fontSizeSp,
            startTimeSec = state.currentTimeSec,
            durationSec = 3.5f,
            xPercent = 50f,
            yPercent = 50f,
            animation = animation
        )
        _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + newItem, selectedElementId = newItem.id) }
    }

    fun addEffectOverlay(effectType: VideoEffectType) {
        pushUndo()
        val state = _uiState.value
        val newItem = MediaOverlayItem(
            type = MediaType.EFFECT,
            title = effectType.title.split("(").first().trim(),
            effectType = effectType,
            startTimeSec = state.currentTimeSec,
            durationSec = 4f,
            opacity = 0.8f
        )
        _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + newItem, selectedElementId = newItem.id) }
    }

    fun addPhotoFromUri(uri: Uri, title: String = "Фото клип") {
        viewModelScope.launch {
            val dataUrl = MediaHelper.uriToDataUrl(context, uri, "image/png")
            if (dataUrl != null) {
                pushUndo()
                val state = _uiState.value
                val newItem = MediaOverlayItem(
                    type = MediaType.PHOTO,
                    title = title,
                    sourceUri = dataUrl,
                    startTimeSec = state.currentTimeSec,
                    durationSec = 4f,
                    animation = MediaAnimationType.ZOOM_IN
                )
                _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + newItem, selectedElementId = newItem.id) }
            }
        }
    }

    fun addVideoFromUri(uri: Uri, title: String = "Видео футаж") {
        viewModelScope.launch {
            val savedFile = MediaHelper.copyUriToInternalFile(context, uri, "imported_videos", "vid", "mp4")
            val source = savedFile?.toURI()?.toString() ?: uri.toString()
            pushUndo()
            val state = _uiState.value
            val newItem = MediaOverlayItem(
                type = MediaType.VIDEO,
                title = title,
                sourceUri = source,
                startTimeSec = state.currentTimeSec,
                durationSec = 6f,
                opacity = 0.9f,
                animation = MediaAnimationType.STATIC
            )
            _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + newItem, selectedElementId = newItem.id) }
        }
    }

    fun addAudioFromUri(uri: Uri, title: String = "Аудиодорожка", isVoiceover: Boolean = true) {
        viewModelScope.launch {
            val dataUrl = MediaHelper.uriToDataUrl(context, uri, "audio/mp3")
            val source = dataUrl ?: uri.toString()
            pushUndo()
            val state = _uiState.value
            val newItem = MediaOverlayItem(
                type = if (isVoiceover) MediaType.VOICEOVER else MediaType.BGM,
                title = title,
                sourceUri = source,
                startTimeSec = state.currentTimeSec,
                durationSec = (state.durationSec - state.currentTimeSec).coerceAtLeast(2f),
                volume = if (isVoiceover) state.voiceoverState.voiceoverVolume else state.voiceoverState.bgmVolume
            )
            _uiState.update { it.copy(mediaOverlays = it.mediaOverlays + newItem, selectedElementId = newItem.id) }
        }
    }

    fun updateMediaOverlay(item: MediaOverlayItem) {
        _uiState.update { state ->
            val updatedList = state.mediaOverlays.map { if (it.id == item.id) item else it }
            state.copy(mediaOverlays = updatedList)
        }
    }

    fun removeMediaOverlay(id: String) {
        pushUndo()
        _uiState.update { state ->
            state.copy(
                mediaOverlays = state.mediaOverlays.filterNot { it.id == id },
                selectedElementId = if (state.selectedElementId == id) null else state.selectedElementId
            )
        }
    }

    fun toggleMediaOverlayEnabled(id: String) {
        _uiState.update { state ->
            val updated = state.mediaOverlays.map {
                if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
            }
            state.copy(mediaOverlays = updated)
        }
    }

    // --- Voiceover Recording ---

    fun startVoiceoverRecording() {
        voiceoverService.startRecording(viewModelScope)
    }

    fun stopVoiceoverRecording() {
        val file = voiceoverService.stopRecording()
        if (file != null) {
            pushUndo()
            val dataUri = Uri.fromFile(file)
            val dataUrl = MediaHelper.uriToDataUrl(context, dataUri, "audio/mp4") ?: file.toURI().toString()
            val state = _uiState.value
            val duration = _uiState.value.voiceoverState.recordingDurationSec.coerceAtLeast(1.5f)
            val count = _uiState.value.mediaOverlays.count { it.type == MediaType.VOICEOVER } + 1
            val newItem = MediaOverlayItem(
                type = MediaType.VOICEOVER,
                title = "Запись голоса #$count",
                sourceUri = dataUrl,
                startTimeSec = state.currentTimeSec,
                durationSec = duration,
                volume = _uiState.value.voiceoverState.voiceoverVolume
            )
            _uiState.update {
                it.copy(
                    mediaOverlays = it.mediaOverlays + newItem,
                    voiceoverState = it.voiceoverState.copy(recordedAudioPath = file.absolutePath),
                    selectedElementId = newItem.id
                )
            }
        }
    }

    fun cancelVoiceoverRecording() {
        voiceoverService.cancelRecording()
    }

    fun setVoiceoverVolume(vol: Float) {
        _uiState.update {
            it.copy(voiceoverState = it.voiceoverState.copy(voiceoverVolume = vol.coerceIn(0f, 2f)))
        }
    }

    fun setBgmVolume(vol: Float) {
        _uiState.update {
            it.copy(voiceoverState = it.voiceoverState.copy(bgmVolume = vol.coerceIn(0f, 2f)))
        }
    }

    fun toggleVoiceoverMute() {
        _uiState.update {
            it.copy(voiceoverState = it.voiceoverState.copy(isVoiceoverMuted = !it.voiceoverState.isVoiceoverMuted))
        }
    }

    fun toggleBgmMute() {
        _uiState.update {
            it.copy(voiceoverState = it.voiceoverState.copy(isBgmMuted = !it.voiceoverState.isBgmMuted))
        }
    }

    // --- JavaScriptInterface Callbacks ---

    fun onJsTimelineReady(durationSec: Float, totalScenes: Int, sceneDataJson: String) {
        val sceneList = mutableListOf<SceneMarker>()
        try {
            val array = JSONArray(sceneDataJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                sceneList.add(
                    SceneMarker(
                        index = obj.optInt("index", i),
                        id = obj.optString("id", "scene-$i"),
                        title = obj.optString("title", "Scene ${i + 1}"),
                        startSec = obj.optDouble("startSec", (i * 4).toDouble()).toFloat(),
                        durationSec = obj.optDouble("durationSec", 4.0).toFloat()
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing scenes: ${e.message}")
        }

        _uiState.update { state ->
            state.copy(
                sceneMarkers = if (sceneList.isNotEmpty()) sceneList else state.sceneMarkers
            )
        }
    }

    fun onJsTick(currentTimeSec: Float, progress: Float, isPlaying: Boolean) {}

    fun onJsSceneChange(sceneIndex: Int, sceneId: String, sceneTitle: String) {
        _uiState.update {
            it.copy(
                activeSceneIndex = sceneIndex,
                activeSceneId = sceneId,
                activeSceneTitle = sceneTitle
            )
        }
    }

    // --- Rendering ---

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
            paramsMap = state.paramsMap,
            mediaOverlays = state.mediaOverlays,
            voiceoverState = state.voiceoverState
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

    // --- Gallery ---

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
            isLivePlaying = state.isPlaying,
            mediaOverlays = state.mediaOverlays,
            voiceoverState = state.voiceoverState
        )
    }
}
