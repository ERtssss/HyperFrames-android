package com.saalpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.ViewCarousel
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
import com.saalpa.ui.theme.StudioAccent
import com.saalpa.ui.theme.StudioAccentContainer
import com.saalpa.ui.theme.StudioAccentLight
import com.saalpa.ui.theme.StudioBorder
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.StudioTextMuted
import com.saalpa.ui.theme.StudioTextPrimary
import com.saalpa.ui.theme.StudioTextSecondary

enum class StudioActivePanel(val title: String, val icon: ImageVector) {
    SCENES("Scenes", Icons.Default.ViewCarousel),
    SCRIPT("Script", Icons.Default.Description),
    FILES("Files", Icons.Default.Folder),
    INSPECTOR("Inspector", Icons.Default.FormatPaint);

    companion object {
        val VOICE get() = SCRIPT
        val MEDIA get() = FILES
        val AVATAR_VOICE get() = SCRIPT
    }
}

@Composable
fun StudioPanelTabs(
    activePanel: StudioActivePanel,
    onSelectPanel: (StudioActivePanel) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StudioSurface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioActivePanel.values().forEach { panel ->
                val isSelected = panel == activePanel

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) StudioAccentContainer else StudioSurfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) StudioAccentLight else StudioBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectPanel(panel) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("tab_${panel.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = panel.icon,
                        contentDescription = panel.title,
                        tint = if (isSelected) StudioAccentLight else StudioTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = panel.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else StudioTextSecondary
                    )
                }
            }
        }
    }
}
