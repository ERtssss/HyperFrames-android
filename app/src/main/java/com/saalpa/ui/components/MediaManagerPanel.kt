package com.saalpa.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.MediaOverlayItem
import com.saalpa.model.MediaType
import com.saalpa.model.PresetMediaAssets
import com.saalpa.model.SceneMarker
import com.saalpa.model.VoiceoverState
import com.saalpa.ui.theme.AmberGlow
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
fun MediaManagerPanel(
    mediaOverlays: List<MediaOverlayItem>,
    voiceoverState: VoiceoverState,
    sceneMarkers: List<SceneMarker>,
    currentTimeSec: Float,
    onAddPhotoUri: (Uri) -> Unit,
    onAddVideoUri: (Uri) -> Unit,
    onAddAudioUri: (Uri, isVoiceover: Boolean) -> Unit,
    onAddPresetPhoto: (MediaOverlayItem) -> Unit,
    onAddPresetVideo: (MediaOverlayItem) -> Unit,
    onAddPresetVoiceover: (MediaOverlayItem) -> Unit,
    onAddPresetBgm: (MediaOverlayItem) -> Unit,
    onUpdateMedia: (MediaOverlayItem) -> Unit,
    onRemoveMedia: (String) -> Unit,
    onToggleMediaEnabled: (String) -> Unit,
    onStartVoiceover: () -> Unit,
    onStopVoiceover: () -> Unit,
    onCancelVoiceover: () -> Unit,
    onSetVoiceoverVolume: (Float) -> Unit,
    onSetBgmVolume: (Float) -> Unit,
    onToggleVoiceoverMute: () -> Unit,
    onToggleBgmMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMediaTab by remember { mutableIntStateOf(0) }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAddPhotoUri(it) }
    }
    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAddVideoUri(it) }
    }
    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onAddAudioUri(it, true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        // Sub-tabs for Media Categories
        TabRow(
            selectedTabIndex = selectedMediaTab,
            containerColor = StudioDarkBg,
            contentColor = PrimaryBrand,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedMediaTab]),
                    color = PrimaryBrand,
                    height = 2.5.dp
                )
            },
            divider = {}
        ) {
            val tabs = listOf(
                "📸 Фото" to MediaType.PHOTO,
                "🎬 Видео" to MediaType.VIDEO,
                "🎙️ Озвучка" to MediaType.VOICEOVER
            )
            tabs.forEachIndexed { index, (label, _) ->
                Tab(
                    selected = selectedMediaTab == index,
                    onClick = { selectedMediaTab = index },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedMediaTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedMediaTab == index) PrimaryBrand else TextMuted
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedMediaTab) {
                0 -> {
                    // --- PHOTOS TAB ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBrand,
                                    contentColor = OnPrimaryBrand
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("pick_photo_button")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Загрузить фото", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Sample Photo Presets
                    item {
                        Text("Пресеты стикеров & персонажей:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetMediaAssets.SAMPLE_PHOTOS.forEach { sample ->
                                OutlinedButton(
                                    onClick = { onAddPresetPhoto(sample) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(sample.title.split(" ").first(), fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }

                    val photos = mediaOverlays.filter { it.type == MediaType.PHOTO }
                    if (photos.isEmpty()) {
                        item {
                            EmptyMediaCard(
                                title = "Нет добавленных фото",
                                description = "Нажмите «Загрузить фото» или выберите пресет для добавления в анимацию."
                            )
                        }
                    } else {
                        items(photos, key = { it.id }) { item ->
                            MediaItemCard(
                                item = item,
                                sceneMarkers = sceneMarkers,
                                onUpdate = onUpdateMedia,
                                onRemove = { onRemoveMedia(item.id) },
                                onToggle = { onToggleMediaEnabled(item.id) }
                            )
                        }
                    }
                }

                1 -> {
                    // --- VIDEOS TAB ---
                    item {
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pick_video_button")
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Добавить видеофутаж", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Video Presets
                    item {
                        Text("Пресеты видеоэффектов:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetMediaAssets.SAMPLE_VIDEOS.forEach { sample ->
                                OutlinedButton(
                                    onClick = { onAddPresetVideo(sample) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(sample.title, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }

                    val videos = mediaOverlays.filter { it.type == MediaType.VIDEO }
                    if (videos.isEmpty()) {
                        item {
                            EmptyMediaCard(
                                title = "Нет активных видеоклипов",
                                description = "Добавьте MP4 видео для наложения в сцену с синхронизацией по времени."
                            )
                        }
                    } else {
                        items(videos, key = { it.id }) { item ->
                            MediaItemCard(
                                item = item,
                                sceneMarkers = sceneMarkers,
                                onUpdate = onUpdateMedia,
                                onRemove = { onRemoveMedia(item.id) },
                                onToggle = { onToggleMediaEnabled(item.id) }
                            )
                        }
                    }
                }

                2 -> {
                    // --- VOICEOVER & AUDIO TAB ---
                    item {
                        VoiceoverRecorderCard(
                            isRecording = voiceoverState.isRecording,
                            recordingDurationSec = voiceoverState.recordingDurationSec,
                            onStart = onStartVoiceover,
                            onStop = onStopVoiceover,
                            onCancel = onCancelVoiceover
                        )
                    }

                    // Volume Master Controls
                    item {
                        AudioVolumeMasterCard(
                            voiceoverVolume = voiceoverState.voiceoverVolume,
                            bgmVolume = voiceoverState.bgmVolume,
                            isVoiceoverMuted = voiceoverState.isVoiceoverMuted,
                            isBgmMuted = voiceoverState.isBgmMuted,
                            onVoiceoverVolChange = onSetVoiceoverVolume,
                            onBgmVolChange = onSetBgmVolume,
                            onToggleVoiceoverMute = onToggleVoiceoverMute,
                            onToggleBgmMute = onToggleBgmMute,
                            onPickCustomAudio = { audioPickerLauncher.launch("audio/*") }
                        )
                    }

                    // Sample Voiceover & Music Presets
                    item {
                        Text("Пресеты звуков & музыки:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetMediaAssets.SAMPLE_VOICEOVERS.forEach { sample ->
                                OutlinedButton(
                                    onClick = {
                                        if (sample.type == MediaType.VOICEOVER) onAddPresetVoiceover(sample)
                                        else onAddPresetBgm(sample)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(sample.title.split(":").last().trim(), fontSize = 9.sp, maxLines = 1)
                                }
                            }
                        }
                    }

                    val audios = mediaOverlays.filter { it.type == MediaType.VOICEOVER || it.type == MediaType.BGM }
                    if (audios.isNotEmpty()) {
                        item {
                            Text("Активные аудиодорожки:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        }
                        items(audios, key = { it.id }) { item ->
                            MediaItemCard(
                                item = item,
                                sceneMarkers = sceneMarkers,
                                onUpdate = onUpdateMedia,
                                onRemove = { onRemoveMedia(item.id) },
                                onToggle = { onToggleMediaEnabled(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceoverRecorderCard(
    isRecording: Boolean,
    recordingDurationSec: Float,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isRecording) CyberPink else StudioCardBorder),
        modifier = Modifier.fillMaxWidth().testTag("voiceover_recorder_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) CyberPink else EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRecording) "ИДЕТ ЗАПИСЬ ОЗВУЧКИ..." else "ЗАПИСЬ ГОЛОСА ДИКТОРА",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) CyberPink else TextPrimary
                    )
                }

                Text(
                    text = String.format("%.1f c", recordingDurationSec),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) CyberPink else TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isRecording) {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberPink,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("start_record_button")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Начать запись диктора (Mic)", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("stop_record_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB4AB))
                    ) {
                        Text("Отмена", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioVolumeMasterCard(
    voiceoverVolume: Float,
    bgmVolume: Float,
    isVoiceoverMuted: Boolean,
    isBgmMuted: Boolean,
    onVoiceoverVolChange: (Float) -> Unit,
    onBgmVolChange: (Float) -> Unit,
    onToggleVoiceoverMute: () -> Unit,
    onToggleBgmMute: () -> Unit,
    onPickCustomAudio: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Микшер громкости", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedButton(
                    onClick = onPickCustomAudio,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBrand)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Импорт аудио", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Voiceover volume slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleVoiceoverMute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isVoiceoverMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute Voice",
                        tint = if (isVoiceoverMuted) Color(0xFFFFB4AB) else PrimaryBrand,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Громкость диктора (Voiceover)", fontSize = 11.sp, color = TextSecondary)
                        Text("${(voiceoverVolume * 100).toInt()}%", fontSize = 11.sp, color = PrimaryBrand, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = voiceoverVolume,
                        onValueChange = onVoiceoverVolChange,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryBrand,
                            activeTrackColor = PrimaryBrand,
                            inactiveTrackColor = StudioCardBorderSubtle
                        )
                    )
                }
            }

            // BGM volume slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleBgmMute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isBgmMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute BGM",
                        tint = if (isBgmMuted) Color(0xFFFFB4AB) else NeonViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Фоновая музыка (BGM)", fontSize = 11.sp, color = TextSecondary)
                        Text("${(bgmVolume * 100).toInt()}%", fontSize = 11.sp, color = NeonViolet, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = bgmVolume,
                        onValueChange = onBgmVolChange,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonViolet,
                            activeTrackColor = NeonViolet,
                            inactiveTrackColor = StudioCardBorderSubtle
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaItemCard(
    item: MediaOverlayItem,
    sceneMarkers: List<SceneMarker>,
    onUpdate: (MediaOverlayItem) -> Unit,
    onRemove: () -> Unit,
    onToggle: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isEnabled) StudioCardBorder else StudioCardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val icon = when (item.type) {
                        MediaType.PHOTO -> Icons.Default.Image
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.VOICEOVER -> Icons.Default.Mic
                        MediaType.BGM -> Icons.Default.MusicNote
                        MediaType.TEXT -> Icons.Default.TextFields
                        MediaType.EFFECT -> Icons.Default.AutoAwesome
                    }
                    val iconColor = when (item.type) {
                        MediaType.PHOTO -> ElectricCyan
                        MediaType.VIDEO -> CyberPink
                        MediaType.VOICEOVER -> EmeraldGreen
                        MediaType.BGM -> NeonViolet
                        MediaType.TEXT -> AmberGlow
                        MediaType.EFFECT -> CyberPink
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isEnabled) TextPrimary else TextMuted
                        )
                        Text(
                            text = "От ${String.format("%.1f", item.startTimeSec)}c · Длительность ${String.format("%.1f", item.durationSec)}c",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (item.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = if (item.isEnabled) PrimaryBrand else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFFB4AB), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Expanded Layer Controls (Position, Scale, Opacity, Animation)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(StudioDarkBg, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    if (item.type == MediaType.PHOTO || item.type == MediaType.VIDEO) {
                        // Position X slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Позиция X", fontSize = 10.sp, color = TextMuted)
                            Text("${item.xPercent.toInt()}%", fontSize = 10.sp, color = ElectricCyan)
                        }
                        Slider(
                            value = item.xPercent,
                            onValueChange = { onUpdate(item.copy(xPercent = it)) },
                            valueRange = 0f..100f
                        )

                        // Position Y slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Позиция Y", fontSize = 10.sp, color = TextMuted)
                            Text("${item.yPercent.toInt()}%", fontSize = 10.sp, color = ElectricCyan)
                        }
                        Slider(
                            value = item.yPercent,
                            onValueChange = { onUpdate(item.copy(yPercent = it)) },
                            valueRange = 0f..100f
                        )

                        // Scale slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Масштаб (Scale)", fontSize = 10.sp, color = TextMuted)
                            Text("${String.format("%.1f", item.scale)}x", fontSize = 10.sp, color = PrimaryBrand)
                        }
                        Slider(
                            value = item.scale,
                            onValueChange = { onUpdate(item.copy(scale = it)) },
                            valueRange = 0.3f..2.5f
                        )
                    }

                    // Start Time slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Начало появления", fontSize = 10.sp, color = TextMuted)
                        Text("${String.format("%.1f", item.startTimeSec)}c", fontSize = 10.sp, color = EmeraldGreen)
                    }
                    Slider(
                        value = item.startTimeSec,
                        onValueChange = { onUpdate(item.copy(startTimeSec = it)) },
                        valueRange = 0f..30f
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMediaCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
