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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.ui.components.ArchitectureExplainerModal
import com.saalpa.ui.components.CodeEditor
import com.saalpa.ui.components.ExportProgressModal
import com.saalpa.ui.components.GalleryView
import com.saalpa.ui.components.QuickCustomizer
import com.saalpa.ui.components.RenderSettingsDialog
import com.saalpa.ui.components.TemplateSelector
import com.saalpa.ui.components.TimelineBar
import com.saalpa.ui.components.WebViewPreview
import com.saalpa.ui.theme.CyberPink
import com.saalpa.ui.theme.ElectricCyan
import com.saalpa.ui.theme.EmeraldGreen
import com.saalpa.ui.theme.NeonViolet
import com.saalpa.ui.theme.OnPrimaryBrand
import com.saalpa.ui.theme.PrimaryBrand
import com.saalpa.ui.theme.PrimaryBrandContainer
import com.saalpa.ui.theme.StudioCardBorder
import com.saalpa.ui.theme.StudioCardBorderSubtle
import com.saalpa.ui.theme.StudioDarkBg
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary

@Composable
fun StudioScreen(
    viewModel: StudioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = StudioDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            StudioTopBar(
                aspectRatioText = uiState.aspectRatio.title.split(" ").first(),
                onExplainerClick = { viewModel.setShowExplainer(true) },
                onRenderClick = { viewModel.startRender() }
            )
        },
        bottomBar = {
            StudioBottomNav(
                activeTab = uiState.activeTab,
                onSelectTab = { viewModel.selectTab(it) },
                galleryCount = uiState.savedVideos.size
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Live Viewport Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                WebViewPreview(
                    compiledHtml = viewModel.getCompiledHtmlForPreview(),
                    currentTimeSec = uiState.currentTimeSec,
                    progress = uiState.progress,
                    isPlaying = uiState.isPlaying,
                    aspectRatio = uiState.aspectRatio
                )
            }

            // Timeline Control Bar
            TimelineBar(
                currentTimeSec = uiState.currentTimeSec,
                durationSec = uiState.durationSec,
                fps = uiState.fps,
                isPlaying = uiState.isPlaying,
                onTogglePlay = { viewModel.togglePlay() },
                onSeek = { viewModel.seekTo(it) },
                onStepFrame = { viewModel.stepFrame(it) }
            )

            // Dynamic Panel based on Active Tab
            when (uiState.activeTab) {
                StudioTab.TEMPLATES -> {
                    TemplateSelector(
                        selectedTemplate = uiState.selectedTemplate,
                        onSelectTemplate = { viewModel.loadTemplate(it) }
                    )
                }

                StudioTab.CUSTOMIZE -> {
                    QuickCustomizer(
                        template = uiState.selectedTemplate,
                        paramsMap = uiState.paramsMap,
                        onUpdateParam = { k, v -> viewModel.updateParam(k, v) }
                    )
                }

                StudioTab.CODE -> {
                    CodeEditor(
                        html = uiState.customHtml,
                        css = uiState.customCss,
                        js = uiState.customJs,
                        isModified = uiState.isCustomCodeActive,
                        onCodeChange = { h, c, j -> viewModel.updateCustomCode(h, c, j) },
                        onReset = { viewModel.resetToTemplateCode() }
                    )
                }

                StudioTab.SETTINGS -> {
                    RenderSettingsDialog(
                        aspectRatio = uiState.aspectRatio,
                        resolution = uiState.resolution,
                        fps = uiState.fps,
                        durationSec = uiState.durationSec,
                        onAspectRatioChange = { viewModel.setAspectRatio(it) },
                        onResolutionChange = { viewModel.setResolution(it) },
                        onFpsChange = { viewModel.setFps(it) },
                        onDurationChange = { viewModel.setDuration(it) },
                        onStartRender = { viewModel.startRender() }
                    )
                }

                StudioTab.GALLERY -> {
                    GalleryView(
                        videos = uiState.savedVideos,
                        selectedVideo = uiState.selectedGalleryVideo,
                        onSelectVideo = { viewModel.selectGalleryVideo(it) },
                        onShareVideo = { viewModel.shareVideo(it) },
                        onDeleteVideo = { viewModel.deleteVideo(it) }
                    )
                }
            }
        }
    }

    // Export Progress Modal
    if (uiState.showRenderDialog) {
        ExportProgressModal(
            renderState = uiState.renderState,
            onCancel = { viewModel.cancelRender() },
            onDismiss = { viewModel.dismissRenderDialog() },
            onOpenGallery = {
                viewModel.dismissRenderDialog()
                viewModel.selectTab(StudioTab.GALLERY)
            },
            onShareVideo = { viewModel.shareVideo(it) }
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
private fun StudioTopBar(
    aspectRatioText: String,
    onExplainerClick: () -> Unit,
    onRenderClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = StudioDarkBg,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onExplainerClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBrandContainer)
                        .border(1.dp, PrimaryBrand.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "HyperFrames",
                        tint = PrimaryBrand,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "HyperFrames",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "HTML to Video Engine",
                        fontSize = 11.sp,
                        color = PrimaryBrand,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Actions: Info + Export CTA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Button
                IconButton(
                    onClick = onExplainerClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StudioSurface)
                        .border(1.dp, StudioCardBorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "How It Works",
                        tint = TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Render Action Button
                Button(
                    onClick = onRenderClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBrand,
                        contentColor = OnPrimaryBrand
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = PrimaryBrand.copy(alpha = 0.4f))
                        .testTag("top_render_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Render MP4",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioBottomNav(
    activeTab: StudioTab,
    onSelectTab: (StudioTab) -> Unit,
    galleryCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = StudioSurface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioTabItem(
                label = "Presets",
                icon = Icons.Default.AutoAwesome,
                isSelected = activeTab == StudioTab.TEMPLATES,
                onClick = { onSelectTab(StudioTab.TEMPLATES) },
                accentColor = PrimaryBrand
            )
            StudioTabItem(
                label = "Customize",
                icon = Icons.Default.Edit,
                isSelected = activeTab == StudioTab.CUSTOMIZE,
                onClick = { onSelectTab(StudioTab.CUSTOMIZE) },
                accentColor = NeonViolet
            )
            StudioTabItem(
                label = "Code",
                icon = Icons.Default.Code,
                isSelected = activeTab == StudioTab.CODE,
                onClick = { onSelectTab(StudioTab.CODE) },
                accentColor = EmeraldGreen
            )
            StudioTabItem(
                label = "Format",
                icon = Icons.Default.Settings,
                isSelected = activeTab == StudioTab.SETTINGS,
                onClick = { onSelectTab(StudioTab.SETTINGS) },
                accentColor = CyberPink
            )
            StudioTabItem(
                label = "Gallery",
                icon = Icons.Default.VideoLibrary,
                isSelected = activeTab == StudioTab.GALLERY,
                onClick = { onSelectTab(StudioTab.GALLERY) },
                accentColor = PrimaryBrand,
                badgeText = if (galleryCount > 0) "$galleryCount" else null
            )
        }
    }
}

@Composable
private fun StudioTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    badgeText: String? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PrimaryBrandContainer.copy(alpha = 0.6f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_tab_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) accentColor else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(PrimaryBrand),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryBrand
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accentColor else TextMuted
            )
        }
    }
}

