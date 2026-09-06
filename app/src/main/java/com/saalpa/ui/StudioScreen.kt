package com.saalpa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.ui.components.ArchitectureExplainerModal
import com.saalpa.ui.components.AudioDrawer
import com.saalpa.ui.components.CapCutBottomToolbar
import com.saalpa.ui.components.CapCutMultiTrackTimeline
import com.saalpa.ui.components.CodeEditor
import com.saalpa.ui.components.EditDrawer
import com.saalpa.ui.components.EffectsDrawer
import com.saalpa.ui.components.ExportProgressModal
import com.saalpa.ui.components.GalleryView
import com.saalpa.ui.components.OverlayDrawer
import com.saalpa.ui.components.RatioDrawer
import com.saalpa.ui.components.TextDrawer
import com.saalpa.ui.components.WebViewPreview
import com.saalpa.ui.theme.CapCutBg
import com.saalpa.ui.theme.CapCutCardBorder
import com.saalpa.ui.theme.CapCutCyan
import com.saalpa.ui.theme.CapCutSurface
import com.saalpa.ui.theme.CapCutSurfaceVariant
import com.saalpa.ui.theme.OnCapCutCyan
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary
import com.saalpa.ui.theme.ViewportDarkBg
import java.util.Locale

@Composable
fun StudioScreen(
    viewModel: StudioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CapCutBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CapCutTopBar(
                state = uiState,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onOpenRatio = { viewModel.selectTab(StudioTab.RATIO) },
                onOpenInfo = { viewModel.setShowExplainer(true) },
                onExportClick = { viewModel.startRender() }
            )
        },
        bottomBar = {
            CapCutBottomToolbar(
                activeTab = uiState.activeTab,
                onTabSelected = { viewModel.selectTab(it) },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CapCutBg)
        ) {
            // 1. CapCut Center Video Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.05f)
                    .background(ViewportDarkBg),
                contentAlignment = Alignment.Center
            ) {
                // Aspect-ratio bounded video frame
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp, horizontal = 12.dp)
                        .aspectRatio(uiState.aspectRatio.ratio)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black)
                        .border(1.dp, CapCutCardBorder, RoundedCornerShape(10.dp))
                        .shadow(12.dp)
                ) {
                    WebViewPreview(
                        compiledHtml = viewModel.getCompiledHtmlForPreview(),
                        currentTimeSec = uiState.currentTimeSec,
                        progress = uiState.progress,
                        isPlaying = uiState.isPlaying,
                        aspectRatio = uiState.aspectRatio,
                        jsBridge = viewModel.jsBridge,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video Timecode Badge Overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "%02d:%04.1f",
                                (uiState.currentTimeSec / 60).toInt(),
                                uiState.currentTimeSec % 60
                            ),
                            color = CapCutCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 2. CapCut Multi-Track Timeline
            CapCutMultiTrackTimeline(
                currentTimeSec = uiState.currentTimeSec,
                durationSec = uiState.durationSec,
                fps = uiState.fps,
                isPlaying = uiState.isPlaying,
                timelineZoom = uiState.timelineZoom,
                mediaOverlays = uiState.mediaOverlays,
                selectedElementId = uiState.selectedElementId,
                sceneMarkers = uiState.sceneMarkers,
                onTogglePlay = { viewModel.togglePlay() },
                onSeek = { viewModel.seekTo(it) },
                onStepFrame = { viewModel.stepFrame(it) },
                onSelectElement = { viewModel.setSelectedElement(it) },
                onSplitAtPlayhead = { viewModel.splitSelectedClipAtPlayhead() },
                onDeleteSelected = { viewModel.deleteSelectedClip() },
                onDuplicateSelected = { viewModel.duplicateSelectedClip() },
                onZoomChange = { viewModel.setTimelineZoom(it) },
                onAddMediaClick = { viewModel.selectTab(StudioTab.OVERLAY) }
            )

            // 3. CapCut Bottom Tool Drawer (Dynamic per active tool)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f)
                    .background(CapCutSurface),
                color = CapCutSurface
            ) {
                when (uiState.activeTab) {
                    StudioTab.EDIT -> {
                        EditDrawer(
                            state = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.AUDIO -> {
                        AudioDrawer(
                            state = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.TEXT -> {
                        TextDrawer(
                            state = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.OVERLAY -> {
                        OverlayDrawer(
                            state = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.EFFECTS -> {
                        EffectsDrawer(
                            state = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.RATIO -> {
                        RatioDrawer(
                            state = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.CODE -> {
                        CodeEditor(
                            html = uiState.customHtml,
                            css = uiState.customCss,
                            js = uiState.customJs,
                            isModified = uiState.isCustomCodeActive,
                            onCodeChange = { h, c, j -> viewModel.updateCustomCode(h, c, j) },
                            onReset = { viewModel.resetToTemplateCode() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StudioTab.GALLERY -> {
                        GalleryView(
                            videos = uiState.savedVideos,
                            selectedVideo = uiState.selectedGalleryVideo,
                            onSelectVideo = { viewModel.selectGalleryVideo(it) },
                            onShareVideo = { viewModel.shareVideo(it) },
                            onDeleteVideo = { viewModel.deleteVideo(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Export / Render Progress Modal
    if (uiState.showRenderDialog) {
        ExportProgressModal(
            renderState = uiState.renderState,
            onCancel = { viewModel.cancelRender() },
            onDismiss = { viewModel.dismissRenderDialog() },
            onOpenGallery = {
                viewModel.dismissRenderDialog()
                viewModel.selectTab(StudioTab.GALLERY)
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

@Composable
private fun CapCutTopBar(
    state: StudioUiState,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenRatio: () -> Unit,
    onOpenInfo: () -> Unit,
    onExportClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = CapCutSurface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Project Logo / Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CapCutCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "CapCut Video Studio",
                        tint = OnCapCutCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HyperFrames",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Center: Format & Resolution Pill (Clickable)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenRatio),
                color = CapCutSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.resolution.label} · ${state.aspectRatio.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CapCutCyan
                    )
                }
            }

            // Right: Undo, Redo, Info, Export Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndo,
                    enabled = state.canUndo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (state.canUndo) TextPrimary else TextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = state.canRedo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (state.canRedo) TextPrimary else TextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenInfo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Info",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // CapCut Glowing Cyan Export Button
                Button(
                    onClick = onExportClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CapCutCyan,
                        contentColor = OnCapCutCyan
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("capcut_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Экспорт",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
