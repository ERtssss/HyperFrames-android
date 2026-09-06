package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.saalpa.model.AspectRatioType
import com.saalpa.model.RenderResolution
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary

@Composable
fun HyperFramesTopBar(
    projectName: String,
    aspectRatio: AspectRatioType,
    resolution: RenderResolution,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectAspectRatio: (AspectRatioType) -> Unit,
    onOpenCodeEditor: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenExplainer: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRatioMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = StudioSurface,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Studio Brand & Project Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioAccent)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HYPER",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = projectName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Studio • ${aspectRatio.label} • ${resolution.label}",
                            fontSize = 9.5.sp,
                            color = StudioTextMuted
                        )
                    }
                }

                // Center / Right Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Undo
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_undo")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Отменить",
                            tint = if (canUndo) StudioTextPrimary else StudioTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_redo")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Повторить",
                            tint = if (canRedo) StudioTextPrimary else StudioTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Aspect Ratio Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioSurfaceVariant)
                                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { showRatioMenu = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("btn_aspect_ratio"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Формат",
                                tint = StudioAccentLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = aspectRatio.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showRatioMenu,
                            onDismissRequest = { showRatioMenu = false },
                            modifier = Modifier.background(StudioSurfaceElevated)
                        ) {
                            AspectRatioType.values().forEach { ratio ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${ratio.title} (${ratio.label})",
                                            color = if (ratio == aspectRatio) StudioAccentLight else StudioTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (ratio == aspectRatio) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectAspectRatio(ratio)
                                        showRatioMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Code IDE button
                    IconButton(
                        onClick = onOpenCodeEditor,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_open_code")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Код IDE",
                            tint = StudioAccentLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Gallery button
                    IconButton(
                        onClick = onOpenGallery,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_open_gallery")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Галерея видео",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Export / Render button
                    Button(
                        onClick = onExportClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_export")
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "Экспорт",
                            modifier = Modifier.size(13.dp)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioBorder)
            )
        }
    }
}
