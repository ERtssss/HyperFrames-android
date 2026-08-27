package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.saalpa.ui.theme.ElectricCyan
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
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onStepFrame: (Int) -> Unit,
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
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Row: Timecode, Frame Number, FPS Indicator
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
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Frame badge
                    Text(
                        text = "F $currentFrame / $totalFrames",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(StudioSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    // FPS badge
                    Text(
                        text = "$fps FPS",
                        color = PrimaryBrand,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(PrimaryBrandContainer, RoundedCornerShape(8.dp))
                            .border(0.8.dp, PrimaryBrand.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
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
                    .height(30.dp)
                    .testTag("timeline_slider")
            )

            // Bottom Row: Playback & Step Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jump to Start
                IconButton(
                    onClick = { onSeek(0f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Rewind to Start",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Step -1 Frame
                IconButton(
                    onClick = { onStepFrame(-1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Frame",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Main Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape, spotColor = PrimaryBrand.copy(alpha = 0.4f))
                        .clip(CircleShape)
                        .background(PrimaryBrand)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = OnPrimaryBrand,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Step +1 Frame
                IconButton(
                    onClick = { onStepFrame(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Frame",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Loop / Time scrub label
                Text(
                    text = String.format(Locale.US, "%.1fs", durationSec),
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

