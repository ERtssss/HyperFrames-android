package com.saalpa.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saalpa.data.HFProjectSummary
import com.saalpa.data.HFProjectsStorageManager
import com.saalpa.data.ImportedZipAsset
import com.saalpa.data.VideoStorageManager
import com.saalpa.data.VoiceoverAudioService
import com.saalpa.data.ZipMediaManager
import com.saalpa.engine.HyperFramesEngine
import com.saalpa.engine.HyperFramesJsBridge
import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.MediaAssetItem
import com.saalpa.model.MediaCategoryType
import com.saalpa.model.RenderConfiguration
import com.saalpa.model.RenderResolution
import com.saalpa.model.RenderState
import com.saalpa.model.SavedVideo
import com.saalpa.model.VideoEffectType
import com.saalpa.model.project.DefaultProjectFactory
import com.saalpa.model.project.ElementType
import com.saalpa.model.project.ElementTransform
import com.saalpa.model.project.HyperFrameAnimation
import com.saalpa.model.project.HyperFrameElement
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.HyperFramesProject
import com.saalpa.model.project.ProjectHtmlCompiler
import com.saalpa.model.project.SceneAvatarSettings
import com.saalpa.model.project.SceneComposition
import com.saalpa.model.project.SceneTransitionType
import com.saalpa.model.project.SceneVoiceSettings
import com.saalpa.ui.components.StudioActivePanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Stack
import java.util.UUID

