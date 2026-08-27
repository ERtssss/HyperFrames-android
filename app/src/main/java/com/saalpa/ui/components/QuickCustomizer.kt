package com.saalpa.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saalpa.model.ParamType
import com.saalpa.model.TemplateParam
import com.saalpa.model.VideoTemplate
import com.saalpa.ui.theme.CyberPink
import com.saalpa.ui.theme.ElectricCyan
import com.saalpa.ui.theme.EmeraldGreen
import com.saalpa.ui.theme.NeonViolet
import com.saalpa.ui.theme.PrimaryBrand
import com.saalpa.ui.theme.StudioCardBorder
import com.saalpa.ui.theme.StudioCardBorderSubtle
import com.saalpa.ui.theme.StudioSurface
import com.saalpa.ui.theme.StudioSurfaceVariant
import com.saalpa.ui.theme.TextMuted
import com.saalpa.ui.theme.TextPrimary
import com.saalpa.ui.theme.TextSecondary

private val PRESET_COLORS = listOf(
    "#6750A4" to PrimaryBrand,
    "#7F39FB" to NeonViolet,
    "#E11D48" to CyberPink,
    "#10B981" to EmeraldGreen,
    "#F59E0B" to Color(0xFFF59E0B),
    "#1C1B1F" to TextPrimary,
    "#3B82F6" to Color(0xFF3B82F6),
    "#FFFFFF" to Color(0xFFFFFFFF)
)

@Composable
fun QuickCustomizer(
    template: VideoTemplate,
    paramsMap: Map<String, String>,
    onUpdateParam: (String, String) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = PrimaryBrand,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CUSTOMIZE PARAMETERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = PrimaryBrand
                    )
                }

                Text(
                    text = "${template.params.size} properties",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(180.dp)
            ) {
                items(template.params, key = { it.key }) { param ->
                    val currentValue = paramsMap[param.key] ?: param.defaultValue

                    when (param.type) {
                        ParamType.COLOR_HEX -> {
                            ColorPickerField(
                                label = param.label,
                                currentColor = currentValue,
                                onColorSelected = { onUpdateParam(param.key, it) }
                            )
                        }
                        else -> {
                            TextInputField(
                                label = param.label,
                                value = currentValue,
                                onValueChange = { onUpdateParam(param.key, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBrand,
                unfocusedBorderColor = StudioCardBorderSubtle,
                focusedContainerColor = StudioSurfaceVariant,
                unfocusedContainerColor = StudioSurfaceVariant,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("param_input_${label.lowercase().replace(" ", "_")}")
        )
    }
}

@Composable
private fun ColorPickerField(
    label: String,
    currentColor: String,
    onColorSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Text(
                text = currentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrand
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PRESET_COLORS) { (hex, composeColor) ->
                val isSelected = currentColor.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(composeColor)
                        .border(
                            if (isSelected) 2.5.dp else 1.dp,
                            if (isSelected) PrimaryBrand else StudioCardBorderSubtle,
                            CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                        .testTag("color_preset_$hex")
                )
            }
        }
    }
}

