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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.AspectRatioType
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary

/**
 * Compact top bar following the HyperFrames design:
 * [ HYPER  +  ↶ ↷   16:9   </>   ▶   Export ]
 */
@Composable
fun HyperFramesTopBar(
    projectName: String,
    aspectRatio: AspectRatioType,
    isPlaying: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onTogglePlay: () -> Unit,
    onNewClick: () -> Unit,
    onSelectAspectRatio: (AspectRatioType) -> Unit,
    onOpenCodeEditor: () -> Unit,
    onOpenProjectManager: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRatioMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = StudioSurface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: HYPER badge & Project Name (Clicking opens Project Manager)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenProjectManager() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("btn_project_manager")
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioAccent)
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HYPER",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = projectName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        maxLines = 1
                    )
                }

                // Middle / Actions: +  ↶ ↷  16:9  </>  ▶  Export
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // + Button (Add Scene / New)
                    IconButton(
                        onClick = onNewClick,
                        modifier = Modifier.size(30.dp).testTag("btn_top_add")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить",
                            tint = StudioAccentLight,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // ↶ Undo
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.size(28.dp).testTag("btn_undo")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Отменить",
                            tint = if (canUndo) StudioTextPrimary else StudioTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // ↷ Redo
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier.size(28.dp).testTag("btn_redo")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Повторить",
                            tint = if (canRedo) StudioTextPrimary else StudioTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 16:9 Aspect Ratio
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioSurfaceVariant)
                                .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .clickable { showRatioMenu = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("btn_aspect_ratio"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = aspectRatio.label,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
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
                                            fontSize = 11.5.sp,
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

                    Spacer(modifier = Modifier.width(2.dp))

                    // </> Code Button (HTML / CSS / JS)
                    IconButton(
                        onClick = onOpenCodeEditor,
                        modifier = Modifier.size(30.dp).testTag("btn_open_code")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Редактор кода",
                            tint = StudioAccentLight,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // ▶ Play / Pause Button
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(30.dp).testTag("btn_top_play")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                            tint = if (isPlaying) StudioAccentLight else StudioTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Export Button
                    Button(
                        onClick = onExportClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(5.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_export")
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Export",
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
