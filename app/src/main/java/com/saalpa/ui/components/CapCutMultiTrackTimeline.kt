package com.saalpa.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.MediaOverlayItem
import com.saalpa.model.MediaType
import com.saalpa.model.SceneMarker
import com.saalpa.ui.theme.CapCutBlue
import com.saalpa.ui.theme.CapCutCardBorder
import com.saalpa.ui.theme.CapCutCardBorderSubtle
import com.saalpa.ui.theme.CapCutCyan
import com.saalpa.ui.theme.CapCutGreen
import com.saalpa.ui.theme.CapCutPink
import com.saalpa.ui.theme.CapCutSurface
import com.saalpa.ui.theme.CapCutSurfaceVariant
import com.saalpa.ui.theme.CapCutTrackBg
import com.saalpa.ui.theme.CapCutTrackHeader
import com.saalpa.ui.theme.CapCutYellow
import com.saalpa.ui.theme.OnCapCutCyan
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun CapCutMultiTrackTimeline(
    currentTimeSec: Float,
    durationSec: Float,
    fps: Int,
    isPlaying: Boolean,
    timelineZoom: Float,
    mediaOverlays: List<MediaOverlayItem>,
    selectedElementId: String?,
    sceneMarkers: List<SceneMarker>,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onStepFrame: (Int) -> Unit,
    onSelectElement: (String?) -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDuplicateSelected: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onAddMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    // Base width: 1 second = 80dp * zoom
    val pxPerSec = (85f * timelineZoom).coerceIn(40f, 220f)
    val totalTimelineWidthDp = (durationSec * pxPerSec).coerceAtLeast(340f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(CapCutSurface),
        color = CapCutSurface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. CapCut Timeline Action Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(CapCutSurfaceVariant)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause + Timecode
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CapCutCyan)
                            .testTag("capcut_play_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = OnCapCutCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = String.format(Locale.US, "%02d:%04.1f", (currentTimeSec / 60).toInt(), currentTimeSec % 60),
                        color = CapCutCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " / " + String.format(Locale.US, "%02d:%04.1f", (durationSec / 60).toInt(), durationSec % 60),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Action buttons: Split, Delete, Duplicate, Zoom
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val hasSelection = selectedElementId != null

                    // Split Button (Разрезать)
                    IconButton(
                        onClick = onSplitAtPlayhead,
                        enabled = hasSelection,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Split",
                            tint = if (hasSelection) TextPrimary else TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete Button (Удалить)
                    IconButton(
                        onClick = onDeleteSelected,
                        enabled = hasSelection,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = if (hasSelection) Color(0xFFFF5252) else TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Duplicate Button
                    IconButton(
                        onClick = onDuplicateSelected,
                        enabled = hasSelection,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = if (hasSelection) TextPrimary else TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Step -1 / +1 Frame
                    IconButton(onClick = { onStepFrame(-1) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.FastRewind, contentDescription = "-1 Frame", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = { onStepFrame(1) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.FastForward, contentDescription = "+1 Frame", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }

                    // Zoom toggle
                    IconButton(
                        onClick = {
                            val nextZoom = if (timelineZoom >= 2.0f) 1.0f else (timelineZoom + 0.5f)
                            onZoomChange(nextZoom)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (timelineZoom > 1.0f) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = "Zoom",
                            tint = CapCutCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. Multi-Track Scrollable Timeline Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(CapCutTrackBg)
            ) {
                // Horizontal Scrollable Container for Ruler and Tracks
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                ) {
                    // Left spacer for padding / playhead lead
                    Spacer(modifier = Modifier.width(36.dp))

                    Box(
                        modifier = Modifier
                            .width(totalTimelineWidthDp.dp)
                            .fillMaxHeight()
                            .pointerInput(durationSec, pxPerSec) {
                                detectTapGestures { offset ->
                                    val tappedSec = (offset.x / (pxPerSec * density)).coerceIn(0f, durationSec)
                                    onSeek(tappedSec)
                                }
                            }
                            .pointerInput(durationSec, pxPerSec) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val draggedSec = (change.position.x / (pxPerSec * density)).coerceIn(0f, durationSec)
                                    onSeek(draggedSec)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Time Ruler with second labels (00:00, 00:01, 00:02...)
                            TimeRulerView(
                                durationSec = durationSec,
                                pxPerSec = pxPerSec,
                                totalWidthDp = totalTimelineWidthDp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Track 1: Main Video & HTML Canvas Animation Track
                            TrackRow(
                                title = "Основное видео",
                                trackColor = CapCutBlue,
                                heightDp = 34
                            ) {
                                MainVideoTrackBlock(
                                    durationSec = durationSec,
                                    totalWidthDp = totalTimelineWidthDp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Track 2: Overlays (Photos & Videos)
                            val visualOverlays = mediaOverlays.filter { it.type == MediaType.PHOTO || it.type == MediaType.VIDEO }
                            TrackRow(
                                title = "Наложение (PIP)",
                                trackColor = CapCutPink,
                                heightDp = 30
                            ) {
                                visualOverlays.forEach { item ->
                                    OverlayClipBlock(
                                        item = item,
                                        pxPerSec = pxPerSec,
                                        isSelected = item.id == selectedElementId,
                                        accentColor = if (item.type == MediaType.PHOTO) CapCutCyan else CapCutPink,
                                        icon = if (item.type == MediaType.PHOTO) Icons.Default.Image else Icons.Default.Movie,
                                        onSelect = { onSelectElement(if (item.id == selectedElementId) null else item.id) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Track 3: Audio & Voiceover Tracks
                            val audioItems = mediaOverlays.filter { it.type == MediaType.VOICEOVER || it.type == MediaType.BGM }
                            TrackRow(
                                title = "Аудио / Голос",
                                trackColor = CapCutGreen,
                                heightDp = 30
                            ) {
                                audioItems.forEach { item ->
                                    OverlayClipBlock(
                                        item = item,
                                        pxPerSec = pxPerSec,
                                        isSelected = item.id == selectedElementId,
                                        accentColor = CapCutGreen,
                                        icon = if (item.type == MediaType.VOICEOVER) Icons.Default.Mic else Icons.Default.MusicNote,
                                        onSelect = { onSelectElement(if (item.id == selectedElementId) null else item.id) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Track 4: Text & Effects Tracks
                            val textAndFxItems = mediaOverlays.filter { it.type == MediaType.TEXT || it.type == MediaType.EFFECT }
                            TrackRow(
                                title = "Текст / Эффекты",
                                trackColor = CapCutYellow,
                                heightDp = 30
                            ) {
                                textAndFxItems.forEach { item ->
                                    OverlayClipBlock(
                                        item = item,
                                        pxPerSec = pxPerSec,
                                        isSelected = item.id == selectedElementId,
                                        accentColor = if (item.type == MediaType.TEXT) CapCutYellow else CapCutPink,
                                        icon = if (item.type == MediaType.TEXT) Icons.Default.TextFields else Icons.Default.AutoAwesome,
                                        onSelect = { onSelectElement(if (item.id == selectedElementId) null else item.id) }
                                    )
                                }
                            }
                        }

                        // CapCut Playhead Line (Draggable Cyan Line with Top Pointer)
                        val playheadOffsetDp = (currentTimeSec * pxPerSec)
                        PlayheadView(offsetDp = playheadOffsetDp)
                    }

                    // Right spacer for scrubbing past the end
                    Spacer(modifier = Modifier.width(60.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeRulerView(
    durationSec: Float,
    pxPerSec: Float,
    totalWidthDp: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(CapCutTrackHeader)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSeconds = durationSec.toInt() + 1
            for (sec in 0..totalSeconds) {
                val xPos = sec * pxPerSec * density

                // Major second tick
                drawLine(
                    color = Color(0xFF6E6E85),
                    start = Offset(xPos, 8f),
                    end = Offset(xPos, size.height),
                    strokeWidth = 1.5f
                )

                // Half second tick
                val halfX = xPos + (pxPerSec * density / 2f)
                if (halfX <= size.width) {
                    drawLine(
                        color = Color(0xFF4A4A5A),
                        start = Offset(halfX, size.height - 6f),
                        end = Offset(halfX, size.height),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Second text labels
        val stepSec = if (pxPerSec > 80f) 1 else 2
        for (sec in 0..durationSec.toInt() step stepSec) {
            val labelOffset = (sec * pxPerSec)
            Text(
                text = String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60),
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .offset { IntOffset(x = (labelOffset * density).toInt() + 4, y = 2) }
            )
        }
    }
}

@Composable
private fun TrackRow(
    title: String,
    trackColor: Color,
    heightDp: Int,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(CapCutTrackBg, RoundedCornerShape(4.dp))
            .border(0.5.dp, CapCutCardBorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
    ) {
        content()
    }
}

@Composable
private fun MainVideoTrackBlock(
    durationSec: Float,
    totalWidthDp: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        shape = RoundedCornerShape(4.dp),
        color = CapCutBlue.copy(alpha = 0.22f),
        border = androidx.compose.foundation.BorderStroke(1.dp, CapCutBlue.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = CapCutBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Видео Сцена (HTML / GSAP Timeline)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Text(
                text = "${durationSec}s",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun OverlayClipBlock(
    item: MediaOverlayItem,
    pxPerSec: Float,
    isSelected: Boolean,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit
) {
    val startOffsetDp = item.startTimeSec * pxPerSec
    val widthDp = (item.durationSec * pxPerSec).coerceAtLeast(24f)

    Box(
        modifier = Modifier
            .offset { IntOffset(x = (startOffsetDp * density).toInt(), y = 0) }
            .width(widthDp.dp)
            .fillMaxHeight()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.45f) else accentColor.copy(alpha = 0.22f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else accentColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else accentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (item.type == MediaType.TEXT) item.textContent else item.title,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayheadView(offsetDp: Float) {
    Box(
        modifier = Modifier
            .offset { IntOffset(x = (offsetDp * density).toInt() - 6, y = 0) }
            .fillMaxHeight()
            .width(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Cyan Pointer Header
        Canvas(modifier = Modifier.size(12.dp, 10.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = CapCutCyan)
        }

        // Vertical playhead line
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(CapCutCyan)
                .shadow(4.dp, spotColor = CapCutCyan)
        )
    }
}
