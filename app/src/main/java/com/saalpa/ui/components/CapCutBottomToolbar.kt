package com.saalpa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.ui.StudioTab
import com.saalpa.ui.theme.CapCutCardBorder
import com.saalpa.ui.theme.CapCutCyan
import com.saalpa.ui.theme.CapCutSurface
import com.saalpa.ui.theme.CapCutSurfaceVariant
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary

@Composable
fun CapCutBottomToolbar(
    activeTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(CapCutSurface),
        color = CapCutSurface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CapCutCardBorder)
            )

            // Horizontal Toolbar of CapCut Tools
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CapCutToolItem(
                    title = "Изменить",
                    icon = Icons.Default.ContentCut,
                    isSelected = activeTab == StudioTab.EDIT,
                    testTag = "tab_edit",
                    onClick = { onTabSelected(StudioTab.EDIT) }
                )

                CapCutToolItem(
                    title = "Аудио",
                    icon = Icons.Default.MusicNote,
                    isSelected = activeTab == StudioTab.AUDIO,
                    testTag = "tab_audio",
                    onClick = { onTabSelected(StudioTab.AUDIO) }
                )

                CapCutToolItem(
                    title = "Текст",
                    icon = Icons.Default.TextFields,
                    isSelected = activeTab == StudioTab.TEXT,
                    testTag = "tab_text",
                    onClick = { onTabSelected(StudioTab.TEXT) }
                )

                CapCutToolItem(
                    title = "Наложение",
                    icon = Icons.Default.Layers,
                    isSelected = activeTab == StudioTab.OVERLAY,
                    testTag = "tab_overlay",
                    onClick = { onTabSelected(StudioTab.OVERLAY) }
                )

                CapCutToolItem(
                    title = "Эффекты",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = activeTab == StudioTab.EFFECTS,
                    testTag = "tab_effects",
                    onClick = { onTabSelected(StudioTab.EFFECTS) }
                )

                CapCutToolItem(
                    title = "Формат",
                    icon = Icons.Default.AspectRatio,
                    isSelected = activeTab == StudioTab.RATIO,
                    testTag = "tab_ratio",
                    onClick = { onTabSelected(StudioTab.RATIO) }
                )

                CapCutToolItem(
                    title = "Код HTML",
                    icon = Icons.Default.Code,
                    isSelected = activeTab == StudioTab.CODE,
                    testTag = "tab_code",
                    onClick = { onTabSelected(StudioTab.CODE) }
                )

                CapCutToolItem(
                    title = "Галерея",
                    icon = Icons.Default.Folder,
                    isSelected = activeTab == StudioTab.GALLERY,
                    testTag = "tab_gallery",
                    onClick = { onTabSelected(StudioTab.GALLERY) }
                )
            }
        }
    }
}

@Composable
private fun CapCutToolItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CapCutSurfaceVariant else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) CapCutCyan else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CapCutCyan else TextMuted,
            maxLines = 1
        )
    }
}
