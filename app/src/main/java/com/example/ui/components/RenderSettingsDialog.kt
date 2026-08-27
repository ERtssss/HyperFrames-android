package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioType
import com.example.model.RenderResolution
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.OnPrimaryBrand
import com.example.ui.theme.PrimaryBrand
import com.example.ui.theme.PrimaryBrandContainer
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCardBorderSubtle
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RenderSettingsDialog(
    aspectRatio: AspectRatioType,
    resolution: RenderResolution,
    fps: Int,
    durationSec: Float,
    onAspectRatioChange: (AspectRatioType) -> Unit,
    onResolutionChange: (RenderResolution) -> Unit,
    onFpsChange: (Int) -> Unit,
    onDurationChange: (Float) -> Unit,
    onStartRender: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(14.dp)
        ) {
            LazyColumn(
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Aspect Ratio
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = null,
                                tint = PrimaryBrand,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ASPECT RATIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBrand,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AspectRatioType.values()) { ratio ->
                                val isSelected = ratio == aspectRatio
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PrimaryBrandContainer else StudioSurfaceVariant)
                                        .border(
                                            1.2.dp,
                                            if (isSelected) PrimaryBrand else StudioCardBorderSubtle,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onAspectRatioChange(ratio) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("ratio_${ratio.name.lowercase()}")
                                ) {
                                    Text(
                                        text = ratio.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryBrand else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Resolution
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = PrimaryBrand,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OUTPUT RESOLUTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBrand,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RenderResolution.values().forEach { res ->
                                val isSelected = res == resolution
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PrimaryBrandContainer else StudioSurfaceVariant)
                                        .border(
                                            1.2.dp,
                                            if (isSelected) PrimaryBrand else StudioCardBorderSubtle,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onResolutionChange(res) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = res.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryBrand else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. FPS & Duration
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // FPS Picker
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = PrimaryBrand,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "FRAMERATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBrand
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(24, 30, 60).forEach { f ->
                                    val isSelected = f == fps
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) PrimaryBrandContainer else StudioSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) PrimaryBrand else StudioCardBorderSubtle,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onFpsChange(f) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$f",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryBrand else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Duration Slider
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = PrimaryBrand,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "DURATION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBrand
                                    )
                                }
                                Text(
                                    text = String.format(Locale.US, "%.1fs", durationSec),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBrand
                                )
                            }
                            Slider(
                                value = durationSec,
                                onValueChange = onDurationChange,
                                valueRange = 1f..10f,
                                steps = 17, // 0.5s steps
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryBrand,
                                    activeTrackColor = PrimaryBrand,
                                    inactiveTrackColor = StudioSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export Button
            Button(
                onClick = onStartRender,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBrand,
                    contentColor = OnPrimaryBrand
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = PrimaryBrand.copy(alpha = 0.4f))
                    .testTag("start_render_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RENDER TO MP4 VIDEO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

