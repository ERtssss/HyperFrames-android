package com.saalpa.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
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

@Composable
fun HyperFramesBottomToolbar(
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CapCutCardBorder)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HyperFramesToolItem(
                    title = "Изменить",
                    icon = Icons.Default.ContentCut,
                    isSelected = activeTab == StudioTab.EDIT,
                    testTag = "tab_edit",
                    onClick = { onTabSelected(StudioTab.EDIT) }
                )

                HyperFramesToolItem(
                    title = "Элементы",
                    icon = Icons.Default.Layers,
                    isSelected = activeTab == StudioTab.ELEMENTS,
                    testTag = "tab_elements",
                    onClick = { onTabSelected(StudioTab.ELEMENTS) }
                )

                HyperFramesToolItem(
                    title = "Аудио",
                    icon = Icons.Default.MusicNote,
                    isSelected = activeTab == StudioTab.AUDIO,
                    testTag = "tab_audio",
                    onClick = { onTabSelected(StudioTab.AUDIO) }
                )

                HyperFramesToolItem(
                    title = "Медиа",
                    icon = Icons.Default.FolderZip,
                    isSelected = activeTab == StudioTab.MEDIA,
                    testTag = "tab_media",
                    onClick = { onTabSelected(StudioTab.MEDIA) }
                )

                HyperFramesToolItem(
                    title = "Код IDE",
                    icon = Icons.Default.Code,
                    isSelected = activeTab == StudioTab.CODE,
                    testTag = "tab_code",
                    onClick = { onTabSelected(StudioTab.CODE) }
                )

                HyperFramesToolItem(
                    title = "Галерея",
                    icon = Icons.Default.VideoLibrary,
                    isSelected = activeTab == StudioTab.GALLERY,
                    testTag = "tab_gallery",
                    onClick = { onTabSelected(StudioTab.GALLERY) }
                )
            }
        }
    }
}

@Composable
private fun HyperFramesToolItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CapCutSurfaceVariant else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) CapCutCyan else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CapCutCyan else TextMuted,
            maxLines = 1
        )
    }
}
