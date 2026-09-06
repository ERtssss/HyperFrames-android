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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImage
import com.saalpa.model.project.HyperFrameScene
import com.saalpa.model.project.SceneAvatarSettings
import com.saalpa.model.project.SceneVoiceSettings
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentContainer
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioBorderSubtle
import com.saalpa.ui.theme.StudioDanger
import com.saalpa.ui.theme.StudioPurple
import com.saalpa.ui.theme.StudioSuccess
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceElevated
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary
import java.util.Locale

data class AvatarPreset(
    val id: String,
    val name: String,
    val role: String,
    val imageUrl: String
)

data class VoicePreset(
    val id: String,
    val name: String,
    val lang: String,
    val tag: String
)

val PRESET_AVATARS = listOf(
    AvatarPreset("alex_pro", "Alex Pro", "AI Host", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80"),
    AvatarPreset("elena_keynote", "Elena Tech", "Keynote", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=300&auto=format&fit=crop&q=80"),
    AvatarPreset("marcus_cyber", "Marcus Cyber", "Creator", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop&q=80"),
    AvatarPreset("sophia_news", "Sophia News", "Anchor", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&auto=format&fit=crop&q=80")
)

val PRESET_VOICES = listOf(
    VoicePreset("voice_rachel", "Rachel", "ru-RU / en-US", "Studio Natural"),
    VoicePreset("voice_alex", "Alex", "ru-RU", "Deep Authoritative"),
    VoicePreset("voice_dmitri", "Дмитрий", "ru-RU", "Esports & Gaming"),
    VoicePreset("voice_anna", "Анна", "ru-RU", "Warm Dynamic")
)

@Composable
fun AvatarVoicePanel(
    scene: HyperFrameScene?,
    onUpdateAvatar: (SceneAvatarSettings) -> Unit,
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
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI AVATAR & VOICE STUDIO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Привязано к сцене: ${scene.title}",
                    fontSize = 10.sp,
                    color = StudioAccentLight
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. AVATAR CONFIGURATION
        Text(
            text = "AI ВЕДУЩИЙ / АВАТАР",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Avatar Enabled Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Включить аватара в этой сцене", fontSize = 11.sp, color = StudioTextPrimary)
            Switch(
                checked = scene.avatar.isEnabled,
                onCheckedChange = { onUpdateAvatar(scene.avatar.copy(isEnabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioAccent)
            )
        }

        if (scene.avatar.isEnabled) {
            Spacer(modifier = Modifier.height(10.dp))

            // Avatar Presets Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PRESET_AVATARS) { preset ->
                    val isSelected = scene.avatar.id == preset.id
                    Surface(
                        modifier = Modifier
                            .width(85.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) StudioPurple else StudioBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onUpdateAvatar(
                                    scene.avatar.copy(
                                        id = preset.id,
                                        characterName = preset.name,
                                        avatarImageUrl = preset.imageUrl
                                    )
                                )
                            },
                        color = StudioSurfaceVariant
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            AsyncImage(
                                model = preset.imageUrl,
                                contentDescription = preset.name,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = preset.name, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary, maxLines = 1)
                            Text(text = preset.role, fontSize = 8.5.sp, color = StudioTextMuted, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Avatar Position selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val positions = listOf(
                    "Нижний правый" to (82f to 75f),
                    "Нижний левый" to (18f to 75f),
                    "Центр (PIP)" to (50f to 50f)
                )
                positions.forEach { (label, coords) ->
                    val isCur = scene.avatar.xPercent == coords.first && scene.avatar.yPercent == coords.second
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCur) StudioAccentContainer else StudioSurfaceVariant)
                            .border(1.dp, if (isCur) StudioAccentLight else StudioBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                onUpdateAvatar(scene.avatar.copy(xPercent = coords.first, yPercent = coords.second))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, fontSize = 9.sp, color = if (isCur) Color.White else StudioTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Avatar Scale Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Размер аватара", fontSize = 11.sp, color = StudioTextSecondary)
                Text(String.format(Locale.US, "%.1fx", scene.avatar.scale), fontSize = 11.sp, color = StudioAccentLight, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = scene.avatar.scale,
                onValueChange = { onUpdateAvatar(scene.avatar.copy(scale = it)) },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = StudioPurple, activeTrackColor = StudioPurple)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. VOICE & SPEECH CONFIGURATION
        Text(
            text = "ГОЛОС И ОЗВУЧКА СЦЕНЫ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Voice Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PRESET_VOICES.forEach { vp ->
                val isSelected = scene.voice.voiceId == vp.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) StudioAccentContainer else StudioSurfaceVariant)
                        .border(1.dp, if (isSelected) StudioAccentLight else StudioBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            onUpdateVoice(scene.voice.copy(voiceId = vp.id, speakerName = vp.name))
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = vp.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else StudioTextPrimary)
                        Text(text = vp.tag, fontSize = 7.5.sp, color = StudioTextMuted, maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Script Readout preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "ТЕКСТ ДЛЯ ОЗВУЧКИ:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StudioTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scene.script.ifBlank { "Скрипт сцены пуст. Добавьте текст в панели Script & Scenes." },
                    fontSize = 11.sp,
                    color = StudioTextPrimary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LIVE MIC VOICEOVER RECORDING TOOL
        Text(
            text = "ЗАПИСАТЬ ДИКТОРА (МИКРОФОН)",
            fontSize = 10.sp,
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
