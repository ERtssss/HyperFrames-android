package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.HyperFramesProject
import com.saalpa.model.project.SceneTransitionType
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioPurple
import com.saalpa.ui.theme.StudioSky
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import com.saalpa.ui.theme.StudioWarning
import java.util.Locale

@Composable
fun SceneTimeline(
    project: HyperFramesProject,
    activeSceneId: String?,
    currentTimeSec: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onSelectScene: (String) -> Unit,
    onAddScene: () -> Unit,
    onChangeSceneDuration: (sceneId: String, deltaSec: Float) -> Unit,
    onChangeTransition: (sceneId: String, transition: SceneTransitionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val totalDuration = project.totalDurationSec

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StudioSurface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioBorder)
            )

            // Timeline Transport Header: Play/Pause, Timecode, Scrubber
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Controls & Timecode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) StudioSurfaceVariant else StudioAccent)
                            .testTag("timeline_play_toggle")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Seek -1s
                    IconButton(
                        onClick = { onSeek((currentTimeSec - 1.0f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Назад на 1с",
                            tint = StudioTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Seek +1s
                    IconButton(
                        onClick = { onSeek((currentTimeSec + 1.0f).coerceAtMost(totalDuration)) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Вперед на 1с",
                            tint = StudioTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Timecode display
                    Surface(
                        color = StudioSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, StudioBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%02d:%04.1f",
                                    (currentTimeSec / 60).toInt(),
                                    currentTimeSec % 60
                                ),
                                color = StudioAccentLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = " / " + String.format(
                                    Locale.US,
                                    "%02d:%04.1f",
                                    (totalDuration / 60).toInt(),
                                    totalDuration % 60
                                ),
                                color = StudioTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Scene Count & Timeline label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCENE TIMELINE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${project.scenes.size} сцен",
                            fontSize = 9.5.sp,
                            color = StudioTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Global Timeline Progress Scrubber
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioSurfaceVariant)
                    .clickable {
                        // handled by full scrubber or drag
                    }
            ) {
                val progressFraction = if (totalDuration > 0f) (currentTimeSec / totalDuration).coerceIn(0f, 1f) else 0f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressFraction)
                        .background(StudioAccentLight)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // The Chain of Scenes (Horizontal Scrollable Sequence)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                var accumulatedTime = 0f

                project.scenes.forEachIndexed { index, scene ->
                    val sceneStart = accumulatedTime
                    val sceneEnd = sceneStart + scene.durationSec
                    val isCurrent = if (activeSceneId != null) scene.id == activeSceneId else (currentTimeSec >= sceneStart && currentTimeSec < sceneEnd)

                    // Scene Item Card
                    SceneTimelineCard(
                        scene = scene,
                        index = index + 1,
                        isSelected = isCurrent,
                        onClick = {
                            onSelectScene(scene.id)
                            onSeek(sceneStart)
                        },
                        onDurationMinus = { onChangeSceneDuration(scene.id, -0.5f) },
                        onDurationPlus = { onChangeSceneDuration(scene.id, 0.5f) }
                    )

                    // Transition node between scenes
                    if (index < project.scenes.size - 1) {
                        SceneTransitionChip(
                            transition = scene.transition,
                            onChangeTransition = { newTransition ->
                                onChangeTransition(scene.id, newTransition)
                            }
                        )
                    }

                    accumulatedTime = sceneEnd
                }

                // Add Scene Button
                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .width(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .border(1.dp, StudioBorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onAddScene() }
                        .testTag("btn_add_scene_timeline"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить сцену",
                            tint = StudioAccentLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+ Сцена",
                            fontSize = 8.5.sp,
                            color = StudioAccentLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun SceneTimelineCard(
    scene: HyperFrameScene,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDurationMinus: () -> Unit,
    onDurationPlus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(54.dp)
            .width(115.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) StudioAccentLight else StudioBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .testTag("scene_card_$index"),
        color = if (isSelected) StudioSurfaceElevated else StudioSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Index & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%02d", index),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) StudioAccentLight else StudioTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = scene.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        maxLines = 1
                    )
                }

                if (scene.avatar.isEnabled) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StudioPurple)
                    )
                }
            }

            // Footer: Duration and Quick +/- adjusters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%.1fs", scene.durationSec),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) StudioAccentLight else StudioTextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(StudioBorder)
                            .clickable(onClick = onDurationMinus),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "-", fontSize = 10.sp, color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(StudioBorder)
                            .clickable(onClick = onDurationPlus),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", fontSize = 10.sp, color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SceneTransitionChip(
    transition: SceneTransitionType,
    onChangeTransition: (SceneTransitionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(StudioSurfaceVariant)
            .border(1.dp, StudioBorderSubtle, CircleShape)
            .clickable { expanded = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "Переход: ${transition.label}",
            tint = if (transition != SceneTransitionType.NONE) StudioAccentLight else StudioTextMuted,
            modifier = Modifier.size(14.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(StudioSurfaceElevated)
        ) {
            SceneTransitionType.values().forEach { t ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = t.label,
                            fontSize = 11.sp,
                            color = if (t == transition) StudioAccentLight else StudioTextPrimary,
                            fontWeight = if (t == transition) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onChangeTransition(t)
                        expanded = false
                    }
                )
            }
        }
    }
}
