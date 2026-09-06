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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.SceneMarker
import com.saalpa.ui.theme.ElectricCyan
import com.saalpa.ui.theme.EmeraldGreen
import com.saalpa.ui.theme.NeonViolet
import com.saalpa.ui.theme.OnPrimaryBrand
import com.saalpa.ui.theme.PrimaryBrand
import com.saalpa.ui.theme.PrimaryBrandContainer
import com.saalpa.ui.theme.StudioCardBorder
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TimelineBar(
    currentTimeSec: Float,
    durationSec: Float,
    fps: Int,
    isPlaying: Boolean,
    playbackSpeed: Float = 1.0f,
    sceneMarkers: List<SceneMarker> = emptyList(),
    activeSceneIndex: Int = 0,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onStepFrame: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit = {},
    onJumpToScene: (SceneMarker) -> Unit = {},
    onPrevScene: () -> Unit = {},
    onNextScene: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalFrames = (durationSec * fps).toInt().coerceAtLeast(1)
    val currentFrame = ((currentTimeSec / durationSec) * totalFrames).toInt().coerceIn(0, totalFrames - 1)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = StudioSurface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Row: Timecode, Frame Number, FPS & Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%02d:%05.2f", (currentTimeSec / 60).toInt(), currentTimeSec % 60),
                        color = PrimaryBrand,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " / " + String.format(Locale.US, "%02d:%05.2f", (durationSec / 60).toInt(), durationSec % 60),
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Badges (FPS + Speed selector)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Frame badge
                    Text(
                        text = "F $currentFrame / $totalFrames",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(StudioSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    // Speed Pill Selector
                    val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                    val nextSpeed = speeds[(speeds.indexOf(playbackSpeed) + 1).takeIf { it in speeds.indices } ?: 0]
                    Text(
                        text = "${playbackSpeed}x",
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(0.8.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable { onSpeedChange(nextSpeed) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Slider Scrub Bar
            Slider(
                value = currentTimeSec,
                onValueChange = onSeek,
                valueRange = 0f..durationSec,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryBrand,
                    activeTrackColor = PrimaryBrand,
                    inactiveTrackColor = StudioSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("timeline_slider")
            )

            // GSAP Scene Markers (if detected/available)
            if (sceneMarkers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sceneMarkers.forEach { marker ->
                        val isSelected = marker.index == activeSceneIndex ||
                                (currentTimeSec >= marker.startSec && currentTimeSec < marker.startSec + marker.durationSec)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) PrimaryBrandContainer else StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                if (isSelected) PrimaryBrand else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable { onJumpToScene(marker) }
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = "🎬 ${marker.title}",
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryBrand else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Row: Playback & Step Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jump to Start
                IconButton(
                    onClick = { onSeek(0f) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Rewind to Start",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Previous Scene / Step -1 Frame
                IconButton(
                    onClick = { onStepFrame(-1) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Frame",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play / Pause Main Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape, spotColor = PrimaryBrand.copy(alpha = 0.4f))
                        .clip(CircleShape)
                        .background(PrimaryBrand)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = OnPrimaryBrand,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Step +1 Frame
                IconButton(
                    onClick = { onStepFrame(1) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Frame",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Next Scene or Loop marker
                IconButton(
                    onClick = onNextScene,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Scene",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
