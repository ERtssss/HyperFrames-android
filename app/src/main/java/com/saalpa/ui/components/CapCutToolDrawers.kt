package com.saalpa.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.MediaOverlayItem
import com.saalpa.model.MediaType
import com.saalpa.model.RenderResolution
import com.saalpa.model.VideoEffectType
import com.saalpa.model.VoiceoverState
import com.saalpa.ui.StudioUiState
import com.saalpa.ui.StudioViewModel
import com.saalpa.ui.theme.CapCutBlue
import com.saalpa.ui.theme.CapCutCardBorder
import com.saalpa.ui.theme.CapCutCardBorderSubtle
import com.saalpa.ui.theme.CapCutCyan
import com.saalpa.ui.theme.CapCutCyanContainer
import com.saalpa.ui.theme.CapCutGreen
import com.saalpa.ui.theme.CapCutPink
import com.saalpa.ui.theme.CapCutSurface
import com.saalpa.ui.theme.CapCutSurfaceVariant
import com.saalpa.ui.theme.CapCutTrackBg
import com.saalpa.ui.theme.CapCutYellow
import com.saalpa.ui.theme.OnCapCutCyan
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun EditDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val selectedItem = state.selectedItem
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        if (selectedItem != null) {
            // Selected Element Inspector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val accentColor = when (selectedItem.type) {
                        MediaType.PHOTO -> CapCutCyan
                        MediaType.VIDEO -> CapCutBlue
                        MediaType.VOICEOVER, MediaType.BGM -> CapCutGreen
                        MediaType.TEXT -> CapCutYellow
                        MediaType.EFFECT -> CapCutPink
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedItem.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.duplicateSelectedClip() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { viewModel.deleteSelectedClip() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { viewModel.setSelectedElement(null) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Duration
            Text(
                text = "Начало: ${String.format(Locale.US, "%.1f", selectedItem.startTimeSec)}s | Длительность: ${String.format(Locale.US, "%.1f", selectedItem.durationSec)}s",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Старт", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(45.dp))
                Slider(
                    value = selectedItem.startTimeSec,
                    onValueChange = { viewModel.updateMediaOverlay(selectedItem.copy(startTimeSec = it)) },
                    valueRange = 0f..state.durationSec.coerceAtLeast(10f),
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Длина", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(45.dp))
                Slider(
                    value = selectedItem.durationSec,
                    onValueChange = { viewModel.updateMediaOverlay(selectedItem.copy(durationSec = it)) },
                    valueRange = 0.2f..60f,
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan),
                    modifier = Modifier.weight(1f)
                )
            }

            // Visual Transform Controls (for Photos, Videos, Text)
            if (selectedItem.type == MediaType.PHOTO || selectedItem.type == MediaType.VIDEO || selectedItem.type == MediaType.TEXT) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Масштаб: ${(selectedItem.scale * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp)
                Slider(
                    value = selectedItem.scale,
                    onValueChange = { viewModel.updateMediaOverlay(selectedItem.copy(scale = it)) },
                    valueRange = 0.2f..3.0f,
                    colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "X Позиция (${selectedItem.xPercent.toInt()}%)", color = TextMuted, fontSize = 10.sp)
                        Slider(
                            value = selectedItem.xPercent,
                            onValueChange = { viewModel.updateMediaOverlay(selectedItem.copy(xPercent = it)) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Y Позиция (${selectedItem.yPercent.toInt()}%)", color = TextMuted, fontSize = 10.sp)
                        Slider(
                            value = selectedItem.yPercent,
                            onValueChange = { viewModel.updateMediaOverlay(selectedItem.copy(yPercent = it)) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan)
                        )
                    }
                }

                // Animation Selector Chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Анимация появления", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MediaAnimationType.values().forEach { anim ->
                        FilterChip(
                            selected = selectedItem.animation == anim,
                            onClick = { viewModel.updateMediaOverlay(selectedItem.copy(animation = anim)) },
                            label = { Text(anim.name.replace("_", " "), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CapCutCyan,
                                selectedLabelColor = OnCapCutCyan
                            )
                        )
                    }
                }
            }

        } else {
            // Template Parameters and Playback Speed (Duration restriction removed)
            Text(
                text = "Параметры шаблона и сцен",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Playback Speed
            Text(text = "Скорость воспроизведения", color = TextSecondary, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.5f, 0.75f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                    FilterChip(
                        selected = state.playbackSpeed == speed,
                        onClick = { viewModel.setPlaybackSpeed(speed) },
                        label = { Text("${speed}x", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CapCutCyan,
                            selectedLabelColor = OnCapCutCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Background Customizers (from current template)
            Text(
                text = "Надписи и параметры композиции",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            state.selectedTemplate.params.forEach { param ->
                val currentValue = state.paramsMap[param.key] ?: param.defaultValue
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { viewModel.updateParam(param.key, it) },
                    label = { Text(param.label, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CapCutCyan,
                        unfocusedBorderColor = CapCutCardBorder,
                        focusedLabelColor = CapCutCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        }
        }
    }
}

@Composable
fun AudioDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState = state.voiceoverState
    val scrollState = rememberScrollState()

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.addAudioFromUri(uri, "Импортированная музыка", isVoiceover = false)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        // Voiceover Recording Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CapCutSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (voiceState.isRecording) CapCutPink else CapCutCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = CapCutGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Запись озвучки (Voiceover)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    if (voiceState.isRecording) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CapCutPink)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%02d:%04.1f", (voiceState.recordingDurationSec / 60).toInt(), voiceState.recordingDurationSec % 60),
                                color = CapCutPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Glowing Mic Button
                val infiniteTransition = rememberInfiniteTransition()
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (voiceState.isRecording) 1.25f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(if (voiceState.isRecording) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(if (voiceState.isRecording) CapCutPink else CapCutGreen)
                        .clickable {
                            if (voiceState.isRecording) {
                                viewModel.stopVoiceoverRecording()
                            } else {
                                viewModel.startVoiceoverRecording()
                            }
                        }
                        .testTag("capcut_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (voiceState.isRecording) "Нажмите чтобы остановить и сохранить" else "Нажмите для записи с микрофона",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sound Mixer (Volume Sliders & Mute Toggles)
        Text(text = "Аудио микшер и громкость", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        // Voiceover Volume
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.toggleVoiceoverMute() }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (voiceState.isVoiceoverMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute VO",
                    tint = if (voiceState.isVoiceoverMuted) Color(0xFFFF5252) else CapCutGreen
                )
            }
            Text(text = "Голос: ${(voiceState.voiceoverVolume * 100).toInt()}%", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(85.dp))
            Slider(
                value = voiceState.voiceoverVolume,
                onValueChange = { viewModel.setVoiceoverVolume(it) },
                valueRange = 0f..2f,
                colors = SliderDefaults.colors(thumbColor = CapCutGreen, activeTrackColor = CapCutGreen),
                modifier = Modifier.weight(1f)
            )
        }

        // BGM Volume
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.toggleBgmMute() }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (voiceState.isBgmMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute BGM",
                    tint = if (voiceState.isBgmMuted) Color(0xFFFF5252) else CapCutCyan
                )
            }
            Text(text = "Музыка: ${(voiceState.bgmVolume * 100).toInt()}%", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(85.dp))
            Slider(
                value = voiceState.bgmVolume,
                onValueChange = { viewModel.setBgmVolume(it) },
                valueRange = 0f..2f,
                colors = SliderDefaults.colors(thumbColor = CapCutCyan, activeTrackColor = CapCutCyan),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Import Audio File Button
        OutlinedButton(
            onClick = { audioPicker.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CapCutCyan),
            border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCyan)
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Импортировать аудиофайл (MP3 / WAV)", fontSize = 12.sp)
        }
    }
}

@Composable
fun TextDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("КРУТОЙ ЭФФЕКТ") }
    var selectedColor by remember { mutableStateOf("#00F0FF") }
    var selectedAnim by remember { mutableStateOf(MediaAnimationType.ZOOM_IN) }
    var fontSize by remember { mutableFloatStateOf(28f) }
    val scrollState = rememberScrollState()

    val colorsList = listOf(
        "#00F0FF" to "Cyan",
        "#FFE600" to "Yellow",
        "#FF2A6D" to "Pink",
        "#FFFFFF" to "White",
        "#00E676" to "Green",
        "#9D4EDD" to "Violet",
        "#FF6D00" to "Orange"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        Text(text = "Добавить текст на видео", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Текст титра / заголовка", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CapCutYellow,
                unfocusedBorderColor = CapCutCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Color Swatches
        Text(text = "Цвет текста", fontSize = 11.sp, color = TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colorsList.forEach { (hex, _) ->
                val color = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == hex) 3.dp else 1.dp,
                            color = if (selectedColor == hex) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = hex }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Font Size Slider
        Text(text = "Размер шрифта: ${fontSize.toInt()} px", fontSize = 11.sp, color = TextSecondary)
        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 16f..56f,
            colors = SliderDefaults.colors(thumbColor = CapCutYellow, activeTrackColor = CapCutYellow)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Animation Style
        Text(text = "Анимация текста", fontSize = 11.sp, color = TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                MediaAnimationType.ZOOM_IN,
                MediaAnimationType.TYPEWRITER,
                MediaAnimationType.BOUNCE,
                MediaAnimationType.GLITCH,
                MediaAnimationType.FADE
            ).forEach { anim ->
                FilterChip(
                    selected = selectedAnim == anim,
                    onClick = { selectedAnim = anim },
                    label = { Text(anim.name.replace("_", " "), fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapCutYellow,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Text Button
        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    viewModel.addTextOverlay(
                        text = textInput,
                        colorHex = selectedColor,
                        fontSizeSp = fontSize.toInt(),
                        animation = selectedAnim
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CapCutYellow, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Добавить на таймлайн", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun OverlayDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.addPhotoFromUri(uri, "Фото слой")
        }
    }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.addVideoFromUri(uri, "Видео слой")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        Text(text = "Наложение слоев (Picture-in-Picture)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { photoPicker.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CapCutCyan, contentColor = OnCapCutCyan)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Фото +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { videoPicker.launch("video/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CapCutPink, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Видео +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active layers list
        Text(text = "Текущие медиа слои (${state.mediaOverlays.size})", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))

        state.mediaOverlays.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { viewModel.setSelectedElement(item.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (item.id == state.selectedElementId) CapCutCyanContainer else CapCutSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (item.id == state.selectedElementId) CapCutCyan else CapCutCardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = when (item.type) {
                                MediaType.PHOTO -> Icons.Default.Image
                                MediaType.VIDEO -> Icons.Default.Movie
                                MediaType.VOICEOVER -> Icons.Default.Mic
                                MediaType.BGM -> Icons.Default.MusicNote
                                MediaType.TEXT -> Icons.Default.TextFields
                                MediaType.EFFECT -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = CapCutCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (item.type == MediaType.TEXT) item.textContent else item.title,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.toggleMediaOverlayEnabled(item.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (item.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle",
                                tint = if (item.isEnabled) CapCutCyan else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.removeMediaOverlay(item.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EffectsDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        Text(text = "Видео эффекты и фильтры", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = "Нажмите на эффект для добавления на таймлайн в позицию курсора", fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        val effects = VideoEffectType.values()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            effects.forEach { effect ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.addEffectOverlay(effect) },
                    colors = CardDefaults.cardColors(containerColor = CapCutSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CapCutCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CapCutPink.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CapCutPink, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = effect.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = "GSAP Real-time Shader", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Button(
                            onClick = { viewModel.addEffectOverlay(effect) },
                            colors = ButtonDefaults.buttonColors(containerColor = CapCutPink),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Добавить", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatioDrawer(
    state: StudioUiState,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        Text(text = "Соотношение сторон (Aspect Ratio)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AspectRatioType.values().forEach { ratio ->
                val isSelected = state.aspectRatio == ratio
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .clickable { viewModel.setAspectRatio(ratio) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CapCutCyanContainer else CapCutSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CapCutCyan else CapCutCardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ratio.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CapCutCyan else TextPrimary
                        )
                        Text(
                            text = ratio.category,
                            fontSize = 9.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resolution selector
        Text(text = "Разрешение рендера (Resolution)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RenderResolution.values().forEach { res ->
                val isSelected = state.resolution == res
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setResolution(res) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CapCutCyanContainer else CapCutSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CapCutCyan else CapCutCardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = res.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CapCutCyan else TextPrimary
                        )
                        Text(
                            text = "${res.width}x${res.height}",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FPS selector
        Text(text = "Частота кадров (FPS)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(24, 30, 60).forEach { fpsValue ->
                FilterChip(
                    selected = state.fps == fpsValue,
                    onClick = { viewModel.setFps(fpsValue) },
                    label = { Text("$fpsValue fps", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapCutCyan,
                        selectedLabelColor = OnCapCutCyan
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
