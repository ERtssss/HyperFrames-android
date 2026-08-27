package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonViolet
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

@Composable
fun ArchitectureExplainerModal(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = StudioSurface,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("explainer_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBrandContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = PrimaryBrand,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HOW HYPERFRAMES WORKS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBrand,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "HTML to Video Engine on Mobile",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ExplainerStepCard(
                            stepNumber = "1",
                            title = "Headless Web Engine",
                            subtitle = "Android Chromium WebView executes HTML5 DOM, CSS animations & JavaScript Canvas 2D/WebGL in background.",
                            icon = Icons.Default.Code,
                            accentColor = PrimaryBrand
                        )
                    }

                    item {
                        ExplainerStepCard(
                            stepNumber = "2",
                            title = "Deterministic Virtual Clock",
                            subtitle = "Instead of lagging with real-time timers, the engine feeds frame timestamps (--time & --progress) for 100% smooth frame-by-frame rendering.",
                            icon = Icons.Default.Timeline,
                            accentColor = NeonViolet
                        )
                    }

                    item {
                        ExplainerStepCard(
                            stepNumber = "3",
                            title = "Direct Framebuffer Capture",
                            subtitle = "Each frame is drawn from the hardware-accelerated WebView pipeline directly into RGBA Bitmaps at the target resolution (1080p, 720p).",
                            icon = Icons.Default.Speed,
                            accentColor = CyberPink
                        )
                    }

                    item {
                        ExplainerStepCard(
                            stepNumber = "4",
                            title = "Hardware H.264 Video Encoder",
                            subtitle = "Android MediaCodec & MediaMuxer encode captured frames into standard MP4 video files directly on device storage.",
                            icon = Icons.Default.Movie,
                            accentColor = EmeraldGreen
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "100% Offline & Private: No external servers or cloud renderers needed. Everything runs natively on your smartphone hardware.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = PrimaryBrand.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand, contentColor = OnPrimaryBrand)
                ) {
                    Text("Got It! Let's Create", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExplainerStepCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