data class StudioUiState(
    val project: HyperFramesProject = DefaultProjectFactory.createDefaultProject(),
    val currentProjectDir: File? = null,
    val projectSummaries: List<HFProjectSummary> = emptyList(),
    val isProjectManagerOpen: Boolean = false,
    val isCodeModified: Boolean = false,
    val activeSceneId: String? = "scene-01",
    val selectedElementId: String? = null,
    val activePanel: StudioActivePanel = StudioActivePanel.SCENES,

    // Playback & Timing
    val currentTimeSec: Float = 0f,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,

    // Code IDE
    val isCodeEditorOpen: Boolean = false,
    val customHtml: String = "",
    val customCss: String = "",
    val customJs: String = "",
    val isCustomCodeActive: Boolean = false,

    // Voice Recording
    val isRecordingVoiceover: Boolean = false,
    val recordingDurationSec: Float = 0f,

    // Media & ZIP Hub
    val importedAssets: List<ImportedZipAsset> = emptyList(),
    val isImportingZip: Boolean = false,

    // Dialogs & Render
    val showSettingsDialog: Boolean = false,
    val showRenderDialog: Boolean = false,
    val renderState: RenderState = RenderState.Idle,
    val showExplainerDialog: Boolean = false,

    // Gallery
    val isGalleryOpen: Boolean = false,
    val savedVideos: List<SavedVideo> = emptyList(),
    val selectedGalleryVideo: SavedVideo? = null,

    // History
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    val activeScene: HyperFrameScene? get() = project.scenes.find { it.id == activeSceneId } ?: project.scenes.firstOrNull()
    val selectedElement: HyperFrameElement? get() = activeScene?.elements?.find { it.id == selectedElementId }
    val totalDurationSec: Float get() = project.totalDurationSec
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "StudioViewModel"
    private val context: Context = application.applicationContext
    private val engine = HyperFramesEngine(context)
    private val storageManager = VideoStorageManager(context)
    private val voiceoverService = VoiceoverAudioService(context)
    private val zipMediaManager = ZipMediaManager(context)
    val projectFileManager = HFProjectsStorageManager(context)

    private val undoStack = Stack<HyperFramesProject>()
    private val redoStack = Stack<HyperFramesProject>()

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var renderJob: Job? = null
    private var autosaveJob: Job? = null

    val jsBridge = HyperFramesJsBridge(
        onTimelineReadyListener = { durationSec, totalScenes, json ->
            Log.d(TAG, "JS Timeline ready: duration=$durationSec, scenes=$totalScenes")
        },
        onTickListener = { timeSec, prog, isPlay -> },
        onSceneChangeListener = { index, id, title ->
            _uiState.update { it.copy(activeSceneId = id) }
        },
        onLogListener = { lvl, msg ->
            Log.d(TAG, "[$lvl] $msg")
        }
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            projectFileManager.ensureStorageInitialized()
            val summaries = projectFileManager.listProjects()
            val firstSummary = summaries.firstOrNull()
            if (firstSummary != null) {
                try {
                    val loaded = projectFileManager.loadProject(firstSummary.dir)
                    val firstScene = loaded.scenes.firstOrNull()
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                project = loaded,
                                currentProjectDir = firstSummary.dir,
                                projectSummaries = summaries,
                                activeSceneId = firstScene?.id,
                                customHtml = firstScene?.composition?.customHtml ?: "",
                                customCss = firstScene?.composition?.customCss ?: "",
                                customJs = firstScene?.composition?.customJs ?: ""
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed loading initial project from disk", e)
                }
            }
        }
        refreshGallery()
        observeVoiceoverService()
        loadImportedAssets()
    }

    private fun pushUndo() {
        undoStack.push(_uiState.value.project)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.pop()
            redoStack.push(_uiState.value.project)
            _uiState.update {
                it.copy(
                    project = prev,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(_uiState.value.project)
            _uiState.update {
                it.copy(
                    project = next,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    // --- Scene Management ---

    fun selectScene(sceneId: String) {
        val startSec = _uiState.value.project.getSceneStartTime(sceneId)
        val scene = _uiState.value.project.scenes.find { it.id == sceneId }
        _uiState.update {
            it.copy(
                activeSceneId = sceneId,
                selectedElementId = null,
                currentTimeSec = startSec,
                customHtml = scene?.composition?.customHtml ?: "",
                customCss = scene?.composition?.customCss ?: "",
                customJs = scene?.composition?.customJs ?: ""
            )
        }
    }

    fun addScene() {
        pushUndo()
        val currentScenes = _uiState.value.project.scenes
        val newIndex = currentScenes.size
        val newSceneId = "scene-${String.format("%02d", newIndex + 1)}"
        val title = "Сцена ${newIndex + 1}"
        val initialHtml = """
            <div id="stage">
                <div class="scene-container">
                    <h1 class="title">$title</h1>
                </div>
            </div>
        """.trimIndent()
        val initialCss = """
            #stage {
                position: absolute;
                inset: 0;
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #0c0e14;
            }
            .title {
                font-family: 'Montserrat', sans-serif;
                font-size: 48px;
                font-weight: 800;
                color: #ffffff;
            }
        """.trimIndent()
        val initialJs = """console.log("$title initialized");"""

        val newScene = HyperFrameScene(
            id = newSceneId,
            index = newIndex,
            title = title,
            durationSec = 5.0f,
            transition = SceneTransitionType.FADE,
            composition = SceneComposition(
                customHtml = initialHtml,
                customCss = initialCss,
                customJs = initialJs
            )
        )

        val updatedScenes = currentScenes + newScene
        val updatedProject = _uiState.value.project.copy(scenes = updatedScenes)
        _uiState.update {
            it.copy(
                project = updatedProject,
                activeSceneId = newScene.id,
                customHtml = initialHtml,
                customCss = initialCss,
                customJs = initialJs
            )
        }
        triggerDebouncedAutosave()
    }

    fun duplicateScene(sceneId: String) {
        val scene = _uiState.value.project.scenes.find { it.id == sceneId } ?: return
        pushUndo()
        val currentScenes = _uiState.value.project.scenes
        val index = currentScenes.indexOf(scene)
        val copy = scene.copy(
            id = "scene_${UUID.randomUUID().toString().take(8)}",
            title = "${scene.title} (Копия)",
            elements = scene.elements.map { it.copy(id = "el_${UUID.randomUUID().toString().take(6)}") }
        )
        val updated = currentScenes.toMutableList().apply { add(index + 1, copy) }
        _uiState.update {
            it.copy(
                project = it.project.copy(scenes = updated),
                activeSceneId = copy.id
            )
        }
    }

    fun deleteScene(sceneId: String) {
        val currentScenes = _uiState.value.project.scenes
        if (currentScenes.size <= 1) return // Keep at least one scene
        pushUndo()
        val updated = currentScenes.filterNot { it.id == sceneId }
        val nextActive = updated.firstOrNull()?.id
        _uiState.update {
            it.copy(
                project = it.project.copy(scenes = updated),
                activeSceneId = nextActive,
                selectedElementId = null
            )
        }
    }

    fun moveSceneUp(sceneId: String) {
        val currentScenes = _uiState.value.project.scenes.toMutableList()
        val index = currentScenes.indexOfFirst { it.id == sceneId }
        if (index > 0) {
            pushUndo()
            val temp = currentScenes[index]
            currentScenes[index] = currentScenes[index - 1]
            currentScenes[index - 1] = temp
            _uiState.update { it.copy(project = it.project.copy(scenes = currentScenes)) }
        }
    }

    fun moveSceneDown(sceneId: String) {
        val currentScenes = _uiState.value.project.scenes.toMutableList()
        val index = currentScenes.indexOfFirst { it.id == sceneId }
        if (index >= 0 && index < currentScenes.size - 1) {
            pushUndo()
            val temp = currentScenes[index]
            currentScenes[index] = currentScenes[index + 1]
            currentScenes[index + 1] = temp
            _uiState.update { it.copy(project = it.project.copy(scenes = currentScenes)) }
        }
    }

    fun updateSceneTitle(sceneId: String, newTitle: String) {
        _uiState.update { state ->
            val updated = state.project.scenes.map { if (it.id == sceneId) it.copy(title = newTitle) else it }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun updateSceneScript(sceneId: String, newScript: String) {
        _uiState.update { state ->
            val updated = state.project.scenes.map { if (it.id == sceneId) it.copy(script = newScript) else it }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun updateSceneDuration(sceneId: String, durationSec: Float) {
        val validDuration = durationSec.coerceIn(0.5f, 30.0f)
        _uiState.update { state ->
            val updated = state.project.scenes.map { if (it.id == sceneId) it.copy(durationSec = validDuration) else it }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun changeSceneDurationDelta(sceneId: String, deltaSec: Float) {
        val scene = _uiState.value.project.scenes.find { it.id == sceneId } ?: return
        updateSceneDuration(sceneId, scene.durationSec + deltaSec)
    }

    fun updateSceneTransition(sceneId: String, transition: SceneTransitionType) {
        pushUndo()
        _uiState.update { state ->
            val updated = state.project.scenes.map { if (it.id == sceneId) it.copy(transition = transition) else it }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun updateSceneBackground(sceneId: String, bgGradient: String) {
        pushUndo()
        _uiState.update { state ->
            val updated = state.project.scenes.map {
                if (it.id == sceneId) it.copy(composition = it.composition.copy(backgroundGradient = bgGradient)) else it
            }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun updateSceneAvatar(sceneId: String, avatar: SceneAvatarSettings) {
        _uiState.update { state ->
            val updated = state.project.scenes.map { if (it.id == sceneId) it.copy(avatar = avatar) else it }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun updateSceneVoice(sceneId: String, voice: SceneVoiceSettings) {
        _uiState.update { state ->
            val updated = state.project.scenes.map { if (it.id == sceneId) it.copy(voice = voice) else it }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    fun toggleSceneAvatar(sceneId: String, enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.project.scenes.map {
                if (it.id == sceneId) it.copy(avatar = it.avatar.copy(isEnabled = enabled)) else it
            }
            state.copy(project = state.project.copy(scenes = updated))
        }
    }

    // --- Element Management ---

    fun selectElement(elementId: String?) {
        _uiState.update {
            it.copy(
                selectedElementId = elementId,
                activePanel = if (elementId != null) StudioActivePanel.INSPECTOR else it.activePanel
            )
        }
    }

    fun addTextElementToActiveScene(
        text: String = "Новый заголовок",
        colorHex: String = "#FFFFFF",
        fontSizeSp: Int = 36
    ) {
        val activeId = _uiState.value.activeSceneId ?: return
        pushUndo()
        val newEl = HyperFrameElement(
            id = "el_${UUID.randomUUID().toString().take(6)}",
            type = ElementType.TEXT,
            name = "Текст",
            textContent = text,
            fontSizeSp = fontSizeSp,
            textColorHex = colorHex,
            transform = ElementTransform(xPercent = 50f, yPercent = 50f, scale = 1.0f),
            animation = HyperFrameAnimation(type = MediaAnimationType.ZOOM_IN)
        )
        _uiState.update { state ->
            val updatedScenes = state.project.scenes.map { sc ->
                if (sc.id == activeId) sc.copy(elements = sc.elements + newEl) else sc
            }
            state.copy(
                project = state.project.copy(scenes = updatedScenes),
                selectedElementId = newEl.id,
                activePanel = StudioActivePanel.INSPECTOR
            )
        }
    }

    fun addImageElementToActiveScene(uri: String, name: String = "Изображение") {
        val activeId = _uiState.value.activeSceneId ?: return
        pushUndo()
        val newEl = HyperFrameElement(
            id = "el_${UUID.randomUUID().toString().take(6)}",
            type = ElementType.IMAGE,
            name = name,
            sourceUri = uri,
            transform = ElementTransform(xPercent = 50f, yPercent = 50f, scale = 1.0f),
            animation = HyperFrameAnimation(type = MediaAnimationType.FADE)
        )
        _uiState.update { state ->
            val updatedScenes = state.project.scenes.map { sc ->
                if (sc.id == activeId) sc.copy(elements = sc.elements + newEl) else sc
            }
            state.copy(
                project = state.project.copy(scenes = updatedScenes),
                selectedElementId = newEl.id,
                activePanel = StudioActivePanel.INSPECTOR
            )
        }
    }

    fun addEffectElementToActiveScene(effectType: VideoEffectType) {
        val activeId = _uiState.value.activeSceneId ?: return
        pushUndo()
        val newEl = HyperFrameElement(
            id = "el_${UUID.randomUUID().toString().take(6)}",
            type = ElementType.EFFECT,
            name = effectType.title.split("(").first().trim(),
            effectType = effectType,
            transform = ElementTransform(opacity = 0.8f)
        )
        _uiState.update { state ->
            val updatedScenes = state.project.scenes.map { sc ->
                if (sc.id == activeId) sc.copy(elements = sc.elements + newEl) else sc
            }
            state.copy(
                project = state.project.copy(scenes = updatedScenes),
                selectedElementId = newEl.id,
                activePanel = StudioActivePanel.INSPECTOR
            )
        }
    }

    fun updateElement(element: HyperFrameElement) {
        val activeId = _uiState.value.activeSceneId ?: return
        _uiState.update { state ->
            val updatedScenes = state.project.scenes.map { sc ->
                if (sc.id == activeId) {
                    sc.copy(elements = sc.elements.map { if (it.id == element.id) element else it })
                } else sc
            }
            state.copy(project = state.project.copy(scenes = updatedScenes))
        }
    }

    fun deleteElement(elementId: String) {
        val activeId = _uiState.value.activeSceneId ?: return
        pushUndo()
        _uiState.update { state ->
            val updatedScenes = state.project.scenes.map { sc ->
                if (sc.id == activeId) {
                    sc.copy(elements = sc.elements.filterNot { it.id == elementId })
                } else sc
            }
            state.copy(
                project = state.project.copy(scenes = updatedScenes),
                selectedElementId = null
            )
        }
    }

    fun addAssetToActiveScene(asset: MediaAssetItem) {
        when (asset.category) {
            MediaCategoryType.IMAGES -> addImageElementToActiveScene(asset.uri, asset.name)
            MediaCategoryType.VIDEOS -> {
                val activeId = _uiState.value.activeSceneId ?: return
                pushUndo()
                val newEl = HyperFrameElement(
                    id = "el_${UUID.randomUUID().toString().take(6)}",
                    type = ElementType.VIDEO,
                    name = asset.name,
                    sourceUri = asset.uri,
                    transform = ElementTransform(xPercent = 50f, yPercent = 50f, scale = 1.0f)
                )
                _uiState.update { state ->
                    val updatedScenes = state.project.scenes.map { sc ->
                        if (sc.id == activeId) sc.copy(elements = sc.elements + newEl) else sc
                    }
                    state.copy(
                        project = state.project.copy(scenes = updatedScenes),
                        selectedElementId = newEl.id,
                        activePanel = StudioActivePanel.INSPECTOR
                    )
                }
            }
            MediaCategoryType.AUDIO -> {
                val activeId = _uiState.value.activeSceneId ?: return
                pushUndo()
                _uiState.update { state ->
                    val updatedScenes = state.project.scenes.map { sc ->
                        if (sc.id == activeId) sc.copy(voice = sc.voice.copy(audioPath = asset.uri)) else sc
                    }
                    state.copy(project = state.project.copy(scenes = updatedScenes))
                }
            }
        }
    }

    // --- Studio Navigation & Panels ---

    fun selectActivePanel(panel: StudioActivePanel) {
        _uiState.update { it.copy(activePanel = panel) }
    }

    fun createNewProject(
        name: String = "My Project",
        aspectRatio: AspectRatioType = AspectRatioType.PORTRAIT_9_16,
        resolution: RenderResolution = RenderResolution.HD_720P
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newProj = projectFileManager.createNewProject(name, aspectRatio, resolution)
                val summaries = projectFileManager.listProjects()
                val targetDir = summaries.find { it.name == name }?.dir
                val firstScene = newProj.scenes.firstOrNull()
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            project = newProj,
                            currentProjectDir = targetDir,
                            projectSummaries = summaries,
                            activeSceneId = firstScene?.id,
                            customHtml = firstScene?.composition?.customHtml ?: "",
                            customCss = firstScene?.composition?.customCss ?: "",
                            customJs = firstScene?.composition?.customJs ?: "",
                            currentTimeSec = 0f,
                            isPlaying = false,
                            isCodeModified = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating new project $name", e)
            }
        }
    }

    fun openProject(dir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loaded = projectFileManager.loadProject(dir)
                val firstScene = loaded.scenes.firstOrNull()
                val summaries = projectFileManager.listProjects()
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            project = loaded,
                            currentProjectDir = dir,
                            projectSummaries = summaries,
                            activeSceneId = firstScene?.id,
                            customHtml = firstScene?.composition?.customHtml ?: "",
                            customCss = firstScene?.composition?.customCss ?: "",
                            customJs = firstScene?.composition?.customJs ?: "",
                            currentTimeSec = 0f,
                            isPlaying = false,
                            isCodeModified = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening project ${dir.name}", e)
            }
        }
    }

    fun setProjectManagerOpen(open: Boolean) {
        _uiState.update { it.copy(isProjectManagerOpen = open) }
        if (open) refreshProjectsList()
    }

    fun refreshProjectsList() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = projectFileManager.listProjects()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(projectSummaries = list) }
            }
        }
    }

    fun duplicateProject(dir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                projectFileManager.duplicateProject(dir)
                refreshProjectsList()
            } catch (e: Exception) {
                Log.e(TAG, "Error duplicating project ${dir.name}", e)
            }
        }
    }

    fun renameProject(dir: File, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newDir = projectFileManager.renameProject(dir, newName)
                if (_uiState.value.currentProjectDir?.absolutePath == dir.absolutePath) {
                    openProject(newDir)
                } else {
                    refreshProjectsList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming project ${dir.name}", e)
            }
        }
    }

    fun deleteProject(dir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                projectFileManager.deleteProject(dir)
                val remaining = projectFileManager.listProjects()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(projectSummaries = remaining) }
                    if (_uiState.value.currentProjectDir?.absolutePath == dir.absolutePath) {
                        val next = remaining.firstOrNull()
                        if (next != null) {
                            openProject(next.dir)
                        } else {
                            createNewProject()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting project ${dir.name}", e)
            }
        }
    }

    fun exportProjectToHfp(dir: File): File? {
        return try {
            val exportDir = File(projectFileManager.getHfProjectsRoot(), "exports").apply { mkdirs() }
            val targetFile = File(exportDir, "${dir.name}.hfp")
            projectFileManager.exportProjectToHfp(dir, targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting .hfp for ${dir.name}", e)
            null
        }
    }

    fun importProjectFromHfp(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val importedDir = projectFileManager.importProjectFromHfp(file)
                openProject(importedDir)
            } catch (e: Exception) {
                Log.e(TAG, "Error importing project from ${file.name}", e)
            }
        }
    }

    fun saveCurrentProjectToDisk() {
        val dir = _uiState.value.currentProjectDir ?: return
        val proj = _uiState.value.project
        viewModelScope.launch(Dispatchers.IO) {
            try {
                projectFileManager.saveProject(proj, dir)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isCodeModified = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving project to disk", e)
            }
        }
    }

    fun saveCurrentSceneCodeNow() {
        autosaveJob?.cancel()
        val dir = _uiState.value.currentProjectDir
        val sceneId = _uiState.value.activeSceneId
        val state = _uiState.value
        if (dir != null && sceneId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    projectFileManager.saveSceneCodeFiles(dir, sceneId, state.customHtml, state.customCss, state.customJs)
                    projectFileManager.saveProject(state.project, dir)
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isCodeModified = false) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed saving scene code files", e)
                }
            }
        }
    }

    fun triggerDebouncedAutosave() {
        _uiState.update { it.copy(isCodeModified = true) }
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(600) // 600ms debounce
            val dir = _uiState.value.currentProjectDir ?: return@launch
            val sceneId = _uiState.value.activeSceneId ?: return@launch
            val state = _uiState.value
            try {
                projectFileManager.saveSceneCodeFiles(dir, sceneId, state.customHtml, state.customCss, state.customJs)
                projectFileManager.saveProject(state.project, dir)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isCodeModified = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Autosave failed", e)
            }
        }
    }

    fun setProjectName(name: String) {
        _uiState.update { it.copy(project = it.project.copy(name = name)) }
        triggerDebouncedAutosave()
    }

    fun setAspectRatio(ratio: AspectRatioType) {
        pushUndo()
        _uiState.update { it.copy(project = it.project.copy(aspectRatio = ratio)) }
        triggerDebouncedAutosave()
    }

    fun setResolution(res: RenderResolution) {
        _uiState.update { it.copy(project = it.project.copy(resolution = res)) }
        triggerDebouncedAutosave()
    }

    fun openCodeEditor(open: Boolean) {
        if (open) {
            val scene = _uiState.value.activeScene
            _uiState.update {
                it.copy(
                    isCodeEditorOpen = true,
                    customHtml = scene?.composition?.customHtml ?: "",
                    customCss = scene?.composition?.customCss ?: "",
                    customJs = scene?.composition?.customJs ?: ""
                )
            }
        } else {
            _uiState.update { it.copy(isCodeEditorOpen = false) }
        }
    }

    fun openCodeEditorForFile(sceneId: String, tabIndex: Int) {
        selectScene(sceneId)
        openCodeEditor(true)
    }

    fun openGallery(open: Boolean) {
        _uiState.update { it.copy(isGalleryOpen = open) }
        if (open) refreshGallery()
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setShowExplainer(show: Boolean) {
        _uiState.update { it.copy(showExplainerDialog = show) }
    }

    // --- Playback Engine ---

    fun togglePlay() {
        if (_uiState.value.isPlaying) pause() else play()
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }

        playbackJob = viewModelScope.launch {
            val speed = _uiState.value.playbackSpeed
            var initialTime = _uiState.value.currentTimeSec
            val totalDuration = _uiState.value.totalDurationSec
            if (initialTime >= totalDuration - 0.05f) {
                initialTime = 0f
            }

            val startEpoch = System.currentTimeMillis() - ((initialTime / speed) * 1000L).toLong()

            while (isActive && _uiState.value.isPlaying) {
                val curSpeed = _uiState.value.playbackSpeed
                val elapsedSec = ((System.currentTimeMillis() - startEpoch) / 1000f) * curSpeed
                val dur = _uiState.value.totalDurationSec

                if (elapsedSec >= dur) {
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
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(timeSec: Float) {
        val dur = _uiState.value.totalDurationSec
        val clamped = timeSec.coerceIn(0f, dur)
        val activeScene = _uiState.value.project.getSceneAtTime(clamped)
        _uiState.update {
            it.copy(
                currentTimeSec = clamped,
                activeSceneId = activeScene?.id ?: it.activeSceneId
            )
        }
    }

    fun stepFrame(deltaFrames: Int) {
        val state = _uiState.value
        val frameDuration = 1f / state.project.fps.toFloat()
        val nextTime = (state.currentTimeSec + deltaFrames * frameDuration).coerceIn(0f, state.totalDurationSec)
        seekTo(nextTime)
    }

    // --- Voiceover Recording ---

    private fun observeVoiceoverService() {
        viewModelScope.launch {
            voiceoverService.isRecording.collectLatest { isRec ->
                _uiState.update { it.copy(isRecordingVoiceover = isRec) }
            }
        }
        viewModelScope.launch {
            voiceoverService.recordingDurationSec.collectLatest { dur ->
                _uiState.update { it.copy(recordingDurationSec = dur) }
            }
        }
    }

    fun startVoiceoverRecording() {
        voiceoverService.startRecording(viewModelScope)
    }

    fun stopVoiceoverRecording() {
        val file = voiceoverService.stopRecording()
        if (file != null && file.exists()) {
            val activeId = _uiState.value.activeSceneId ?: return
            _uiState.update { state ->
                val updatedScenes = state.project.scenes.map { sc ->
                    if (sc.id == activeId) sc.copy(voice = sc.voice.copy(audioPath = file.absolutePath)) else sc
                }
                state.copy(project = state.project.copy(scenes = updatedScenes))
            }
        }
    }

    fun playRecordedVoiceover() {
        val path = _uiState.value.activeScene?.voice?.audioPath ?: return
        voiceoverService.playAudio(path)
    }

    // --- Media & ZIP Hub ---

    fun loadImportedAssets() {
        viewModelScope.launch {
            val list = zipMediaManager.loadAllImportedAssets()
            _uiState.update { it.copy(importedAssets = list) }
        }
    }

    fun importZipArchive(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingZip = true) }
            zipMediaManager.unpackZip(uri)
            val all = zipMediaManager.loadAllImportedAssets()
            _uiState.update { it.copy(importedAssets = all, isImportingZip = false) }
        }
    }

    fun importMediaFiles(uris: List<Uri>) {
        viewModelScope.launch {
            zipMediaManager.importFiles(uris)
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

    // --- Code IDE ---

    fun updateCustomCode(html: String, css: String, js: String) {
        val activeId = _uiState.value.activeSceneId ?: _uiState.value.project.scenes.firstOrNull()?.id
        _uiState.update { current ->
            val updatedScenes = current.project.scenes.map { sc ->
                if (sc.id == activeId) {
                    sc.copy(
                        composition = sc.composition.copy(
                            customHtml = html,
                            customCss = css,
                            customJs = js
                        )
                    )
                } else sc
            }
            current.copy(
                customHtml = html,
                customCss = css,
                customJs = js,
                isCustomCodeActive = true,
                project = current.project.copy(scenes = updatedScenes)
            )
        }
        triggerDebouncedAutosave()
    }

    fun resetCustomCode() {
        _uiState.update {
            it.copy(
                isCustomCodeActive = false,
                customHtml = "",
                customCss = "",
                customJs = ""
            )
        }
    }

    // --- Render & Export ---

    fun startRender() {
        pause()
        val state = _uiState.value
        val compiledHtml = getCompiledHtmlForPreview()

        val config = RenderConfiguration(
            projectId = state.project.id,
            projectName = state.project.name,
            durationSec = state.totalDurationSec,
            fps = state.project.fps,
            resolution = state.project.resolution,
            aspectRatio = state.project.aspectRatio,
            customHtml = compiledHtml,
            customCss = "",
            customJs = "",
            paramsMap = emptyMap(),
            mediaOverlays = emptyList()
        )

        _uiState.update { it.copy(showRenderDialog = true, renderState = RenderState.Idle) }

        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            engine.renderVideo(config).collectLatest { status ->
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

    // --- HTML Compilation ---

    fun getCompiledHtmlForPreview(): String {
        val state = _uiState.value
        if (state.isCustomCodeActive && state.customHtml.isNotBlank()) {
            return state.customHtml
        }

        return ProjectHtmlCompiler.compile(
            project = state.project
        )
    }
}
