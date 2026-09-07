package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saalpa.engine.HyperFramesJsBridge
import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAssetItem
import com.saalpa.model.MediaCategoryType
import com.saalpa.model.RenderResolution
import com.saalpa.model.VideoEffectType
import com.saalpa.model.project.HyperFrameElement
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.HyperFramesProject
import com.saalpa.model.project.SceneAvatarSettings
import com.saalpa.model.project.SceneTransitionType
import com.saalpa.model.project.SceneVoiceSettings
import com.saalpa.ui.StudioUiState
import com.saalpa.ui.theme.StudioBg
import com.saalpa.ui.theme.StudioBorder

@Composable
fun HyperFramesStudioLayout(
    state: StudioUiState,
    compiledHtml: String,
    jsBridge: HyperFramesJsBridge,
    // Top bar actions
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNewProject: () -> Unit = {},
    onSelectAspectRatio: (AspectRatioType) -> Unit,
    onOpenCodeEditor: () -> Unit,
    onOpenProjectManager: () -> Unit = {},
    onOpenCodeFile: (sceneId: String, tabIndex: Int) -> Unit = { _, _ -> },
    onOpenGallery: () -> Unit,
    onOpenExplainer: () -> Unit,
    onExportClick: () -> Unit,
    // Panel tab switcher
    onSelectPanel: (StudioActivePanel) -> Unit,
    // Playback
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    // Scene actions
    onSelectScene: (String) -> Unit,
    onAddScene: () -> Unit,
    onDuplicateScene: (String) -> Unit,
    onDeleteScene: (String) -> Unit,
    onMoveSceneUp: (String) -> Unit,
    onMoveSceneDown: (String) -> Unit,
    onUpdateSceneTitle: (sceneId: String, title: String) -> Unit,
    onUpdateSceneScript: (sceneId: String, script: String) -> Unit,
    onChangeSceneDuration: (sceneId: String, deltaSec: Float) -> Unit,
    onSetSceneDuration: (Float) -> Unit,
    onChangeTransition: (sceneId: String, transition: SceneTransitionType) -> Unit,
    onUpdateSceneBg: (String) -> Unit,
    onToggleSceneAvatar: (Boolean) -> Unit = {},
    onUpdateSceneAvatar: ((SceneAvatarSettings) -> Unit)? = null,
    onUpdateSceneVoice: (SceneVoiceSettings) -> Unit,
    // Element actions
    onSelectElement: (String?) -> Unit,
    onUpdateElement: (HyperFrameElement) -> Unit,
    onDeleteElement: (String) -> Unit,
    onAddTextElement: () -> Unit,
    onAddImageElement: () -> Unit,
    onAddEffectElement: (VideoEffectType) -> Unit,
    // Media actions
    onImportLocalMedia: () -> Unit,
    onImportZipArchive: () -> Unit,
    onAddAssetToScene: (MediaAssetItem) -> Unit,
    // Voice recording
    onStartRecordingVoiceover: () -> Unit,
    onStopRecordingVoiceover: () -> Unit,
    onPlayRecordedVoiceover: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeScene = state.activeScene
    val selectedElement = state.selectedElement

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        val isWide = maxWidth > 720.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Modern Top Bar
            HyperFramesTopBar(
                projectName = state.project.name,
                aspectRatio = state.project.aspectRatio,
                isPlaying = state.isPlaying,
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onTogglePlay = onTogglePlay,
                onNewClick = onAddScene,
                onSelectAspectRatio = onSelectAspectRatio,
                onOpenCodeEditor = onOpenCodeEditor,
                onOpenProjectManager = onOpenProjectManager,
                onExportClick = onExportClick
            )

            if (isWide) {
                // LANDSCAPE / TABLET LAYOUT: 3-column split + bottom Scene Timeline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left Column: Script & Scenes Panel (30%)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.30f)
                    ) {
                        ScriptScenePanel(
                            project = state.project,
                            activeSceneId = state.activeSceneId,
                            onSelectScene = onSelectScene,
                            onAddScene = onAddScene,
                            onDuplicateScene = onDuplicateScene,
                            onDeleteScene = onDeleteScene,
                            onMoveSceneUp = onMoveSceneUp,
                            onMoveSceneDown = onMoveSceneDown,
                            onUpdateSceneTitle = onUpdateSceneTitle,
                            onUpdateSceneScript = onUpdateSceneScript,
                            onOpenVoicePanel = { onSelectPanel(StudioActivePanel.VOICE) },
                            onOpenAvatarPanel = { onSelectPanel(StudioActivePanel.VOICE) }
                        )
                    }

                    // Divider
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(StudioBorder))

                    // Center Column: Canvas Workspace (42%)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.42f)
                    ) {
                        CanvasWorkspace(
                            htmlContent = compiledHtml,
                            aspectRatio = state.project.aspectRatio,
                            currentTimeSec = state.currentTimeSec,
                            totalDurationSec = state.totalDurationSec,
                            activeScene = activeScene,
                            isPlaying = state.isPlaying,
                            onTogglePlay = onTogglePlay,
                            jsBridge = jsBridge
                        )
                    }

                    // Divider
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(StudioBorder))

                    // Right Column: Inspector Panel (28%)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.28f)
                    ) {
                        InspectorPanel(
                            scene = activeScene,
                            selectedElement = selectedElement,
                            onUpdateSceneDuration = onSetSceneDuration,
                            onUpdateSceneBg = onUpdateSceneBg,
                            onUpdateSceneTransition = { onChangeTransition(activeScene?.id ?: "", it) },
                            onToggleSceneAvatar = onToggleSceneAvatar,
                            onUpdateElement = onUpdateElement,
                            onDeleteElement = onDeleteElement,
                            onAddTextElement = onAddTextElement,
                            onAddImageElement = onAddImageElement,
                            onAddEffectElement = onAddEffectElement,
                            onDeselectElement = { onSelectElement(null) }
                        )
                    }
                }
            } else {
                // PORTRAIT PHONE LAYOUT: Stacked with Tab Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Canvas Workspace (Upper half)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.46f)
                    ) {
                        CanvasWorkspace(
                            htmlContent = compiledHtml,
                            aspectRatio = state.project.aspectRatio,
                            currentTimeSec = state.currentTimeSec,
                            totalDurationSec = state.totalDurationSec,
                            activeScene = activeScene,
                            isPlaying = state.isPlaying,
                            onTogglePlay = onTogglePlay,
                            jsBridge = jsBridge
                        )
                    }

                    // Studio Panel Tab Switcher
                    StudioPanelTabs(
                        activePanel = state.activePanel,
                        onSelectPanel = onSelectPanel
                    )

                    // Active Panel Container (Lower half)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.54f)
                    ) {
                        when (state.activePanel) {
                            StudioActivePanel.SCENES, StudioActivePanel.SCRIPT -> {
                                ScriptScenePanel(
                                    project = state.project,
                                    activeSceneId = state.activeSceneId,
                                    onSelectScene = onSelectScene,
                                    onAddScene = onAddScene,
                                    onDuplicateScene = onDuplicateScene,
                                    onDeleteScene = onDeleteScene,
                                    onMoveSceneUp = onMoveSceneUp,
                                    onMoveSceneDown = onMoveSceneDown,
                                    onUpdateSceneTitle = onUpdateSceneTitle,
                                    onUpdateSceneScript = onUpdateSceneScript,
                                    onOpenVoicePanel = { onSelectPanel(StudioActivePanel.VOICE) },
                                    onOpenAvatarPanel = { onSelectPanel(StudioActivePanel.VOICE) }
                                )
                            }
                            StudioActivePanel.FILES -> {
                                ProjectFilesView(
                                    project = state.project,
                                    currentProjectDir = state.currentProjectDir,
                                    onOpenCodeFile = onOpenCodeFile
                                )
                            }
                            StudioActivePanel.INSPECTOR -> {
                                InspectorPanel(
                                    scene = activeScene,
                                    selectedElement = selectedElement,
                                    onUpdateSceneDuration = onSetSceneDuration,
                                    onUpdateSceneBg = onUpdateSceneBg,
                                    onUpdateSceneTransition = { onChangeTransition(activeScene?.id ?: "", it) },
                                    onToggleSceneAvatar = onToggleSceneAvatar,
                                    onUpdateElement = onUpdateElement,
                                    onDeleteElement = onDeleteElement,
                                    onAddTextElement = onAddTextElement,
                                    onAddImageElement = onAddImageElement,
                                    onAddEffectElement = onAddEffectElement,
                                    onDeselectElement = { onSelectElement(null) }
                                )
                            }
                            StudioActivePanel.VOICE -> {
                                VoicePanel(
                                    scene = activeScene,
                                    onUpdateVoice = onUpdateSceneVoice,
                                    isRecordingVoiceover = state.isRecordingVoiceover,
                                    recordingDurationSec = state.recordingDurationSec,
                                    onStartRecordingVoiceover = onStartRecordingVoiceover,
                                    onStopRecordingVoiceover = onStopRecordingVoiceover,
                                    onPlayRecordedVoiceover = onPlayRecordedVoiceover
                                )
                            }
                            StudioActivePanel.MEDIA -> {
                                val mediaAssetItems = state.importedAssets.map { imp ->
                                    val cat = when (imp.mediaType) {
                                        com.saalpa.model.MediaType.PHOTO -> MediaCategoryType.IMAGES
                                        com.saalpa.model.MediaType.VIDEO -> MediaCategoryType.VIDEOS
                                        else -> MediaCategoryType.AUDIO
                                    }
                                    MediaAssetItem(
                                        id = imp.id,
                                        name = imp.name,
                                        uri = imp.dataUrl ?: imp.file.toURI().toString(),
                                        category = cat
                                    )
                                }
                                MediaPanel(
                                    importedAssets = mediaAssetItems,
                                    onImportLocalMedia = onImportLocalMedia,
                                    onImportZipArchive = onImportZipArchive,
                                    onAddAssetToScene = onAddAssetToScene
                                )
                            }
                        }
                    }
                }
            }

            // Bottom SCENE TIMELINE (Always pinned at the bottom)
            SceneTimeline(
                project = state.project,
                activeSceneId = state.activeSceneId,
                currentTimeSec = state.currentTimeSec,
                isPlaying = state.isPlaying,
                onTogglePlay = onTogglePlay,
                onSeek = onSeek,
                onSelectScene = onSelectScene,
                onAddScene = onAddScene,
                onChangeSceneDuration = onChangeSceneDuration,
                onChangeTransition = onChangeTransition
            )
        }
    }
}
