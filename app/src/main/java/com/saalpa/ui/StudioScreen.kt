package com.saalpa.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.saalpa.ui.components.ArchitectureExplainerModal
import com.saalpa.ui.components.CanvasWorkspace
import com.saalpa.ui.components.CodeEditor
import com.saalpa.ui.components.ExportProgressModal
import com.saalpa.ui.components.GalleryView
import com.saalpa.ui.components.HyperFramesStudioLayout
import com.saalpa.ui.components.ProjectManagerDialog
import com.saalpa.ui.theme.StudioBg
import java.io.File

@Composable
fun StudioScreen(
    viewModel: StudioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Media File Picker (photos, videos, audio)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMediaFiles(uris)
            Toast.makeText(context, "Импортировано файлов: ${uris.size}", Toast.LENGTH_SHORT).show()
        }
    }

    // ZIP Archive Picker
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importZipArchive(uri)
            Toast.makeText(context, "Распаковка ZIP архива...", Toast.LENGTH_SHORT).show()
        }
    }

    // HFP Project File Import Picker
    val hfpPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val tempFile = File(context.cacheDir, "import_${System.currentTimeMillis()}.hfp")
                    tempFile.outputStream().use { out -> inputStream.copyTo(out) }
                    viewModel.importProjectFromHfp(tempFile)
                    Toast.makeText(context, "Проект импортирован", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка импорта: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startVoiceoverRecording()
        } else {
            Toast.makeText(context, "Требуется разрешение на запись аудио", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (uiState.isCodeEditorOpen) {
            // Full Screen Code IDE with tabs (HTML, CSS, JS) and Live Preview
            CodeEditor(
                sceneId = uiState.activeSceneId ?: "scene-01",
                sceneTitle = uiState.activeScene?.title ?: "Сцена 1",
                scenes = uiState.project.scenes,
                html = uiState.customHtml,
                css = uiState.customCss,
                js = uiState.customJs,
                isModified = uiState.isCodeModified,
                onCodeChange = { h, c, j -> viewModel.updateCustomCode(h, c, j) },
                onSaveCode = { viewModel.saveCurrentSceneCodeNow() },
                onSelectScene = { viewModel.selectScene(it) },
                onBackToStudio = { viewModel.openCodeEditor(false) },
                previewContent = {
                    CanvasWorkspace(
                        htmlContent = viewModel.getCompiledHtmlForPreview(),
                        aspectRatio = uiState.project.aspectRatio,
                        currentTimeSec = uiState.currentTimeSec,
                        totalDurationSec = uiState.totalDurationSec,
                        activeScene = uiState.activeScene,
                        isPlaying = uiState.isPlaying,
                        onTogglePlay = { viewModel.togglePlay() },
                        jsBridge = viewModel.jsBridge
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (uiState.isGalleryOpen) {
            // Video Gallery Screen
            GalleryView(
                videos = uiState.savedVideos,
                selectedVideo = uiState.selectedGalleryVideo,
                onSelectVideo = { viewModel.selectGalleryVideo(it) },
                onShareVideo = { viewModel.shareVideo(it) },
                onDeleteVideo = { viewModel.deleteVideo(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Main Creative Studio Screen
            HyperFramesStudioLayout(
                state = uiState,
                compiledHtml = viewModel.getCompiledHtmlForPreview(),
                jsBridge = viewModel.jsBridge,
                // Top bar actions
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onNewProject = { viewModel.createNewProject() },
                onSelectAspectRatio = { viewModel.setAspectRatio(it) },
                onOpenCodeEditor = { viewModel.openCodeEditor(true) },
                onOpenProjectManager = { viewModel.setProjectManagerOpen(true) },
                onOpenCodeFile = { sceneId, tabIndex -> viewModel.openCodeEditorForFile(sceneId, tabIndex) },
                onOpenGallery = { viewModel.openGallery(true) },
                onOpenExplainer = { viewModel.setShowExplainer(true) },
                onExportClick = { viewModel.startRender() },
                // Panel tab switcher
                onSelectPanel = { viewModel.selectActivePanel(it) },
                // Playback
                onTogglePlay = { viewModel.togglePlay() },
                onSeek = { viewModel.seekTo(it) },
                // Scene actions
                onSelectScene = { viewModel.selectScene(it) },
                onAddScene = { viewModel.addScene() },
                onDuplicateScene = { viewModel.duplicateScene(it) },
                onDeleteScene = { viewModel.deleteScene(it) },
                onMoveSceneUp = { viewModel.moveSceneUp(it) },
                onMoveSceneDown = { viewModel.moveSceneDown(it) },
                onUpdateSceneTitle = { id, title -> viewModel.updateSceneTitle(id, title) },
                onUpdateSceneScript = { id, script -> viewModel.updateSceneScript(id, script) },
                onChangeSceneDuration = { id, delta -> viewModel.changeSceneDurationDelta(id, delta) },
                onSetSceneDuration = { dur -> uiState.activeSceneId?.let { viewModel.updateSceneDuration(it, dur) } },
                onChangeTransition = { id, trans -> viewModel.updateSceneTransition(id, trans) },
                onUpdateSceneBg = { bg -> uiState.activeSceneId?.let { viewModel.updateSceneBackground(it, bg) } },
                onToggleSceneAvatar = { enabled -> uiState.activeSceneId?.let { viewModel.toggleSceneAvatar(it, enabled) } },
                onUpdateSceneAvatar = { av -> uiState.activeSceneId?.let { viewModel.updateSceneAvatar(it, av) } },
                onUpdateSceneVoice = { vc -> uiState.activeSceneId?.let { viewModel.updateSceneVoice(it, vc) } },
                // Element actions
                onSelectElement = { viewModel.selectElement(it) },
                onUpdateElement = { viewModel.updateElement(it) },
                onDeleteElement = { viewModel.deleteElement(it) },
                onAddTextElement = { viewModel.addTextElementToActiveScene() },
                onAddImageElement = { mediaPickerLauncher.launch("image/*") },
                onAddEffectElement = { viewModel.addEffectElementToActiveScene(it) },
                // Media actions
                onImportLocalMedia = { mediaPickerLauncher.launch("*/*") },
                onImportZipArchive = { zipPickerLauncher.launch("application/zip") },
                onAddAssetToScene = { viewModel.addAssetToActiveScene(it) },
                // Voice recording
                onStartRecordingVoiceover = {
                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        viewModel.startVoiceoverRecording()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecordingVoiceover = { viewModel.stopVoiceoverRecording() },
                onPlayRecordedVoiceover = { viewModel.playRecordedVoiceover() }
            )
        }

        // Project Manager Modal Dialog
        if (uiState.isProjectManagerOpen) {
            ProjectManagerDialog(
                projects = uiState.projectSummaries,
                currentProjectDir = uiState.currentProjectDir,
                onDismiss = { viewModel.setProjectManagerOpen(false) },
                onOpenProject = { viewModel.openProject(it) },
                onCreateNewProject = { name, ratio, res -> viewModel.createNewProject(name, ratio, res) },
                onDuplicateProject = { viewModel.duplicateProject(it) },
                onRenameProject = { dir, newName -> viewModel.renameProject(dir, newName) },
                onDeleteProject = { viewModel.deleteProject(it) },
                onExportHfp = { dir ->
                    val exported = viewModel.exportProjectToHfp(dir)
                    if (exported != null) {
                        Toast.makeText(context, "Экспортировано: ${exported.name}", Toast.LENGTH_LONG).show()
                    }
                },
                onImportHfp = { hfpPickerLauncher.launch("*/*") }
            )
        }

        // Export / Render Progress Modal
        if (uiState.showRenderDialog) {
            ExportProgressModal(
                renderState = uiState.renderState,
                onCancel = { viewModel.cancelRender() },
                onDismiss = { viewModel.dismissRenderDialog() },
                onOpenGallery = {
                    viewModel.dismissRenderDialog()
                    viewModel.openGallery(true)
                },
                onShareVideo = { video -> viewModel.shareVideo(video) }
            )
        }

        // Architecture Explainer Modal
        if (uiState.showExplainerDialog) {
            ArchitectureExplainerModal(
                onDismiss = { viewModel.setShowExplainer(false) }
            )
        }
    }
}
