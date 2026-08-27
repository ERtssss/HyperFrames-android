package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TemplateRepository
import com.example.model.VideoTemplate
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonViolet
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
fun TemplateSelector(
    selectedTemplate: VideoTemplate,
    onSelectTemplate: (VideoTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MOTION PRESETS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = PrimaryBrand
            )
            Text(
                text = "${TemplateRepository.templates.size} Templates",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(TemplateRepository.templates, key = { it.id }) { template ->
                val isSelected = template.id == selectedTemplate.id
                val icon = getTemplateIcon(template.id)
                val accentColor = getTemplateAccent(template.id)

                TemplateCard(
                    template = template,
                    isSelected = isSelected,
                    icon = icon,
                    accentColor = accentColor,
                    onClick = { onSelectTemplate(template) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: VideoTemplate,
    isSelected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(175.dp)
            .height(125.dp)
            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(16.dp), spotColor = accentColor.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("template_card_${template.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryBrandContainer.copy(alpha = 0.35f) else StudioSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PrimaryBrand else StudioCardBorderSubtle
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Category Badge & Selection Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = PrimaryBrand,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${template.defaultDurationSec.toInt()}s",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Content: Name & Category
                Column {
                    Text(
                        text = template.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = template.category,
                        color = if (isSelected) PrimaryBrand else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun getTemplateIcon(id: String): ImageVector {
    return when (id) {
        "kinetic_typography" -> Icons.Default.TextFields
        "cyberpunk_hud" -> Icons.Default.AutoAwesome
        "aesthetic_story" -> Icons.Default.Style
        "code_typer" -> Icons.Default.Code
        "canvas_galaxy_wave" -> Icons.Default.MovieFilter
        "stat_counter" -> Icons.Default.DataExploration
        else -> Icons.Default.AutoAwesome
    }
}

private fun getTemplateAccent(id: String): Color {
    return when (id) {
        "kinetic_typography" -> PrimaryBrand
        "cyberpunk_hud" -> CyberPink
        "aesthetic_story" -> NeonViolet
        "code_typer" -> EmeraldGreen
        "canvas_galaxy_wave" -> NeonViolet
        "stat_counter" -> PrimaryBrand
        else -> PrimaryBrand
    }
}

