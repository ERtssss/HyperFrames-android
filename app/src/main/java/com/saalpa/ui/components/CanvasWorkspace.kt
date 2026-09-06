package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.engine.HyperFramesJsBridge
import com.saalpa.model.AspectRatioType
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import com.saalpa.ui.theme.ViewportBorder
import com.saalpa.ui.theme.ViewportDarkBg
import java.util.Locale

@Composable
fun CanvasWorkspace(
    htmlContent: String,
    aspectRatio: AspectRatioType,
    currentTimeSec: Float,
    totalDurationSec: Float,
    activeScene: HyperFrameScene?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    jsBridge: HyperFramesJsBridge? = null,
    onReload: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSafeArea by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(false) }

    val aspectMultiplier = when (aspectRatio) {
        AspectRatioType.PORTRAIT_9_16 -> 9f / 16f
        AspectRatioType.LANDSCAPE_16_9 -> 16f / 9f
        AspectRatioType.SQUARE_1_1 -> 1f
        AspectRatioType.PORTRAIT_4_5 -> 4f / 5f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViewportDarkBg)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val maxW = maxWidth
            val maxH = maxHeight

            // Fit canvas maintaining aspect ratio
            val (frameWidth, frameHeight) = if (maxW / maxH > aspectMultiplier) {
                (maxH * aspectMultiplier) to maxH
            } else {
                maxW to (maxW / aspectMultiplier)
            }

            // Canvas Container Box
            Box(
                modifier = Modifier
                    .width(frameWidth)
                    .height(frameHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, ViewportBorder, RoundedCornerShape(8.dp))
                    .testTag("canvas_viewport")
            ) {
                // Live WebView Engine
                WebViewPreview(
                    compiledHtml = htmlContent,
                    currentTimeSec = currentTimeSec,
                    progress = if (totalDurationSec > 0f) (currentTimeSec / totalDurationSec).coerceIn(0f, 1f) else 0f,
                    isPlaying = isPlaying,
                    aspectRatio = aspectRatio,
                    jsBridge = jsBridge,
                    modifier = Modifier.fillMaxSize()
                )

                // Optional Safe Area Overlay (TikTok / Reels / Shorts margin guides)
                if (showSafeArea) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 40.dp)
                            .border(1.dp, Color(0x666366F1), RoundedCornerShape(4.dp))
                    )
                }

                // Optional 3x3 Composition Grid
                if (showGrid) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x33FFFFFF)))
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x33FFFFFF)))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0x33FFFFFF)))
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0x33FFFFFF)))
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Active Scene Watermark / Badge (Top Left)
                if (activeScene != null) {
                    Surface(
                        color = Color(0xCC0E1015),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StudioAccentLight)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeScene.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioTextPrimary
                            )
                        }
                    }
                }

                // Floating Mini Canvas Controls (Top Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Safe Area Toggle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (showSafeArea) StudioAccent else Color(0x990E1015))
                            .clickable { showSafeArea = !showSafeArea },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Safe Area",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Grid Toggle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (showGrid) StudioAccent else Color(0x990E1015))
                            .clickable { showGrid = !showGrid },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Grid",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Reload
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0x990E1015))
                            .clickable(onClick = onReload),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить Canvas",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Canvas Floating Play Overlay (Bottom Center)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    Surface(
                        color = Color(0xCC0E1015),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderSubtle),
                        modifier = Modifier.clickable(onClick = onTogglePlay)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Пауза" else "Плей",
                                tint = StudioAccentLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%02d:%04.1f", (currentTimeSec / 60).toInt(), currentTimeSec % 60),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
