package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioDanger
import com.saalpa.ui.theme.StudioPurple
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import java.util.Locale

@Composable
fun ScriptScenePanel(
    project: HyperFramesProject,
    activeSceneId: String?,
    onSelectScene: (String) -> Unit,
    onAddScene: () -> Unit,
    onDuplicateScene: (String) -> Unit,
    onDeleteScene: (String) -> Unit,
    onMoveSceneUp: (String) -> Unit,
    onMoveSceneDown: (String) -> Unit,
    onUpdateSceneTitle: (sceneId: String, newTitle: String) -> Unit,
    onUpdateSceneScript: (sceneId: String, newScript: String) -> Unit,
    onOpenVoicePanel: (sceneId: String) -> Unit,
    onOpenAvatarPanel: (sceneId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioSurface)
    ) {
        // Panel Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCRIPT & SCENES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Управление сценарием и сценами видео",
                    fontSize = 10.sp,
                    color = StudioTextMuted
                )
            }

            Button(
                onClick = onAddScene,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("btn_add_scene_panel")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Добавить сцену", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(StudioBorder)
        )

        // List of Scenes with Scripts
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(project.scenes, key = { _, scene -> scene.id }) { index, scene ->
                val isSelected = scene.id == activeSceneId

                ScriptSceneCard(
                    scene = scene,
                    index = index + 1,
                    isFirst = index == 0,
                    isLast = index == project.scenes.size - 1,
                    isSelected = isSelected,
                    onSelect = { onSelectScene(scene.id) },
                    onDuplicate = { onDuplicateScene(scene.id) },
                    onDelete = { onDeleteScene(scene.id) },
                    onMoveUp = { onMoveSceneUp(scene.id) },
                    onMoveDown = { onMoveSceneDown(scene.id) },
                    onUpdateTitle = { onUpdateSceneTitle(scene.id, it) },
                    onUpdateScript = { onUpdateSceneScript(scene.id, it) },
                    onOpenVoice = { onOpenVoicePanel(scene.id) }
                )
            }
        }
    }
}

@Composable
private fun ScriptSceneCard(
    scene: HyperFrameScene,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateScript: (String) -> Unit,
    onOpenVoice: () -> Unit
) {
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember(scene.title) { mutableStateOf(scene.title) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) StudioAccentLight else StudioBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect)
            .testTag("scene_script_card_$index"),
        color = if (isSelected) StudioSurfaceElevated else StudioSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Scene Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) StudioAccent else StudioSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "SCENE %02d", index),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else StudioTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scene.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }

                // Actions: Move up/down, duplicate, delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isFirst) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Вверх",
                                tint = StudioTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    if (!isLast) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Вниз",
                                tint = StudioTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Дублировать",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = StudioDanger,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Script Text Input (Live speech & prompt generation)
            OutlinedTextField(
                value = scene.script,
                onValueChange = onUpdateScript,
                label = { Text("Сценарий озвучки (Script)", fontSize = 10.sp, color = StudioTextMuted) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = StudioTextPrimary,
                    lineHeight = 16.sp
                ),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioAccentLight,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scene_script_input_$index")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Metadata Chips: Avatar, Voice, Duration, Transition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioSurface)
                            .border(0.5.dp, StudioBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenVoice)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Голос",
                            tint = StudioAccentLight,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = scene.voice.speakerName.split(" ").first(),
                            fontSize = 9.5.sp,
                            color = StudioTextSecondary
                        )
                    }

                    // Elements count chip
                    if (scene.elements.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioSurface)
                                .border(0.5.dp, StudioBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Элементов: ${scene.elements.size}",
                                fontSize = 9.5.sp,
                                color = StudioTextMuted
                            )
                        }
                    }
                }

                // Duration & Transition
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.1fs • %s", scene.durationSec, scene.transition.label.split(" ").first()),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioTextMuted
                    )
                }
            }
        }
    }
}
