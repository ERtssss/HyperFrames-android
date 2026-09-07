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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.saalpa.model.project.SceneVoiceSettings
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentContainer
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioDanger
import com.saalpa.ui.theme.StudioSuccess
import com.saalpa.ui.theme.StudioSurface
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import java.util.Locale

data class VoicePreset(
    val id: String,
    val name: String,
    val lang: String,
    val tag: String
)

val PRESET_VOICES = listOf(
    VoicePreset("voice_rachel", "Rachel", "ru / en", "Studio Natural"),
    VoicePreset("voice_alex", "Alex", "ru-RU", "Deep Authoritative"),
    VoicePreset("voice_dmitri", "Дмитрий", "ru-RU", "Esports & Dynamic"),
    VoicePreset("voice_anna", "Анна", "ru-RU", "Warm Narrative"),
    VoicePreset("voice_maxim", "Максим", "ru-RU", "Cinema Voiceover"),
    VoicePreset("voice_elena", "Елена", "ru-RU", "Commercial Promo")
)

@Composable
fun VoicePanel(
    scene: HyperFrameScene?,
    onUpdateVoice: (SceneVoiceSettings) -> Unit,
    isRecordingVoiceover: Boolean,
    recordingDurationSec: Float,
    onStartRecordingVoiceover: () -> Unit,
    onStopRecordingVoiceover: () -> Unit,
    onPlayRecordedVoiceover: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (scene == null) return
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioSurface)
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("voice_panel")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = StudioAccentLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ГОЛОС И ОЗВУЧКА",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Привязано к сцене: ${scene.title}",
                    fontSize = 10.sp,
                    color = StudioAccentLight
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. VOICE PRESETS SELECTION
        Text(
            text = "ГОЛОСОВЫЕ МОДЕЛИ (VOICE MODELS)",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PRESET_VOICES) { vp ->
                val isSelected = scene.voice.voiceId == vp.id
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) StudioAccentContainer else StudioSurfaceVariant)
                        .border(1.dp, if (isSelected) StudioAccentLight else StudioBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            onUpdateVoice(scene.voice.copy(voiceId = vp.id, speakerName = vp.name, language = vp.lang))
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = vp.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else StudioTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = vp.tag,
                            fontSize = 8.5.sp,
                            color = StudioTextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. SCRIPT READOUT PREVIEW
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ТЕКСТ ДЛЯ ОЗВУЧКИ СЦЕНЫ:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scene.script.ifBlank { "Скрипт сцены пуст. Добавьте текст в панели Script & Scenes." },
                    fontSize = 11.sp,
                    color = StudioTextPrimary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. VOICE PARAMETERS (SPEED, PITCH, VOLUME, MUTE)
        Text(
            text = "ПАРАМЕТРЫ СИНТЕЗА И АУДИО",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Speed Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Скорость речи (Rate)", fontSize = 11.sp, color = StudioTextSecondary)
            }
            Text(
                text = String.format(Locale.US, "%.2fx", scene.voice.speed),
                fontSize = 11.sp,
                color = StudioAccentLight,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = scene.voice.speed,
            onValueChange = { onUpdateVoice(scene.voice.copy(speed = it)) },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
        )

        // Pitch Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Высота тона (Pitch)", fontSize = 11.sp, color = StudioTextSecondary)
            }
            Text(
                text = String.format(Locale.US, "%.2fx", scene.voice.pitch),
                fontSize = 11.sp,
                color = StudioAccentLight,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = scene.voice.pitch,
            onValueChange = { onUpdateVoice(scene.voice.copy(pitch = it)) },
            valueRange = 0.5f..1.5f,
            colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
        )

        // Volume Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Громкость озвучки", fontSize = 11.sp, color = StudioTextSecondary)
            }
            Text(
                text = "${(scene.voice.volume * 100).toInt()}%",
                fontSize = 11.sp,
                color = StudioAccentLight,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = scene.voice.volume,
            onValueChange = { onUpdateVoice(scene.voice.copy(volume = it)) },
            valueRange = 0f..1.0f,
            colors = SliderDefaults.colors(thumbColor = StudioAccentLight, activeTrackColor = StudioAccent)
        )

        // Mute Toggle
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
                    imageVector = if (scene.voice.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                    contentDescription = null,
                    tint = if (scene.voice.isMuted) StudioDanger else StudioTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Отключить звук озвучки (Mute)", fontSize = 11.sp, color = StudioTextPrimary)
            }
            Switch(
                checked = scene.voice.isMuted,
                onCheckedChange = { onUpdateVoice(scene.voice.copy(isMuted = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioDanger)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. LIVE MIC VOICEOVER RECORDING TOOL
        Text(
            text = "ЗАПИСЬ ДИКТОРА (LIVE MICROPHONE)",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSurfaceVariant)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingVoiceover) StudioDanger else StudioAccent)
                        .clickable {
                            if (isRecordingVoiceover) onStopRecordingVoiceover() else onStartRecordingVoiceover()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecordingVoiceover) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecordingVoiceover) "Стоп" else "Запись",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isRecordingVoiceover) "Идёт запись речи..." else (scene.voice.audioPath?.let { "Озвучка записана" } ?: "Микрофон готов"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecordingVoiceover) StudioDanger else StudioTextPrimary
                    )
                    Text(
                        text = if (isRecordingVoiceover) String.format(Locale.US, "Время: %.1f сек", recordingDurationSec) else "Нажмите для записи аудиодорожки сцены",
                        fontSize = 9.sp,
                        color = StudioTextMuted
                    )
                }
            }

            if (!isRecordingVoiceover && scene.voice.audioPath != null) {
                IconButton(
                    onClick = onPlayRecordedVoiceover,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StudioSuccess)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Прослушать", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for any existing references
 */
@Composable
fun AvatarVoicePanel(
    scene: HyperFrameScene?,
    onUpdateAvatar: (Any) -> Unit = {},
    onUpdateVoice: (SceneVoiceSettings) -> Unit,
    isRecordingVoiceover: Boolean,
    recordingDurationSec: Float,
    onStartRecordingVoiceover: () -> Unit,
    onStopRecordingVoiceover: () -> Unit,
    onPlayRecordedVoiceover: () -> Unit,
    modifier: Modifier = Modifier
) {
    VoicePanel(
        scene = scene,
        onUpdateVoice = onUpdateVoice,
        isRecordingVoiceover = isRecordingVoiceover,
        recordingDurationSec = recordingDurationSec,
        onStartRecordingVoiceover = onStartRecordingVoiceover,
        onStopRecordingVoiceover = onStopRecordingVoiceover,
        onPlayRecordedVoiceover = onPlayRecordedVoiceover,
        modifier = modifier
    )
}
