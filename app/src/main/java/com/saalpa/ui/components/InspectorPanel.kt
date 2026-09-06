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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.VideoEffectType
import com.saalpa.model.project.ElementType
import com.saalpa.model.project.HyperFrameElement
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.SceneTransitionType
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioDanger
import com.saalpa.ui.theme.StudioPurple
import com.saalpa.ui.theme.StudioSky
import com.saalpa.ui.theme.StudioSuccess
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import java.util.Locale

@Composable
fun InspectorPanel(
    scene: HyperFrameScene?,
    selectedElement: HyperFrameElement?,
    onUpdateSceneDuration: (Float) -> Unit,
    onUpdateSceneBg: (String) -> Unit,
    onUpdateSceneTransition: (SceneTransitionType) -> Unit,
    onToggleSceneAvatar: (Boolean) -> Unit,
    onUpdateElement: (HyperFrameElement) -> Unit,
    onDeleteElement: (String) -> Unit,
    onAddTextElement: () -> Unit,
    onAddImageElement: () -> Unit,
    onAddEffectElement: (VideoEffectType) -> Unit,
    onDeselectElement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioSurface)
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        if (selectedElement != null) {
            // ELEMENT INSPECTOR
            ElementInspectorHeader(
                element = selectedElement,
                onClose = onDeselectElement,
                onDelete = { onDeleteElement(selectedElement.id) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Text Content if Text/Badge
            if (selectedElement.type == ElementType.TEXT || selectedElement.type == ElementType.BADGE) {
                InspectorSectionTitle("Текст и Типографика")
                OutlinedTextField(
                    value = selectedElement.textContent,
                    onValueChange = { onUpdateElement(selectedElement.copy(textContent = it)) },
                    label = { Text("Содержимое текста", fontSize = 10.sp, color = StudioTextMuted) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = StudioTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAccentLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedContainerColor = StudioSurfaceVariant,
                        unfocusedContainerColor = StudioSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Размер шрифта", fontSize = 11.sp, color = StudioTextSecondary)
                    Text("${selectedElement.fontSizeSp} px", fontSize = 11.sp, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = selectedElement.fontSizeSp.toFloat(),
                    onValueChange = { onUpdateElement(selectedElement.copy(fontSizeSp = it.toInt())) },
                    valueRange = 16f..72f,
                    colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
                )

                // Quick Color Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colors = listOf("#FFFFFF", "#6366F1", "#10B981", "#F59E0B", "#EF4444", "#00F0FF")
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (selectedElement.textColorHex.equals(hex, ignoreCase = true)) 2.dp else 1.dp,
                                    color = if (selectedElement.textColorHex.equals(hex, ignoreCase = true)) Color.White else StudioBorder,
                                    shape = CircleShape
                                )
                                .clickable { onUpdateElement(selectedElement.copy(textColorHex = hex)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TRANSFORM CONTROLS (X, Y, Scale, Opacity, Rotation)
            InspectorSectionTitle("Трансформация (Canvas Transform)")

            // Position X
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Позиция X", fontSize = 11.sp, color = StudioTextSecondary)
                Text("${selectedElement.transform.xPercent.toInt()}%", fontSize = 11.sp, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = selectedElement.transform.xPercent,
                onValueChange = { onUpdateElement(selectedElement.copy(transform = selectedElement.transform.copy(xPercent = it))) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
            )

            // Position Y
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Позиция Y", fontSize = 11.sp, color = StudioTextSecondary)
                Text("${selectedElement.transform.yPercent.toInt()}%", fontSize = 11.sp, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = selectedElement.transform.yPercent,
                onValueChange = { onUpdateElement(selectedElement.copy(transform = selectedElement.transform.copy(yPercent = it))) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
            )

            // Scale
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Масштаб (Scale)", fontSize = 11.sp, color = StudioTextSecondary)
                Text(String.format(Locale.US, "%.2fx", selectedElement.transform.scale), fontSize = 11.sp, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = selectedElement.transform.scale,
                onValueChange = { onUpdateElement(selectedElement.copy(transform = selectedElement.transform.copy(scale = it))) },
                valueRange = 0.3f..2.5f,
                colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
            )

            // Opacity
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Прозрачность (Opacity)", fontSize = 11.sp, color = StudioTextSecondary)
                Text("${(selectedElement.transform.opacity * 100).toInt()}%", fontSize = 11.sp, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = selectedElement.transform.opacity,
                onValueChange = { onUpdateElement(selectedElement.copy(transform = selectedElement.transform.copy(opacity = it))) },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ANIMATION PICKER
            InspectorSectionTitle("Анимация появления (In Animation)")
            AnimationSelector(
                current = selectedElement.animation.type,
                onSelect = { anim ->
                    onUpdateElement(selectedElement.copy(animation = selectedElement.animation.copy(type = anim)))
                }
            )

        } else if (scene != null) {
            // SCENE INSPECTOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "СВОЙСТВА СЦЕНЫ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = scene.title,
                        fontSize = 10.sp,
                        color = StudioAccentLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scene Duration
            InspectorSectionTitle("Длительность сцены (Seconds)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Время показа", fontSize = 11.sp, color = StudioTextSecondary)
                Text(String.format(Locale.US, "%.1f сек", scene.durationSec), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = scene.durationSec,
                onValueChange = onUpdateSceneDuration,
                valueRange = 1.0f..15.0f,
                colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scene Transition
            InspectorSectionTitle("Переход сцены (Transition)")
            TransitionSelector(
                current = scene.transition,
                onSelect = onUpdateSceneTransition
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Background Presets
            InspectorSectionTitle("Фон сцены (Canvas Background)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val bgPresets = listOf(
                    "Deep Dark" to "radial-gradient(circle at 50% 30%, #1c2333 0%, #08090d 100%)",
                    "Indigo Glow" to "radial-gradient(circle at 50% 30%, #312e81 0%, #0c0e14 100%)",
                    "Cyber Noir" to "radial-gradient(circle at 50% 40%, #1e1b4b 0%, #030712 100%)",
                    "Emerald" to "radial-gradient(circle at 50% 30%, #064e3b 0%, #022c22 100%)"
                )
                bgPresets.forEach { (name, gradient) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .background(StudioSurfaceVariant)
                            .clickable { onUpdateSceneBg(gradient) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = name, fontSize = 9.sp, color = StudioTextSecondary, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Avatar Toggle
            InspectorSectionTitle("AI Аватар сцены")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Аватар",
                        tint = StudioPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(scene.avatar.characterName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                        Text("Ведущий сцены", fontSize = 9.sp, color = StudioTextMuted)
                    }
                }
                Switch(
                    checked = scene.avatar.isEnabled,
                    onCheckedChange = onToggleSceneAvatar,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StudioAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add Elements to Scene
            InspectorSectionTitle("Добавить в эту сцену")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onAddTextElement,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant, contentColor = StudioTextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp).border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(13.dp), tint = StudioAccentLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Текст", fontSize = 10.sp)
                }

                Button(
                    onClick = onAddImageElement,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant, contentColor = StudioTextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp).border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(13.dp), tint = StudioSky)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Фото", fontSize = 10.sp)
                }

                Button(
                    onClick = { onAddEffectElement(VideoEffectType.NEON_GLOW) },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant, contentColor = StudioTextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp).border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = StudioPurple)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Эффект", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scene Elements List
            if (scene.elements.isNotEmpty()) {
                InspectorSectionTitle("Элементы сцены (${scene.elements.size})")
                scene.elements.forEach { el ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurfaceVariant)
                            .border(0.5.dp, StudioBorderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (el.type == ElementType.TEXT) el.textContent else el.name,
                            fontSize = 11.sp,
                            color = StudioTextPrimary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteElement(el.id) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = StudioDanger, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementInspectorHeader(
    element: HyperFrameElement,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ИНСПЕКТОР ЭЛЕМЕНТА",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${element.type.label}: ${element.name}",
                fontSize = 10.sp,
                color = StudioAccentLight
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = StudioDanger, modifier = Modifier.size(15.dp))
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть инспектор", tint = StudioTextSecondary, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun InspectorSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        color = StudioTextMuted,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun AnimationSelector(
    current: MediaAnimationType,
    onSelect: (MediaAnimationType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StudioSurfaceVariant)
                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = current.title, fontSize = 11.sp, color = StudioTextPrimary)
            Text(text = "▼", fontSize = 9.sp, color = StudioTextMuted)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(StudioSurfaceElevated)
        ) {
            MediaAnimationType.values().forEach { anim ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = anim.title,
                            fontSize = 11.sp,
                            color = if (anim == current) StudioAccentLight else StudioTextPrimary
                        )
                    },
                    onClick = {
                        onSelect(anim)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TransitionSelector(
    current: SceneTransitionType,
    onSelect: (SceneTransitionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StudioSurfaceVariant)
                .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = current.label, fontSize = 11.sp, color = StudioTextPrimary)
            Text(text = "▼", fontSize = 9.sp, color = StudioTextMuted)
        }

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
                            color = if (t == current) StudioAccentLight else StudioTextPrimary
                        )
                    },
                    onClick = {
                        onSelect(t)
                        expanded = false
                    }
                )
            }
        }
    }
}
